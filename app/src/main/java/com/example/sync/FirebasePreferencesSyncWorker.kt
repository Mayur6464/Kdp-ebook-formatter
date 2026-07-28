package com.example.sync

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import com.example.data.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebasePreferencesSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting Firebase preferences background sync worker...")

            val db = AppDatabase.getDatabase(applicationContext)
            val book = db.bookDao().getFirstBookSync() ?: return Result.success()

            val userId = try {
                FirebaseAuth.getInstance().currentUser?.uid ?: "kdp_author_default_user"
            } catch (e: Exception) {
                "kdp_author_default_user"
            }

            val payload = mapOf(
                "userId" to userId,
                "deviceModel" to Build.MODEL,
                "lastSyncedAt" to System.currentTimeMillis(),
                "exportSettings" to mapOf(
                    "trimWidthInches" to book.trimWidthInches,
                    "trimHeightInches" to book.trimHeightInches,
                    "marginTopInches" to book.marginTopInches,
                    "marginBottomInches" to book.marginBottomInches,
                    "marginLeftInches" to book.marginLeftInches,
                    "marginRightInches" to book.marginRightInches,
                    "gutterInches" to book.gutterInches,
                    "bodyFontFamily" to book.bodyFontFamily,
                    "bodyFontSizePt" to book.bodyFontSizePt,
                    "chapterTitleSizePt" to book.chapterTitleSizePt,
                    "heading2SizePt" to book.heading2SizePt,
                    "enableRunningHeaders" to book.enableRunningHeaders,
                    "enablePageNumbers" to book.enablePageNumbers
                ),
                "checklistPreferences" to mapOf(
                    "coverGenre" to book.coverGenre,
                    "bookTitle" to book.title,
                    "subtitle" to book.subtitle,
                    "author" to book.author
                )
            )

            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("user_preferences")
                    .document(userId)
                    .set(payload, SetOptions.merge())
                    .await()
                Log.d(TAG, "Successfully synced Export Settings & Checklist preferences to Firebase Firestore.")
            } catch (e: Exception) {
                Log.w(TAG, "Firebase sync completed with local cache or offline fallback: ${e.message}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed with exception: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "FirebasePrefSyncWorker"
        const val WORK_NAME = "FirebasePreferencesSyncWork"

        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<FirebasePreferencesSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<FirebasePreferencesSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "${WORK_NAME}_Periodic",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }
    }
}
