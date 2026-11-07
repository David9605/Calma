package com.calmaceleste.app.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.calmaceleste.app.data.Reminder
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.Scheduler
import java.util.Calendar

private val presets = listOf(
    "🌬️ Pausa de respiración" to (11 to 0),
    "💧 Tomar agua" to (10 to 30),
    "💊 Mi medicación" to (8 to 0),
    "🌙 Hora de dormir" to (22 to 30),
    "🚶 Estirarme y caminar" to (16 to 0)
)

private val dayLabels = listOf("D" to Calendar.SUNDAY, "L" to Calendar.MONDAY, "M" to Calendar.TUESDAY,
    "X" to Calendar.WEDNESDAY, "J" to Calendar.THURSDAY, "V" to Calendar.FRIDAY, "S" to Calendar.SATURDAY)

private fun fmt(h: Int, m: Int) = "%02d:%02d".format(h, m)

@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val list by Repo.reminders.items.collectAsState()
    var editing by remember { mutableStateOf<Reminder?>(null) }
    var dialogOpen by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Reminder?>(null) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun askPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun save(r: Reminder) {
        Repo.reminders.update { l -> if (l.any { it.id == r.id }) l.map { if (it.id == r.id) r else it } else l + r }
        Scheduler.schedule(ctx, r)
    }

    ScreenScaffold("Recordatorios", onBack) {
        SoftCard {
            Text(
                "Pequeñas pausas durante el día: respirar, tomar agua, tu medicación, dormir. " +
                    "Suenan aunque no tengas Internet.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        SectionTitle("Añadir rápido")
        presets.forEach { (title, time) ->
            OutlinedButton(
                onClick = {
                    askPermission()
                    save(Reminder(Repo.newId(), title.substringAfter(' '), time.first, time.second))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("$title · ${fmt(time.first, time.second)}") }
        }
        BigButton("Crear uno a mi medida", soft = true) {
            askPermission()
            editing = null
            dialogOpen = true
        }

        SectionTitle("Mis recordatorios")
        if (list.isEmpty()) SoftText("Todavía no tienes ninguno.")
        list.sortedWith(compareBy({ it.hour }, { it.minute })).forEach { r ->
            SoftCard(onClick = { editing = r; dialogOpen = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(fmt(r.hour, r.minute), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text(r.title, style = MaterialTheme.typography.titleMedium)
                        SoftText(
                            if (r.days.isEmpty()) "Todos los días"
                            else dayLabels.filter { it.second in r.days }.joinToString(" ") { it.first }
                        )
                    }
                    Switch(checked = r.enabled, onCheckedChange = { on -> save(r.copy(enabled = on)) })
                    IconButton(onClick = { toDelete = r }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Borrar")
                    }
                }
            }
        }
        SoftText("Si no te llegan, revisa que las notificaciones estén permitidas y que el ahorro de batería no cierre la app.")
    }

    if (dialogOpen) {
        ReminderDialog(
            initial = editing,
            onSave = { r -> save(r); dialogOpen = false; editing = null },
            onDismiss = { dialogOpen = false; editing = null }
        )
    }
    toDelete?.let { r ->
        ConfirmDialog(
            title = "¿Borrar este recordatorio?",
            text = r.title,
            onConfirm = {
                Scheduler.cancel(ctx, r)
                Repo.reminders.update { l -> l.filter { it.id != r.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun ReminderDialog(initial: Reminder?, onSave: (Reminder) -> Unit, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var hour by remember { mutableIntStateOf(initial?.hour ?: 9) }
    var minute by remember { mutableIntStateOf(initial?.minute ?: 0) }
    var days by remember { mutableStateOf(initial?.days ?: emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo recordatorio" else "Editar recordatorio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("¿Qué quieres recordar?") }, singleLine = true)
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(ctx, { _, h, m -> hour = h; minute = m }, hour, minute, true).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Hora: ${fmt(hour, minute)}", fontSize = 18.sp) }
                Text("Días (sin marcar = todos)", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayLabels.forEach { (label, day) ->
                        FilterChip(
                            selected = day in days,
                            onClick = { days = if (day in days) days - day else days + day },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        Reminder(
                            id = initial?.id ?: Repo.newId(),
                            title = title.trim(),
                            hour = hour,
                            minute = minute,
                            days = days,
                            enabled = initial?.enabled ?: true
                        )
                    )
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
