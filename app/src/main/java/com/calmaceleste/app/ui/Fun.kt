package com.calmaceleste.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmaceleste.app.data.Joke
import com.calmaceleste.app.data.Repo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ---------------------------------------------------------------------------------------------
// Chistes (300, guardados en el teléfono: app/src/main/assets/chistes.txt)
// ---------------------------------------------------------------------------------------------

@Composable
private fun JokeBody(joke: Joke?, reveal: Boolean, onReveal: () -> Unit) {
    if (joke == null) {
        SoftText("No hay chistes en esta lista todavía.")
        return
    }
    Text(joke.text, style = MaterialTheme.typography.bodyLarge)
    val punch = joke.punchline
    if (punch != null) {
        Spacer(Modifier.height(10.dp))
        AnimatedVisibility(visible = reveal, enter = fadeIn(tween(400)) + expandVertically()) {
            Text(
                punch,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (!reveal) TextButton(onClick = onReveal) { Text("Ver respuesta") }
    }
}

/** Un chiste al azar, para usar dentro de los 10 pasos. */
@Composable
fun RandomJoke() {
    var joke by remember { mutableStateOf(Repo.jokes.randomOrNull()) }
    var reveal by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        JokeBody(joke, reveal) { reveal = true }
        OutlinedButton(
            onClick = {
                joke = Repo.jokes.randomOrNull()
                reveal = false
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Otro chiste") }
    }
}

@Composable
fun JokesScreen(onBack: () -> Unit) {
    val cats = remember { listOf("Todos", "Favoritos") + Repo.jokeCategories }
    var sel by remember { mutableIntStateOf(0) }
    var favs by remember { mutableStateOf(Repo.prefs.favoriteJokes()) }
    var joke by remember { mutableStateOf<Joke?>(null) }
    var reveal by remember { mutableStateOf(false) }

    fun pool(): List<Joke> = when (sel) {
        0 -> Repo.jokes
        1 -> Repo.jokes.filter { it.id in favs }
        else -> Repo.jokes.filter { it.category == cats[sel] }
    }

    fun next() {
        val p = pool()
        val current = joke?.id
        joke = p.filter { it.id != current }.randomOrNull() ?: p.randomOrNull()
        reveal = false
    }

    LaunchedEffect(sel) { next() }

    ScreenScaffold("Chistes", onBack) {
        SoftText("${Repo.jokes.size} chistes guardados en tu teléfono. No necesitas Internet.")
        ChipRow(cats, sel) { sel = it }
        SoftCard {
            JokeBody(joke, reveal) { reveal = true }
            val j = joke
            if (j != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val isFav = j.id in favs
                    IconButton(onClick = {
                        Repo.prefs.toggleFavorite(j.id)
                        favs = Repo.prefs.favoriteJokes()
                    }) {
                        Icon(
                            if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFav) "Quitar de favoritos" else "Guardar en favoritos",
                            tint = if (isFav) Color(0xFFE57C9B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        BigButton("Otro chiste") { next() }
    }
}

// ---------------------------------------------------------------------------------------------
// Mates para calmar la mente
// ---------------------------------------------------------------------------------------------

private data class MathQ(val text: String, val answer: Int, val options: List<Int>)

private fun optionsFor(answer: Int): List<Int> {
    val set = linkedSetOf(answer)
    var guard = 0
    while (set.size < 4 && guard < 50) {
        guard++
        val delta = Random.nextInt(1, 6) * (if (Random.nextBoolean()) 1 else -1)
        val v = answer + delta
        if (v >= 0) set.add(v)
    }
    var extra = 1
    while (set.size < 4) set.add(answer + extra++)
    return set.toList().shuffled()
}

private fun makeQuestion(level: Int): MathQ {
    when (level) {
        1 -> {
            return if (Random.nextBoolean()) {
                val a = Random.nextInt(2, 15)
                val b = Random.nextInt(2, 12)
                MathQ("$a + $b = ?", a + b, optionsFor(a + b))
            } else {
                val a = Random.nextInt(10, 25)
                val b = Random.nextInt(2, 10)
                MathQ("$a − $b = ?", a - b, optionsFor(a - b))
            }
        }
        2 -> {
            return when (Random.nextInt(3)) {
                0 -> {
                    val start = Random.nextInt(60, 100)
                    MathQ("$start − 7 = ?", start - 7, optionsFor(start - 7))
                }
                1 -> {
                    val s = Random.nextInt(1, 10)
                    val d = Random.nextInt(2, 6)
                    val ans = s + 4 * d
                    MathQ("${s}, ${s + d}, ${s + 2 * d}, ${s + 3 * d}, ¿?", ans, optionsFor(ans))
                }
                else -> {
                    val a = Random.nextInt(11, 40)
                    val b = Random.nextInt(11, 40)
                    MathQ("$a + $b = ?", a + b, optionsFor(a + b))
                }
            }
        }
        else -> {
            return when (Random.nextInt(3)) {
                0 -> {
                    val a = Random.nextInt(3, 10)
                    val b = Random.nextInt(3, 10)
                    MathQ("$a × $b = ?", a * b, optionsFor(a * b))
                }
                1 -> {
                    val s = Random.nextInt(2, 9)
                    val m = Random.nextInt(2, 4)
                    val ans = s * m * m * m * m
                    MathQ("${s}, ${s * m}, ${s * m * m}, ${s * m * m * m}, ¿?", ans, optionsFor(ans))
                }
                else -> {
                    val x = Random.nextInt(5, 20)
                    val b = Random.nextInt(5, 30)
                    MathQ("¿Qué número sumado a $x da ${x + b}?", b, optionsFor(b))
                }
            }
        }
    }
}

private const val QUESTIONS_PER_SESSION = 5

/**
 * Sin cronómetro, sin errores sonoros, sin puntaje negativo. Si fallas, se tacha esa opción y sigues.
 * Se adapta: tras dos fallos seguidos baja de nivel; tras tres aciertos seguidos sube.
 */
@Composable
fun MathQuiz(startLevel: Int = 1) {
    var level by remember { mutableIntStateOf(startLevel.coerceIn(1, 3)) }
    var q by remember { mutableStateOf(makeQuestion(startLevel.coerceIn(1, 3))) }
    var done by remember { mutableIntStateOf(0) }
    var streakOk by remember { mutableIntStateOf(0) }
    var streakBad by remember { mutableIntStateOf(0) }
    val wrong = remember { mutableStateOf(setOf<Int>()) }
    var solved by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(solved) {
        if (solved) {
            delay(900)
            done++
            if (done < QUESTIONS_PER_SESSION) {
                q = makeQuestion(level)
                wrong.value = emptySet()
                solved = false
                message = ""
            }
        }
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (done >= QUESTIONS_PER_SESSION) {
            Text("🌟", fontSize = 40.sp)
            Text(
                "Muy bien. Tu mente se concentró en algo distinto. Eso ayuda.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = {
                    done = 0
                    streakOk = 0
                    streakBad = 0
                    q = makeQuestion(level)
                    wrong.value = emptySet()
                    solved = false
                    message = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cinco más") }
        } else {
            SoftText("Pregunta ${done + 1} de $QUESTIONS_PER_SESSION · Nivel ${listOf("fácil", "medio", "reto")[level - 1]}")
            Text(
                q.text,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            q.options.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { opt ->
                        val isWrong = opt in wrong.value
                        val isRight = solved && opt == q.answer
                        OutlinedButton(
                            onClick = {
                                if (solved) return@OutlinedButton
                                if (opt == q.answer) {
                                    solved = true
                                    message = "¡Muy bien!"
                                    streakOk++
                                    streakBad = 0
                                    if (streakOk >= 3 && level < 3) {
                                        level++
                                        streakOk = 0
                                    }
                                } else {
                                    wrong.value = wrong.value + opt
                                    message = "Casi, inténtalo otra vez"
                                    streakBad++
                                    streakOk = 0
                                    if (streakBad >= 2 && level > 1) {
                                        level--
                                        streakBad = 0
                                    }
                                }
                            },
                            enabled = !isWrong,
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text(
                                if (isRight) "$opt ✓" else "$opt",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MathScreen(onBack: () -> Unit) {
    var start by remember { mutableIntStateOf(1) }
    ScreenScaffold("Mates para calmar la mente", onBack) {
        SoftCard {
            Text(
                "Concentrarte en una cuenta sencilla le da un respiro a los pensamientos ansiosos. " +
                    "No hay tiempo ni puntos: solo tú y los números.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        ChipRow(listOf("Fácil", "Medio", "Reto"), start - 1) { start = it + 1 }
        key(start) {
            SoftCard { MathQuiz(startLevel = start) }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tararea para calmarte
// ---------------------------------------------------------------------------------------------

/**
 * Inhalar 4 s y tararear 8 s. Tararear hace vibrar el pecho y alarga la exhalación, lo que ayuda a calmar el cuerpo.
 * No usa el micrófono, así que la app no necesita ese permiso.
 */
@Composable
fun HumExercise(autoStart: Boolean = true) {
    var running by remember { mutableStateOf(autoStart) }
    var rounds by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("Listo") }
    var left by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(0.5f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(running) {
        if (!running) {
            label = "En pausa"
            return@LaunchedEffect
        }
        while (true) {
            label = "Inhala por la nariz"
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            coroutineScope {
                launch { scale.animateTo(1f, tween(4000, easing = FastOutSlowInEasing)) }
                for (s in 4 downTo 1) { left = s; delay(1000) }
            }
            label = "Mmmmm… tararea"
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            coroutineScope {
                launch { scale.animateTo(0.5f, tween(8000, easing = FastOutSlowInEasing)) }
                for (s in 8 downTo 1) { left = s; delay(1000) }
            }
            rounds++
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val base = size.minDimension / 2f
                val r = base * scale.value
                drawCircle(primary.copy(alpha = 0.18f), radius = (r * 1.2f).coerceAtMost(base))
                drawCircle(primary.copy(alpha = 0.55f), radius = r * 0.85f)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0B3954), textAlign = TextAlign.Center)
                if (running && left > 0) Text("$left", fontSize = 28.sp, fontWeight = FontWeight.Light, color = Color(0xFF0B3954))
            }
        }
        SoftText(
            if (rounds == 0) "Puedes tararear la melodía de tu canción favorita."
            else "Rondas completadas: $rounds 💙"
        )
        OutlinedButton(onClick = { running = !running }, modifier = Modifier.fillMaxWidth()) {
            Text(if (running) "Pausar" else "Empezar")
        }
    }
}

@Composable
fun HumScreen(onBack: () -> Unit) {
    ScreenScaffold("Tararea para calmarte", onBack) {
        SoftCard {
            Text(
                "Tararear activa el nervio vago y relaja el cuerpo. Inhala 4 segundos y tararea suave 8 segundos, " +
                    "sintiendo la vibración en el pecho y en los labios. Prueba con una canción lenta.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        SoftCard { HumExercise(autoStart = false) }
    }
}
