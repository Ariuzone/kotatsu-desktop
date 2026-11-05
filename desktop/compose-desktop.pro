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
