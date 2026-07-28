# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.internal.GeneratedComponent
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep,includedescriptorclasses class com.btween.app.**$$serializer { *; }
-keepclassmembers class com.btween.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.btween.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# App data models (kept for reflection-based serialization/backup)
-keep class com.btween.app.data.** { *; }
-keep class com.btween.app.domain.model.** { *; }

# androidx.security.crypto (EncryptedSharedPreferences) pulls in Google Tink, which
# references error-prone's annotations at compile time only - they're never present at
# runtime and don't need to be kept, just silenced so R8 doesn't fail the build over them.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy

# Retrofit + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface com.btween.app.data.remote.api.**

