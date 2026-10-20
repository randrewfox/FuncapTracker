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
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val WORK_NAME = "funcap_periodic_reminder_work"
    const val CHANNEL_ID = "funcap_reminders_channel"
    const val EXTRA_TEST_TYPE = "extra_preferred_test_type"
    const val EXTRA_INTERVAL_DAYS = "extra_reminder_interval_days"
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
     * Formats the timeframe description dynamically based on the selected interval in days.
     */
    fun getTimeframeDescription(intervalDays: Int): String {
        return when (intervalDays) {
            1 -> "over the past day"
            7 -> "over the past week"
            14 -> "over the past 2 weeks"
            30 -> "over the past month"
            60 -> "over the past 2 months"
            90 -> "over the past 3 months"
            365 -> "over the past year"
            else -> {
                if (intervalDays > 0 && intervalDays % 7 == 0) {
                    val weeks = intervalDays / 7
                    "over the past $weeks weeks"
                } else {
                    "over the past $intervalDays days"
                }
            }
        }
    }

    /**
     * Calculates the delay in milliseconds so the first notification fires on the target day at the exact hour and minute.
     */
    private fun calculateInitialDelayMillis(intervalDays: Int, targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, intervalDays)
            set(Calendar.HOUR_OF_DAY, targetHour.coerceIn(0, 23))
            set(Calendar.MINUTE, targetMinute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var delay = target.timeInMillis - now.timeInMillis
        if (delay <= 0) {
            delay += TimeUnit.DAYS.toMillis(1)
        }
        return delay
    }

    /**
     * Schedules or updates the periodic FUNCAP reminder at a customizable interval (in days) and target time.
     */
    fun schedulePeriodicReminder(
        context: Context,
        enabled: Boolean,
        intervalDays: Int,
        preferredTestType: TestType,
        reminderHour: Int = 13,
        reminderMinute: Int = 0
    ) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val safeIntervalDays = intervalDays.coerceIn(1, 365).toLong()
        val initialDelayMillis = calculateInitialDelayMillis(intervalDays, reminderHour, reminderMinute)

        val data = workDataOf(
            EXTRA_TEST_TYPE to preferredTestType.name,
            EXTRA_INTERVAL_DAYS to intervalDays
        )

        val request = PeriodicWorkRequestBuilder<FuncapReminderWorker>(
            safeIntervalDays,
            TimeUnit.DAYS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
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
        preferredTestType: TestType,
        intervalDays: Int = 30
    ): Boolean {
        ensureNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
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

        val timeframe = getTimeframeDescription(intervalDays)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("Time for your ${preferredTestType.displayName} Check-In")
            .setContentText(
                "Log your functional capacity $timeframe to keep your trend history up to date."
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "It's time for your scheduled ${preferredTestType.displayName} assessment. " +
                    "Base your answers on an average day $timeframe."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        return true
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

        val intervalDays = inputData.getInt(ReminderScheduler.EXTRA_INTERVAL_DAYS, 30)

        ReminderScheduler.showReminderNotification(
            context = applicationContext,
            preferredTestType = preferredType,
            intervalDays = intervalDays
        )
        return Result.success()
    }
}
