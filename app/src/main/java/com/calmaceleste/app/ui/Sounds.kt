package com.calmaceleste.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.SoundKind
import com.calmaceleste.app.system.SoundPlayer
import kotlinx.coroutines.delay

/** Reproductor pequeño para un sonido concreto, dentro de los 10 pasos. */
@Composable
fun SoundMini(kind: SoundKind) {
    val player = remember { SoundPlayer().also { it.volume = Repo.prefs.soundVolume } }
    DisposableEffect(kind) {
        player.start(kind)
        onDispose { player.stop() }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SoftText("Sonando: ${kind.emoji} ${kind.label}")
        OutlinedButton(
            onClick = { if (player.isPlaying) player.stop() else player.start(kind) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (player.isPlaying) "Silenciar" else "Reanudar") }
        CountdownRing(60, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun SoundsScreen(onBack: () -> Unit) {
    val player = remember { SoundPlayer().also { it.volume = Repo.prefs.soundVolume } }
    var current by remember { mutableStateOf<SoundKind?>(null) }
    var volume by remember { mutableFloatStateOf(Repo.prefs.soundVolume) }
    var minutes by remember { mutableIntStateOf(0) } // 0 = sin temporizador
    var secondsLeft by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) { onDispose { player.stop() } }

    // Temporizador para dormir: baja el volumen al final y se apaga solo.
    LaunchedEffect(current, minutes) {
        if (current == null || minutes == 0) {
            secondsLeft = 0
            return@LaunchedEffect
        }
        secondsLeft = minutes * 60
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
            if (secondsLeft in 1..20) player.volume = volume * (secondsLeft / 20f)
        }
        player.stop()
        player.volume = volume
        current = null
    }

    ScreenScaffold("Sonidos relajantes", onBack) {
        SoftText("Los sonidos se generan en tu teléfono: no necesitas Internet ni descargar nada.")
        SoundKind.entries.forEach { k ->
            val active = current == k
            SoftCard(onClick = {
                if (active) {
                    player.stop()
                    current = null
                } else {
                    player.volume = volume
                    player.start(k)
                    current = k
                }
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${k.emoji}  ${k.label}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(if (active) "Sonando · toca para parar" else "Toca para escuchar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        SoftCard {
            Text("Volumen", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = volume,
                onValueChange = {
                    volume = it
                    player.volume = it
                },
                onValueChangeFinished = { Repo.prefs.soundVolume = volume }
            )
        }
        SoftCard {
            Text("Temporizador para dormir", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ChipRow(listOf("Sin límite", "15 min", "30 min", "60 min"), listOf(0, 15, 30, 60).indexOf(minutes)) {
                minutes = listOf(0, 15, 30, 60)[it]
            }
            if (secondsLeft > 0) {
                Spacer(Modifier.height(6.dp))
                SoftText("Se apagará en %d:%02d".format(secondsLeft / 60, secondsLeft % 60))
            }
        }
    }
}
