/*
 * Fichier modifié — portage Windows de Kotatsu-Redo (https://github.com/Kotatsu-Redo/Kotatsu-Redo).
 * Dérivé de l'app Android Kotatsu-Redo (GPLv3). Modification : 2026-11-06 — adaptation JVM/desktop.
 * Adaptation : WorkManager (DownloadWorker + Scheduler + PausingReceiver/broadcasts) remplacé
 * par un service en coroutines : file d'attente séquentielle, téléchargement des pages en
 * parallèle borné (comme l'original), progression publiée via StateFlow. Les notifications
 * Android, l'image proxy et le slowdown dispatcher ne sont pas portés ; le mime type des
 * fichiers est déduit de l'URL et du Content-Type HTTP (pas de décodage Bitmap sur desktop).
 */
package org.koitharu.kotatsu.download.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.buffer
import okio.sink
import okio.use
import org.koitharu.kotatsu.local.domain.model.isLocal
import org.koitharu.kotatsu.core.parser.MangaRepository
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.DownloadFormat
import org.koitharu.kotatsu.core.util.MimeTypes
import org.koitharu.kotatsu.core.util.Throttler
import org.koitharu.kotatsu.core.util.ext.MimeType
import org.koitharu.kotatsu.core.util.ext.deleteAwait
import org.koitharu.kotatsu.core.util.ext.ensureSuccess
import org.koitharu.kotatsu.core.util.ext.toMimeType
import org.koitharu.kotatsu.core.util.ext.writeAllCancellable
import org.koitharu.kotatsu.core.util.progress.RealtimeEtaEstimator
import org.koitharu.kotatsu.local.data.LocalMangaRepository
import org.koitharu.kotatsu.local.data.index.LocalMangaIndex
import org.koitharu.kotatsu.local.data.input.LocalMangaParser
import org.koitharu.kotatsu.local.data.output.LocalMangaOutput
import org.koitharu.kotatsu.local.domain.MangaLock
import org.koitharu.kotatsu.local.domain.model.LocalManga
import org.koitharu.kotatsu.parsers.exception.TooManyRequestExceptions
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.util.ifNullOrEmpty
import org.koitharu.kotatsu.parsers.util.runCatchingCancellable
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Service de téléchargement desktop : remplace WorkManager + DownloadWorker de l'app Android.
 * File d'attente séquentielle (un manga à la fois), pages téléchargées en parallèle
 * ([MAX_PAGES_PARALLELISM] max), progression observable via [observeState].
 *
 * Câblage attendu (orchestrateur) :
 * ```
 * DownloadManager(
 *   scope = appCoroutineScope,
 *   okHttp = networkModule.mangaHttpClient,
 *   localMangaRepository = localRepository,
 *   mangaRepositoryFactory = MangaRepository.Factory(loaderContext, contentCache, mirrorSwitcher, localRepository),
 *   localMangaIndex = index,
 *   settings = settings,
 *   lock = MangaLock(),
 *   localStorageChanges = MutableSharedFlow(),
 * )
 * ```
 */
