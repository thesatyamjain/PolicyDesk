# PolicyDesk R8 / Proguard Rules for Production Release

# Kotlin Coroutines
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }

# AndroidX Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Room Entities & DAOs
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.EntityDeletionOrUpdateAdapter { *; }
-keep class * extends androidx.room.EntityInsertionAdapter { *; }

# WorkManager Workers
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Hilt / Dagger
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject *;
}

# Domain & Data Models (preserves fields for JSON serialization & reflection)
-keep class com.policydesk.app.domain.model.** { *; }
-keep class com.policydesk.app.data.local.entity.** { *; }
-keep class com.policydesk.app.data.ai.ExtractedPolicyData { *; }
-keep class com.policydesk.app.data.security.** { *; }
-keep class com.policydesk.app.data.drive.** { *; }

# Google Play Services Auth & Drive API
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# AndroidX Security Crypto & Biometric
-keep class androidx.security.crypto.** { *; }
-keep class androidx.biometric.** { *; }

# Keep line numbers and source files for crash reports
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
