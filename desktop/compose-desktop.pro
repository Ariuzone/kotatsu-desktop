# Règles ProGuard pour le packaging desktop (distribué embarqué).
# Les classes ci-dessous ne sont utilisées par OkHttp que sur GraalVM / Android
# et n'existent pas sur desktop JVM standard — avertissements à ignorer.
-dontwarn org.graalvm.nativeimage.**
-dontwarn org.graalvm.polyglot.**
-dontwarn com.oracle.svm.core.annotate.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.openjsse.**
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn kotlinx.serialization.internal.*
-keep class org.jetbrains.skiko.** { *; }

# Room KMP + sqlite-bundled : pont JNI (méthodes natives appelées par nom depuis la
# bibliothèque native) et chargement réflexif des implémentations générées (*_Impl).
-keep class androidx.sqlite.** { *; }
-keep class androidx.room.** { *; }
-keep class org.koitharu.kotatsu.core.db.** { *; }
-keep class org.koitharu.kotatsu.favourites.data.** { *; }
-keep class org.koitharu.kotatsu.history.data.** { *; }
-keep class org.koitharu.kotatsu.bookmarks.data.** { *; }
-keep class org.koitharu.kotatsu.local.data.index.** { *; }
-keep class org.koitharu.kotatsu.tracker.data.** { *; }
-keep class org.koitharu.kotatsu.stats.data.** { *; }
-keep class org.koitharu.kotatsu.scrobbling.** { *; }
-keep class org.koitharu.kotatsu.explore.data.** { *; }
-keep class org.koitharu.kotatsu.suggestions.data.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# okio : ProGuard casse les fonctions inline (VerifyError buffer$hash) en build release
-dontobfuscate
