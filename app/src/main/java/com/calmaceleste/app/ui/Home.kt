package com.calmaceleste.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private data class Tile(val emoji: String, val label: String, val route: String)

private val quickTiles = listOf(
    Tile("🌬️", "Respirar", Routes.BREATHING),
    Tile("📖", "Promesas de Dios", Routes.PROMISES),
    Tile("🕊️", "Biblia hablada", Routes.BIBLE),
    Tile("🎵", "Mi canción", Routes.SONGS),
    Tile("😄", "Chistes", Routes.JOKES),
    Tile("🎮", "Juegos", Routes.GAMES),
    Tile("💞", "Mensajes de amor", Routes.LOVE),
    Tile("📞", "Mi gente", Routes.CONTACTS)
)

@Composable
fun HomeScreen(go: (String) -> Unit) {
    val greeting = remember {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            h < 6 -> "Buenas madrugadas"
            h < 12 -> "Buenos días"
            h < 19 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }
    CalmBackground {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "Aquí estás a salvo. Respira.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { go(Routes.SETTINGS) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                }
            }

            Spacer(Modifier.height(20.dp))
            Appear(100) { PulseButton { go(Routes.SESSION) } }
            Spacer(Modifier.height(20.dp))

            quickTiles.chunked(2).forEachIndexed { rowIndex, row ->
                Appear(250 + rowIndex * 120, Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { t ->
                            ToolTile(t, Modifier.weight(1f)) { go(t.route) }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Appear(550, Modifier.fillMaxWidth()) {
                val promise = remember { com.calmaceleste.app.data.Promises.today() }
                SoftCard(onClick = { go(Routes.PROMISES) }) {
                    Text("📖 Promesa de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("«${promise.text}»", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        promise.ref,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Appear(650, Modifier.fillMaxWidth()) {
                SoftCard(onClick = { go(Routes.CRISIS) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧭", fontSize = 28.sp)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Mi plan de crisis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Los pasos que escribí para cuando me sienta mal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { go(Routes.TOOLS) }) { Text("Todas las herramientas") }
                TextButton(onClick = { go(Routes.HELP) }) { Text("Ayuda urgente") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** El botón principal: late despacio, como una respiración. */
@Composable
private fun PulseButton(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseValue"
    )
    val halo = MaterialTheme.colorScheme.primary
    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val base = size.minDimension / 2f
            drawCircle(halo.copy(alpha = 0.16f), radius = base * (0.78f + 0.2f * pulse))
            drawCircle(halo.copy(alpha = 0.22f), radius = base * (0.68f + 0.12f * pulse))
        }
        Box(
            Modifier
                .size(176.dp)
                .graphicsLayer {
                    val s = 1f + 0.04f * pulse
                    scaleX = s
                    scaleY = s
                }
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFFC3E8FA), Color(0xFF6DB9E6))))
                .clickable(role = Role.Button, onClickLabel = "Empezar a calmarme", onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Me siento\nansioso",
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0B3954)
                )
                Text("toca aquí", fontSize = 13.sp, color = Color(0xFF0B3954).copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun ToolTile(t: Tile, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier) {
        SoftCard(onClick = onClick) {
            Text(t.emoji, fontSize = 30.sp)
            Spacer(Modifier.height(6.dp))
            Text(t.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}

private data class ToolItem(val emoji: String, val title: String, val desc: String, val route: String)

private val allTools = listOf(
    ToolItem("💙", "Me siento ansioso (10 pasos)", "Una secuencia guiada que se adapta a ti.", Routes.SESSION),
    ToolItem("🌬️", "Respiración guiada", "4-7-8, en caja, exhalación larga y más.", Routes.BREATHING),
    ToolItem("🖐️", "Anclaje 5-4-3-2-1", "Vuelve al presente con tus sentidos.", Routes.GROUNDING),
    ToolItem("📖", "Promesas de Dios", "Protección, sanidad y que Él nunca te deja.", Routes.PROMISES),
    ToolItem("🕊️", "Biblia hablada", "Escucha con los ojos cerrados, acostado.", Routes.BIBLE),
    ToolItem("💞", "Mensajes de amor", "Notas de voz y videos de quienes te aman.", Routes.LOVE),
    ToolItem("🎵", "Mi canción favorita", "Tu música, sin Internet.", Routes.SONGS),
    ToolItem("🎬", "Mis videos divertidos", "Los que te hacen reír.", Routes.VIDEOS),
    ToolItem("🎶", "Tararea para calmarte", "Ejercicio guiado de tarareo.", Routes.HUM),
    ToolItem("😄", "Chistes", "300 chistes para distraerte.", Routes.JOKES),
    ToolItem("🎮", "Minijuegos relajantes", "Burbujas, jardín zen y luciérnagas.", Routes.GAMES),
    ToolItem("🧮", "Mates para calmar la mente", "Preguntas suaves, sin presión.", Routes.MATH),
    ToolItem("🌧️", "Sonidos relajantes", "Lluvia, mar, viento y temporizador.", Routes.SOUNDS),
    ToolItem("📞", "Contactos de apoyo", "Llama o escribe en un toque.", Routes.CONTACTS),
    ToolItem("⏰", "Recordatorios", "Pausas, agua, medicación, dormir.", Routes.REMINDERS),
    ToolItem("📔", "Diario de ánimo y patrones", "Descubre qué te afecta.", Routes.DIARY),
    ToolItem("💌", "Caja de cosas buenas", "Fotos y mensajes que te hacen bien.", Routes.GOODBOX),
    ToolItem("💭", "Registro de pensamientos", "Cuestiona los pensamientos ansiosos.", Routes.THOUGHTS),
    ToolItem("🧭", "Mi plan de crisis", "Tus pasos, escritos con calma.", Routes.CRISIS),
    ToolItem("🆘", "Ayuda urgente", "Números de emergencia y de apoyo.", Routes.HELP),
    ToolItem("🔒", "Privacidad y ajustes", "PIN, copia de seguridad, valorar y más.", Routes.SETTINGS)
)

@Composable
fun ToolsScreen(go: (String) -> Unit, onBack: () -> Unit) {
    ScreenScaffold("Todas las herramientas", onBack) {
        allTools.forEachIndexed { i, t ->
            Appear(i * 40) {
                SoftCard(onClick = { go(t.route) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t.emoji, fontSize = 28.sp)
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            SoftText(t.desc)
                        }
                    }
                }
            }
        }
    }
}
