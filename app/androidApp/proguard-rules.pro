# ProGuard rules for Merge2048.
# See https://developer.android.com/build/shrink-r8

# ---- kotlinx.serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes.
-keep,includedescriptorclasses class com.finley.android.merge2048.**$$serializer { *; }

-keepclassmembers class com.finley.android.merge2048.domain.** {
    *** Companion;
}
-keepclasseswithmembers class com.finley.android.merge2048.domain.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- General Android / R8 ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Compose tooling preview functions.
-keep class androidx.compose.ui.tooling.** { *; }

# ---- Ktor (server module) ----
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