class DownloadManager(
	private val scope: CoroutineScope,
	private val okHttp: OkHttpClient,
	private val localMangaRepository: LocalMangaRepository,
	private val mangaRepositoryFactory: MangaRepository.Factory,
	private val localMangaIndex: LocalMangaIndex,
	private val settings: AppSettings,
	private val lock: MangaLock,
	private val localStorageChanges: MutableSharedFlow<LocalManga?>,
) {

	private class QueuedDownload(
		val manga: Manga,
		val task: DownloadTask,
		val pausingHandle: PausingHandle = PausingHandle(),
	) {
		var job: Job? = null

		@Volatile
		var isCancelled: Boolean = false
	}

	private val channel = Channel<QueuedDownload>(Channel.UNLIMITED)
	private val downloads = ConcurrentHashMap<Long, QueuedDownload>()
	private val statesMap = ConcurrentHashMap<Long, DownloadState>()
	private val _states = MutableStateFlow<List<DownloadState>>(emptyList())

	init {
		scope.launch {
			for (download in channel) {
				if (download.isCancelled) {
					continue
				}
				val job = launch { downloadMangaImpl(download) }
				download.job = job
				job.join()
				downloads.remove(download.manga.id)
			}
		}
	}

	/** États des téléchargements (en file, en cours, terminés, en erreur). */
	fun observeState(): StateFlow<List<DownloadState>> = _states.asStateFlow()

	fun enqueue(
		manga: Manga,
		chaptersIds: Set<Long>? = null,
		destination: File? = null,
		format: DownloadFormat? = null,
		allowMeteredNetwork: Boolean = true,
	) {
		val task = DownloadTask(
			mangaId = manga.id,
			chaptersIds = chaptersIds?.toLongArray(),
			destination = destination,
			format = format,
			allowMeteredNetwork = allowMeteredNetwork,
		)
		val download = QueuedDownload(manga, task)
		downloads[manga.id] = download
		publish(DownloadState(manga = manga, isIndeterminate = true))
		channel.trySend(download)
	}

	fun pause(mangaId: Long) {
		val download = downloads[mangaId] ?: return
		download.pausingHandle.pause()
		statesMap[mangaId]?.let { publish(it.copy(isPaused = true, eta = -1L, isStuck = false)) }
	}

	fun resume(mangaId: Long) {
		val download = downloads[mangaId] ?: return
		download.pausingHandle.resume()
		statesMap[mangaId]?.let { publish(it.copy(isPaused = false)) }
	}

	/** Saute la page/le chapitre en erreur (ou toutes les erreurs suivantes avec [skipAll]). */
	fun skip(mangaId: Long) {
		downloads[mangaId]?.pausingHandle?.skip()
	}

	fun skipAll(mangaId: Long) {
		downloads[mangaId]?.pausingHandle?.skipAll()
	}

	/** Annule le téléchargement en cours ou retire la tâche en attente pour ce manga. */
	fun cancel(mangaId: Long) {
		val download = downloads.remove(mangaId) ?: return
		download.isCancelled = true
		statesMap[mangaId]?.let { state ->
			publish(state.copy(isStopped = true, eta = -1L, isStuck = false))
		}
		download.job?.cancel()
	}

	fun cancelAll() {
		for (mangaId in downloads.keys.toList()) {
			cancel(mangaId)
		}
	}

	/**
	 * Port de DownloadWorker.downloadMangaImpl : télécharge la couverture puis les chapitres
	 * demandés, écrit via [LocalMangaOutput] selon le format, publie la progression.
	 */
	private suspend fun downloadMangaImpl(download: QueuedDownload) {
		val task = download.task
		val pausingHandle = download.pausingHandle
		val etaEstimator = RealtimeEtaEstimator()
		val progressThrottler = Throttler(PROGRESS_THROTTLE_MS)
		val lastPublishedState = AtomicReference<DownloadState>(
			DownloadState(manga = download.manga, isIndeterminate = true),
		)

		suspend fun currentState(): DownloadState = checkNotNull(lastPublishedState.get())

		suspend fun publishState(state: DownloadState) {
			val previousState = currentState()
			lastPublishedState.set(state)
			if (previousState.isParticularProgress && state.isParticularProgress) {
				etaEstimator.onProgressChanged(state.progress, state.max)
			} else {
				etaEstimator.reset()
				progressThrottler.reset()
			}
			commitState(state)
		}

		suspend fun checkIsPaused() {
			if (pausingHandle.isPaused) {
				publishState(currentState().copy(isPaused = true, eta = -1L, isStuck = false))
				try {
					pausingHandle.awaitResumed()
				} finally {
					publishState(currentState().copy(isPaused = false))
				}
			}
		}

		suspend fun <R> runFailsafe(block: suspend () -> R): R? {
			checkIsPaused()
			var countDown = MAX_FAILSAFE_ATTEMPTS
			failsafe@ while (true) {
				try {
					return block()
				} catch (e: IOException) {
					val retryDelay = if (e is TooManyRequestExceptions) e.getRetryDelay() else DOWNLOAD_ERROR_DELAY
					if (countDown <= 0 || retryDelay < 0 || retryDelay > MAX_RETRY_DELAY) {
						if (pausingHandle.skipAllErrors()) {
							return null
						}
						publishState(
							currentState().copy(
								isPaused = true,
								error = e,
								errorMessage = e.message ?: e.javaClass.simpleName,
								eta = -1L,
								isStuck = false,
							),
						)
						countDown = MAX_FAILSAFE_ATTEMPTS
						pausingHandle.pause()
						try {
							pausingHandle.awaitResumed()
							if (pausingHandle.skipCurrentError()) {
								return null
							}
						} finally {
							publishState(currentState().copy(isPaused = false, error = null, errorMessage = null))
						}
					} else {
						countDown--
						delay(retryDelay)
					}
				}
			}
		}

		publishState(DownloadState(manga = download.manga, isIndeterminate = true))
		var manga = download.manga
		try {
			val downloadedIds = getDoneChapters(manga)
			val chaptersToSkip = downloadedIds.toMutableSet()
			lock.withLock(manga) {
				val destination = localMangaRepository.getOutputDir(manga, task.destination)
				checkNotNull(destination) { "No writable destination for local manga" }
				val tempFiles = ConcurrentLinkedQueue<File>()
				var output: LocalMangaOutput? = null
				try {
					if (manga.isLocal) {
						manga = localMangaRepository.getRemoteManga(manga)
							?: error("Cannot obtain remote manga instance")
					}
					val repo = mangaRepositoryFactory.create(manga.source)
					val mangaDetails = if (manga.chapters.isNullOrEmpty() || manga.description.isNullOrEmpty()) {
						repo.getDetails(manga)
					} else {
						manga
					}
					output = LocalMangaOutput.getOrCreate(
						root = destination,
						manga = mangaDetails,
						format = task.format ?: settings.preferredDownloadFormat,
					)
					val coverUrl = mangaDetails.largeCoverUrl.ifNullOrEmpty { mangaDetails.coverUrl }
					if (!coverUrl.isNullOrEmpty()) {
						downloadFile(coverUrl, destination, tempFiles).let { file ->
							output?.addCover(file, MimeTypes.getMimeTypeFromUrl(coverUrl))
							file.deleteAwait()
						}
					}
					val chapters = getChapters(mangaDetails, task)
					for ((chapterIndex, chapter) in chapters.withIndex()) {
						checkIsPaused()
						if (chaptersToSkip.remove(chapter.value.id)) {
							publishState(currentState().copy(downloadedChapters = currentState().downloadedChapters + 1))
							continue
						}
						val pages = runFailsafe {
							repo.getPages(chapter.value)
						} ?: continue
						val pageCounter = AtomicInteger(0)
						coroutineScope {
							val semaphore = Semaphore(MAX_PAGES_PARALLELISM)
							for ((pageIndex, page) in pages.withIndex()) {
								checkIsPaused()
								launch {
									semaphore.withPermit {
										val result = runFailsafe {
											val url = repo.getPageUrl(page)
											val file = downloadFile(url, destination, tempFiles)
											output?.addPage(
												chapter = chapter,
												file = file,
												pageNumber = pageIndex,
												type = MimeTypes.getMimeTypeFromUrl(url),
											)
											if (file.extension == "tmp") {
												file.deleteAwait()
											}
										}
										val done = pageCounter.getAndIncrement() + 1
										val progress = DownloadProgress(
											totalChapters = chapters.size,
											currentChapter = chapterIndex,
											totalPages = pages.size,
											currentPage = done,
										)
										val base = currentState()
										val next = base.copy(
											totalChapters = progress.totalChapters,
											currentChapter = progress.currentChapter,
											totalPages = progress.totalPages,
											currentPage = progress.currentPage,
											isIndeterminate = false,
											eta = etaEstimator.getEta(),
											isStuck = etaEstimator.isStuck(),
										)
										lastPublishedState.set(next)
										if (progressThrottler.throttle()) {
											commitState(next)
										}
										result
									}
								}
							}
						}
						if (output?.flushChapter(chapter.value) == true) {
							runCatchingCancellable {
								localStorageChanges.emit(
									LocalMangaParser(output!!.rootFile).getManga(withDetails = false),
								)
							}.onFailure { it.printStackTrace() }
						}
						publishState(currentState().copy(downloadedChapters = currentState().downloadedChapters + 1))
					}
					publishState(currentState().copy(isIndeterminate = true, eta = -1L, isStuck = false))
					output?.mergeWithExisting()
					output?.finish()
					val localManga = LocalMangaParser(output!!.rootFile).getManga(withDetails = false)
					localMangaIndex.put(localManga)
					localStorageChanges.emit(localManga)
					publishState(currentState().copy(localManga = localManga, eta = -1L, isStuck = false))
				} catch (e: Exception) {
					if (e !is CancellationException) {
						publishState(
							currentState().copy(
								error = e,
								errorMessage = e.message ?: e.javaClass.simpleName,
							),
						)
					}
					throw e
				} finally {
					withContext(NonCancellable) {
						output?.use { output ->
							runCatchingCancellable { output.cleanup() }
								.onFailure { e -> e.printStackTrace() }
						}
						tempFiles.forEach { file ->
							runCatchingCancellable { file.deleteAwait() }
								.onFailure { e -> e.printStackTrace() }
						}
					}
				}
			}
		} catch (e: CancellationException) {
			publishState(currentState().copy(isStopped = true, eta = -1L, isStuck = false))
			throw e
		}
	}

	private fun commitState(state: DownloadState) {
		statesMap[state.manga.id] = state
		_states.update { list ->
			val index = list.indexOfFirst { it.manga.id == state.manga.id }
			if (index >= 0) {
				list.toMutableList().apply { this[index] = state }
			} else {
				list + state
			}
		}
	}

	private fun publish(state: DownloadState) = commitState(state)

	/** Télécharge un fichier distant dans un fichier temporaire de [destination]. */
	private suspend fun downloadFile(
		url: String,
		destination: File,
		tempFiles: MutableCollection<File>,
	): File {
		val request = Request.Builder()
			.url(url)
			.build()
		val call = okHttp.newCall(request)
		val response = call.awaitInterruptible()
		response.ensureSuccess().use { resp ->
			val body = checkNotNull(resp.body)
			var file: File? = null
			try {
				file = destination.createTempFile(
					MimeTypes.getExtension(body.contentType()?.toMimeType()),
				).also { tempFiles += it }
				file.sink(append = false).buffer().use {
					it.writeAllCancellable(body.source())
				}
				return file
			} catch (e: Exception) {
				file?.delete()
				throw e
			}
		}
	}

	private suspend fun okhttp3.Call.awaitInterruptible() = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
		continuation.invokeOnCancellation { cancel() }
		try {
			val response = execute()
			if (continuation.isActive) {
				continuation.resumeWith(Result.success(response))
			} else {
				response.close()
			}
		} catch (e: Exception) {
			if (continuation.isActive) {
				continuation.resumeWith(Result.failure(e))
			}
		}
	}

	private fun File.createTempFile(ext: String?) = File(
		this,
		buildString {
			append(UUID.randomUUID().toString())
			if (!ext.isNullOrEmpty()) {
				append('.')
				append(ext)
			}
			append(".tmp")
		},
	)

	/** Chapitres déjà présents localement : ils seront sautés (reprise des téléchargements). */
	private suspend fun getDoneChapters(manga: Manga): Set<Long> = runCatchingCancellable {
		localMangaRepository.getDetails(manga).chapters?.map { it.id }
	}.getOrNull().orEmpty().toSet()

	private fun getChapters(
		manga: Manga,
		task: DownloadTask,
	): List<IndexedValue<MangaChapter>> {
		val chapters = checkNotNull(manga.chapters) { "Chapters list must not be null" }
		val chaptersIdsSet = task.chaptersIds?.toMutableSet()
		val result = ArrayList<IndexedValue<MangaChapter>>((chaptersIdsSet ?: chapters).size)
		val counters = HashMap<String?, Int>()
		for (chapter in chapters) {
			val index = counters[chapter.branch] ?: 0
			counters[chapter.branch] = index + 1
			if (chaptersIdsSet != null && !chaptersIdsSet.remove(chapter.id)) {
				continue
			}
			result.add(IndexedValue(index, chapter))
		}
		if (chaptersIdsSet != null) {
			check(chaptersIdsSet.isEmpty()) {
				"${chaptersIdsSet.size} of ${task.chaptersIds?.size} requested chapters not found in manga"
			}
		}
		check(result.isNotEmpty()) { "Chapters list must not be empty" }
		return result
	}

	private companion object {

		const val MAX_FAILSAFE_ATTEMPTS = 2
		const val MAX_PAGES_PARALLELISM = 4
		const val DOWNLOAD_ERROR_DELAY = 2_000L
		const val MAX_RETRY_DELAY = 7_200_000L // 2 hours
		const val PROGRESS_THROTTLE_MS = 400L
	}
}
