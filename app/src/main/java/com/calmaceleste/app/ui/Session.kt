package com.calmaceleste.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmaceleste.app.data.Promise
import com.calmaceleste.app.data.Promises
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.data.SessionEngine
import com.calmaceleste.app.data.SessionLog
import com.calmaceleste.app.data.Step
import com.calmaceleste.app.data.StepType
import com.calmaceleste.app.data.patternByKey
import com.calmaceleste.app.system.SoundKind

/**
 * El corazón de la app: mide la ansiedad, entrega 10 pasos cortos (cuerpo → distracción → conexión),
 * vuelve a medir y, si no hubo mejoría, entrega 10 pasos distintos.
 */
@Composable
fun SessionScreen(onExit: () -> Unit, go: (String) -> Unit) {
    var stage by remember { mutableIntStateOf(0) } // 0 inicio, 1 pasos, 2 revisión, 3 final
    var before by remember { mutableIntStateOf(6) }
    var after by remember { mutableIntStateOf(5) }
    var round by remember { mutableIntStateOf(1) }
    var steps by remember { mutableStateOf<List<Step>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    val used = remember { mutableStateListOf<String>() }
    val helpful = remember { mutableStateListOf<String>() }
    // Promesas de la Biblia: una distinta en cada paso (y al empezar y al revisar).
    val promises = remember { Promises.all.shuffled() }
    fun promiseAt(n: Int): Promise = promises[n % promises.size]

    fun startRound() {
        val s = SessionEngine.buildRound(used.toSet(), first = round == 1)
        steps = s
        used.addAll(s.map { it.id })
        index = 0
        stage = 1
    }

    fun finish() {
        helpful.forEach { Repo.prefs.markHelpful(it) }
        val log = SessionLog(Repo.newId(), System.currentTimeMillis(), before, after, round)
        Repo.sessions.update { it + log }
        stage = 3
    }

    val title = when (stage) {
        0 -> "Vamos a calmarnos"
        1 -> "Ronda $round"
        2 -> "¿Cómo te sientes?"
        else -> "Lo lograste"
    }

    ScreenScaffold(title, onBack = onExit) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
            label = "stage"
        ) { st ->
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (st) {
                    0 -> {
                        SoftCard {
                            Text("¿Qué tan fuerte es tu ansiedad ahora mismo?", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "$before/10 · ${anxietyWord(before)}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            ScoreSlider(before) { before = it }
                            SoftText("0 = nada · 10 = lo máximo")
                        }
                        SoftCard {
                            Text(
                                "Haremos 10 pasos cortos, uno por uno. Puedes saltar los que no quieras. " +
                                    "Si no te calmas, te daré 10 pasos distintos.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        PromiseBanner(promiseAt(0))
                        BigButton("Empezar") { startRound() }
                        TextButton(onClick = { go(Routes.HELP) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Necesito ayuda urgente")
                        }
                    }

                    1 -> {
                        val last = index == steps.lastIndex
                        SoftText("Paso ${index + 1} de ${steps.size} · Ronda $round")
                        LinearProgressIndicator(
                            progress = { (index + 1f) / steps.size.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp))
                        )
                        AnimatedContent(
                            targetState = index,
                            transitionSpec = {
                                (fadeIn(tween(420)) + slideInHorizontally(tween(420)) { it / 8 }) togetherWith
                                    (fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { -it / 8 })
                            },
                            label = "step"
                        ) { i ->
                            val s = steps.getOrNull(i)
                            if (s != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    PromiseBanner(promiseAt((round - 1) * 10 + i + 1))
                                    StepCard(s, go)
                                }
                            }
                        }
                        BigButton(if (last) "Terminar ronda" else "Siguiente paso") {
                            if (last) stage = 2 else index++
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { stage = 2 }) { Text("Ya me siento mejor") }
                            TextButton(onClick = { go(Routes.HELP) }) { Text("Ayuda urgente") }
                        }
                    }

                    2 -> {
                        SoftCard {
                            Text("¿Cómo está tu ansiedad ahora?", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text("$after/10 · ${anxietyWord(after)}", style = MaterialTheme.typography.headlineSmall)
                            ScoreSlider(after) { after = it }
                            val d = before - after
                            SoftText(
                                when {
                                    d > 0 -> "Bajó $d ${if (d == 1) "punto" else "puntos"} desde que empezaste 💙"
                                    d == 0 -> "Sigue igual. Es normal: a veces hace falta más de una ronda."
                                    else -> "Está un poco más alta. Vamos con calma, no estás haciendo nada mal."
                                }
                            )
                        }
                        PromiseBanner(promiseAt(round * 10 + 11))
                        if (steps.isNotEmpty()) {
                            SoftCard {
                                Text("¿Cuáles te ayudaron? (opcional)", style = MaterialTheme.typography.titleMedium)
                                SoftText("La app los usará más seguido la próxima vez.")
                                Spacer(Modifier.height(8.dp))
                                steps.chunked(2).forEach { pair ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        pair.forEach { s ->
                                            FilterChip(
                                                selected = s.id in helpful,
                                                onClick = { if (s.id in helpful) helpful.remove(s.id) else helpful.add(s.id) },
                                                label = { Text(s.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        if (after >= 8) {
                            SoftCard(tint = Color(0xFFFFE6E3)) {
                                Text("No tienes que pasar por esto solo", style = MaterialTheme.typography.titleMedium, color = Color(0xFF5A1F1B))
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Si sientes que no puedes más, que estás en peligro o piensas en hacerte daño, pide ayuda ahora.",
                                    color = Color(0xFF5A1F1B)
                                )
                                Spacer(Modifier.height(10.dp))
                                BigButton("Ver ayuda urgente") { go(Routes.HELP) }
                            }
                        } else if (round >= 2 && after >= 5) {
                            SoftCard {
                                Text("Quizá te haría bien hablar con alguien", style = MaterialTheme.typography.titleMedium)
                                SoftText("Una llamada o un mensaje a alguien de confianza puede aliviar mucho.")
                                Spacer(Modifier.height(10.dp))
                                BigButton("Ir a mis contactos", soft = true) { go(Routes.CONTACTS) }
                            }
                        }
                        BigButton("Me siento mejor") { finish() }
                        BigButton("Quiero 10 pasos más", soft = true) {
                            round++
                            startRound()
                        }
                    }

                    else -> {
                        val d = before - after
                        SoftCard {
                            Text("💙", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (d > 0) "Lo hiciste muy bien" else "Fuiste valiente por intentarlo",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (d > 0) {
                                    "Tu ansiedad pasó de $before a $after. Recuerda lo que te funcionó: lo tienes siempre contigo."
                                } else {
                                    "A veces cuesta. Mañana puede ir mejor. Si la ansiedad se repite seguido, hablar con un profesional puede ayudarte mucho."
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        BigButton("Volver al inicio") { onExit() }
                        BigButton("Anotar cómo me siento en mi diario", soft = true) { go(Routes.DIARY) }
                    }
                }
            }
        }
    }
}

private fun emojiFor(s: Step): String = when (s.type) {
    StepType.BREATH -> "🌬️"
    StepType.GROUND -> "🖐️"
    StepType.TIP -> "✨"
    StepType.JOKE -> "😄"
    StepType.SONG -> "🎵"
    StepType.VIDEO -> "🎬"
    StepType.GAME -> "🎮"
    StepType.MATH -> "🧮"
    StepType.HUM -> "🎶"
    StepType.GOODBOX -> "💌"
    StepType.SOUND -> "🌧️"
    StepType.CONTACT -> "📞"
    StepType.BIBLE -> "🕊️"
}

@Composable
private fun StepCard(step: Step, go: (String) -> Unit) {
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emojiFor(step), fontSize = 30.sp)
            Spacer(Modifier.width(10.dp))
            Text(step.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        if (step.text.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(step.text, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(14.dp))
        when (step.type) {
            StepType.BREATH -> BreathingCircle(
                pattern = patternByKey(step.arg),
                running = true,
                modifier = Modifier.align(Alignment.CenterHorizontally).size(250.dp)
            )
            StepType.GROUND -> GroundingContent(onDone = {})
            StepType.TIP -> CountdownRing(step.seconds, Modifier.align(Alignment.CenterHorizontally))
            StepType.JOKE -> RandomJoke()
            StepType.SONG -> SongPlayerCard()
            StepType.VIDEO -> VideoPlayerCard()
            StepType.GAME -> {
                val m = Modifier.fillMaxWidth().height(300.dp)
                when (step.arg) {
                    "ripples" -> RipplesGame(m)
                    "memory" -> MemoryGame(m)
                    "mandala" -> MandalaColoring(Modifier.fillMaxWidth().height(440.dp))
                    "bubbles" -> BubblesGame(m)
                    "zen" -> ZenGarden(m)
                    else -> FirefliesGame(m)
                }
            }
            StepType.MATH -> MathQuiz(startLevel = step.arg.toIntOrNull() ?: 1)
            StepType.HUM -> HumExercise()
            StepType.GOODBOX -> GoodBoxRandomCard()
            StepType.SOUND -> SoundMini(SoundKind.valueOf(step.arg))
            StepType.CONTACT -> ContactsQuick(onAdd = { go(Routes.CONTACTS) })
            StepType.BIBLE -> BibleStepContent()
        }
    }
}
