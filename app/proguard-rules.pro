# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# Maps Compose / GMS
-keep class com.google.android.gms.** { *; }
-keep class com.google.maps.android.compose.** { *; }
-keepnames class * implements android.os.Parcelable {
  public static final ** CREATOR;
}
