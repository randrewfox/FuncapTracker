package com.funcap.tracker.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.funcap.tracker.MainActivity
import com.funcap.tracker.model.TestType
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val WORK_NAME = "funcap_periodic_reminder_work"
    const val CHANNEL_ID = "funcap_reminders_channel"
    const val EXTRA_TEST_TYPE = "extra_preferred_test_type"
    private const val NOTIFICATION_ID = 1001

    fun ensureNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FUNCAP Assessment Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Periodic reminders to log your FUNCAP55 or FUNCAP27 functional capacity score"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**
     * Schedules or updates the periodic FUNCAP reminder at a customizable interval (in days).
     */
    fun schedulePeriodicReminder(
        context: Context,
        enabled: Boolean,
        intervalDays: Int,
        preferredTestType: TestType
    ) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val safeIntervalDays = intervalDays.coerceIn(1, 365).toLong()
        val data = workDataOf(EXTRA_TEST_TYPE to preferredTestType.name)

        val request = PeriodicWorkRequestBuilder<FuncapReminderWorker>(
            safeIntervalDays,
            TimeUnit.DAYS
        )
            .setInitialDelay(safeIntervalDays, TimeUnit.DAYS)
            .setInputData(data)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun showReminderNotification(
        context: Context,
        preferredTestType: TestType
    ) {
        ensureNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_TEST_TYPE, preferredTestType.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("Time for your ${preferredTestType.displayName} Check-In")
            .setContentText(
                "Log your functional capacity over the past month to keep your trend history up to date."
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "It's time for your scheduled ${preferredTestType.displayName} assessment. " +
                    "Base your answers on an average day during the past month."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

class FuncapReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val rawType = inputData.getString(ReminderScheduler.EXTRA_TEST_TYPE)
        val preferredType = rawType?.let {
            runCatching { TestType.valueOf(it) }.getOrNull()
        } ?: TestType.FUNCAP27

        ReminderScheduler.showReminderNotification(applicationContext, preferredType)
        return Result.success()
    }
}
