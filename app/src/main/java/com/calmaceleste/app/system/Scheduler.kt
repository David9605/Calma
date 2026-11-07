package com.calmaceleste.app.system

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.calmaceleste.app.MainActivity
import com.calmaceleste.app.R
import com.calmaceleste.app.data.Reminder
import com.calmaceleste.app.data.Repo
import java.util.Calendar

object Scheduler {
    const val CHANNEL = "recordatorios"
    const val EXTRA_ID = "reminder_id"

    fun createChannel(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(CHANNEL, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT)
        ch.description = "Pausas de respiración, medicación, agua y descanso"
        nm.createNotificationChannel(ch)
    }

    private fun pending(ctx: Context, r: Reminder): PendingIntent {
        val i = Intent(ctx, ReminderReceiver::class.java).putExtra(EXTRA_ID, r.id)
        return PendingIntent.getBroadcast(
            ctx, r.code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancel(ctx: Context, r: Reminder) {
        ctx.getSystemService(AlarmManager::class.java).cancel(pending(ctx, r))
    }

    /** Programa la próxima ocurrencia. Usa alarma "inexacta con Doze" (sin permisos especiales). */
    fun schedule(ctx: Context, r: Reminder) {
        cancel(ctx, r)
        if (!r.enabled) return
        val next = nextTrigger(r) ?: return
        val am = ctx.getSystemService(AlarmManager::class.java)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending(ctx, r))
    }

    fun scheduleAll(ctx: Context) {
        Repo.reminders.items.value.forEach { schedule(ctx, it) }
    }

    fun nextTrigger(r: Reminder, from: Long = System.currentTimeMillis()): Long? {
        for (offset in 0..7) {
            val c = Calendar.getInstance().apply {
                timeInMillis = from
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, r.hour)
                set(Calendar.MINUTE, r.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayOk = r.days.isEmpty() || c.get(Calendar.DAY_OF_WEEK) in r.days
            if (dayOk && c.timeInMillis > from + 1000) return c.timeInMillis
        }
        return null
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(Scheduler.EXTRA_ID, -1L)
        val r = Repo.reminders.items.value.firstOrNull { it.id == id } ?: return
        if (!r.enabled) return

        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val texts = listOf(
            "Un momento para ti. Respira hondo 💙",
            "Tómate una pausa pequeña. Te la mereces.",
            "Estás haciéndolo bien. Respira con calma."
        )
        val n = NotificationCompat.Builder(context, Scheduler.CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_calm)
            .setContentTitle(r.title)
            .setContentText(texts.random())
            .setAutoCancel(true)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(r.code, n)
        } catch (e: SecurityException) {
            // Sin permiso de notificaciones: no se muestra nada.
        }
        // Reprograma la siguiente.
        Scheduler.schedule(context, r)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Scheduler.scheduleAll(context)
    }
}
