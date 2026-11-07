package com.calmaceleste.app.system

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.calmaceleste.app.data.MediaItem
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

enum class SoundKind(val label: String, val emoji: String) {
    RAIN("Lluvia", "🌧️"),
    SEA("Mar", "🌊"),
    WIND("Viento", "🍃"),
    WHITE("Ruido blanco", "☁️"),
    BROWN("Ruido profundo", "🌙")
}

/**
 * Genera sonidos relajantes directamente en el teléfono (sin archivos ni Internet).
 * Más adelante puedes añadir grabaciones reales en res/raw y mezclarlas aquí.
 */
class SoundPlayer {
    @Volatile private var generation = 0
    @Volatile var volume: Float = 0.6f
    var isPlaying by mutableStateOf(false)
        private set

    fun start(kind: SoundKind) {
        generation++ // el hilo anterior ve el cambio y se apaga solo
        val mine = generation
        isPlaying = true
        Thread { generate(kind, mine) }.start()
    }

    fun stop() {
        generation++
        isPlaying = false
    }

    private fun generate(kind: SoundKind, mine: Int) {
        val sampleRate = 22050
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuf, 8192))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            isPlaying = false
            return
        }

        val rnd = Random()
        val buf = ShortArray(2048)
        var lp1 = 0f
        var lp2 = 0f
        var brown = 0f
        var drop = 0f
        var t = 0.0
        val dt = 1.0 / sampleRate
        try {
            track.play()
            while (generation == mine) {
                for (i in buf.indices) {
                    val white = rnd.nextFloat() * 2f - 1f
                    val s: Float = when (kind) {
                        SoundKind.WHITE -> white * 0.35f
                        SoundKind.BROWN -> {
                            brown = (brown + 0.02f * white) / 1.02f
                            brown * 6f
                        }
                        SoundKind.RAIN -> {
                            lp1 += 0.5f * (white - lp1)
                            if (rnd.nextInt(2500) == 0) drop = 0.6f
                            drop *= 0.9985f
                            lp1 * 0.5f + drop * white * 0.6f
                        }
                        SoundKind.SEA -> {
                            brown = (brown + 0.02f * white) / 1.02f
                            lp1 += 0.1f * (white - lp1)
                            val wave = 0.55f + 0.45f * sin(2.0 * PI * 0.09 * t).toFloat()
                            brown * 5f * wave + lp1 * 0.25f * wave * wave
                        }
                        SoundKind.WIND -> {
                            lp1 += 0.04f * (white - lp1)
                            lp2 += 0.008f * (white - lp2)
                            val lfo = 0.5f + 0.5f * sin(2.0 * PI * 0.07 * t + 1.3).toFloat()
                            (lp1 - lp2) * 4f * (0.3f + 0.7f * lfo) * 1.2f
                        }
                    }
                    val v = (s.coerceIn(-1f, 1f) * volume * 30000f).toInt()
                    buf[i] = v.toShort()
                    t += dt
                }
                track.write(buf, 0, buf.size)
            }
        } catch (e: Exception) {
            // ignorar
        } finally {
            try { track.stop() } catch (_: Exception) {}
            track.release()
        }
    }
}

/** Reproductor de las canciones que la persona eligió de su teléfono. */
class SongController(private val ctx: Context) {
    private var mp: MediaPlayer? = null
    var currentId by mutableStateOf<Long?>(null)
        private set
    var playing by mutableStateOf(false)
        private set

    fun play(item: MediaItem, onEnd: () -> Unit = {}) {
        stop()
        try {
            val p = MediaPlayer()
            mp = p
            p.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            p.setDataSource(ctx, Uri.parse(item.uri))
            p.setOnPreparedListener { it.start(); playing = true }
            p.setOnCompletionListener { playing = false; onEnd() }
            p.setOnErrorListener { _, _, _ -> playing = false; true }
            currentId = item.id
            p.prepareAsync()
        } catch (e: Exception) {
            stop()
        }
    }

    fun toggle() {
        val p = mp ?: return
        try {
            if (p.isPlaying) { p.pause(); playing = false } else { p.start(); playing = true }
        } catch (_: Exception) {
        }
    }

    fun stop() {
        try { mp?.release() } catch (_: Exception) {}
        mp = null
        playing = false
        currentId = null
    }
}
