-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class com.msa.qiblapro.** { *; }

-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.compose.** { *; }

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
