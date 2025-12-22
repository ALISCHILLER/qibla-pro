# --- General Optimization ---
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepattributes *Annotation*, Signature
-keep class com.msa.qiblapro.app.** { *; }

# --- Google Maps & Services ---
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.maps.android.compose.** { *; }
-keep class com.google.maps.android.clustering.** { *; }

# --- DataStore & Protobuf (if used) ---
-keep class androidx.datastore.** { *; }

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    val SHUTDOWN_PRE_28;
}

# --- Keep Model Classes (Important for Obfuscation) ---
-keep class com.msa.qiblapro.data.settings.AppSettings { *; }
-keep class com.msa.qiblapro.data.settings.ThemeMode { *; }
-keep class com.msa.qiblapro.data.settings.NeonAccent { *; }
-keep class com.msa.qiblapro.util.IranCity { *; }

# --- Parcelable ---
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
