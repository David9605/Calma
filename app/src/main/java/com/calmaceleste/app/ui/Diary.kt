package com.calmaceleste.app.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmaceleste.app.data.Insights
import com.calmaceleste.app.data.MOOD_EMOJIS
import com.calmaceleste.app.data.MOOD_LABELS
import com.calmaceleste.app.data.MOOD_TAGS
import com.calmaceleste.app.data.MoodEntry
import com.calmaceleste.app.data.Repo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(onBack: () -> Unit) {
    val entries by Repo.mood.items.collectAsState()
    var mood by remember { mutableIntStateOf(0) } // 0 = sin elegir; 1..5
    var note by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var saved by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<MoodEntry?>(null) }

    ScreenScaffold("Mi diario de ánimo", onBack) {
        SoftCard {
            Text("¿Cómo te sientes ahora?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MOOD_EMOJIS.forEachIndexed { i, e ->
                    val selected = mood == i + 1
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .clickable { mood = i + 1; saved = false },
                        contentAlignment = Alignment.Center
                    ) { Text(e, fontSize = if (selected) 32.sp else 26.sp) }
                }
            }
            if (mood > 0) {
                Spacer(Modifier.height(6.dp))
                Text(MOOD_LABELS[mood - 1], modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(12.dp))
            SoftText("¿Qué influye? (opcional)")
            Spacer(Modifier.height(6.dp))
            TagGrid(MOOD_TAGS, tags) { t -> tags = if (t in tags) tags - t else tags + t }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Una nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(10.dp))
            BigButton(if (saved) "Guardado 💙" else "Guardar", enabled = mood > 0 && !saved) {
                Repo.mood.update { it + MoodEntry(Repo.newId(), System.currentTimeMillis(), mood, note.trim(), tags) }
                note = ""
                tags = emptyList()
                saved = true
            }
        }

        SectionTitle("Tu última semana")
        SoftCard { WeekChart(Insights.weekChart(entries)) }

        val insights = remember(entries) { Insights.compute(entries) }
        SectionTitle("Patrones que noto")
        if (insights.isEmpty()) {
            SoftText("Anota cómo te sientes durante unos días (al menos 6 registros) y aquí aparecerán patrones, como los días más difíciles o lo que más te afecta.")
        } else {
            insights.forEach { ins ->
                SoftCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(ins.emoji, fontSize = 24.sp)
                        Text(ins.text, modifier = Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            SoftText("Son observaciones sencillas calculadas en tu teléfono, no un diagnóstico.")
        }

        SectionTitle("Historial")
        if (entries.isEmpty()) SoftText("Aún no hay registros.")
        val fmt = remember { SimpleDateFormat("EEE d MMM, HH:mm", Locale("es")) }
        entries.sortedByDescending { it.time }.take(30).forEach { e ->
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(MOOD_EMOJIS[(e.mood - 1).coerceIn(0, 4)], fontSize = 28.sp)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(fmt.format(Date(e.time)), style = MaterialTheme.typography.titleMedium)
                        if (e.tags.isNotEmpty()) SoftText(e.tags.joinToString(" · "))
                        if (e.note.isNotBlank()) Text(e.note, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { toDelete = e }) { Icon(Icons.Filled.Delete, contentDescription = "Borrar") }
                }
            }
        }
    }

    toDelete?.let { e ->
        ConfirmDialog(
            title = "¿Borrar este registro?",
            text = "No se puede deshacer.",
            onConfirm = {
                Repo.mood.update { l -> l.filter { it.id != e.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun WeekChart(data: List<Pair<String, Float?>>) {
    Row(
        Modifier.fillMaxWidth().height(130.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            val target: Dp = if (value == null) 4.dp else (14 + (value / 5f) * 80f).dp
            val h by animateDpAsState(target, tween(700), label = "bar")
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                    if (value == null) "" else MOOD_EMOJIS[(value.toInt() - 1).coerceIn(0, 4)],
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .width(22.dp)
                        .height(h)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (value == null) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primary
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
