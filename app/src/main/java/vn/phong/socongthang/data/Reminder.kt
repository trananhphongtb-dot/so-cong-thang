package vn.phong.socongthang.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "so_cong_reminder"
private const val WORK_NAME = "so_cong_daily_reminder"

fun scheduleReminder(context: Context) {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 20)
        set(Calendar.MINUTE, 30)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
    }
    val delay = target.timeInMillis - now.timeInMillis
    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .addTag(WORK_NAME)
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        WORK_NAME, ExistingWorkPolicy.REPLACE, request
    )
}

class ReminderWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        try {
            val store = Store(applicationContext)
            val days = store.loadDays()
            val cal = Calendar.getInstance()
            val key = dateKey(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val today = days[key]
            val needsReminder = today != null && !today.done &&
                (today.scheduled || today.jobNumbers.isNotEmpty())
            if (needsReminder) {
                showNotification()
            }
        } catch (_: Exception) {
            // Reminder must never crash the worker chain.
        }
        // Chain the next day's check so the reminder keeps working
        // even if the app is not opened again.
        try {
            scheduleReminder(applicationContext)
        } catch (_: Exception) {
        }
        return Result.success()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Nhắc tích công", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(applicationContext, Class.forName("vn.phong.socongthang.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Chưa tích xong hôm nay")
            .setContentText("Hôm nay có hẹn hoặc đã tích nhưng chưa bấm Đã xong. Bấm để mở app tích ngay.")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(1001, notification)
    }
}
