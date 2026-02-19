package com.fiscalia.quindio.worker

import android.content.Context
import androidx.work.*
import com.fiscalia.quindio.data.database.AppDatabase
import com.fiscalia.quindio.repository.StatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class DailyResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = StatRepository(
                database.dailyStatDao(),
                database.historicalStatDao()
            )

            // Archivar estadísticas del día anterior
            repository.archiveDailyStats()

            // Limpiar tabla diaria
            repository.clearDailyStats()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val currentDate = LocalDateTime.now()
            val targetTime = currentDate.with(LocalTime.MIDNIGHT).plusDays(1)
            val delay = Duration.between(currentDate, targetTime).toMillis()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag("daily_reset")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_stats_reset",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("daily_stats_reset")
        }
    }
}