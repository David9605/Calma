package com.calmaceleste.app

import com.calmaceleste.app.data.BREATH_PATTERNS
import com.calmaceleste.app.data.Catalog
import com.calmaceleste.app.data.Phase
import com.calmaceleste.app.data.StepType
import com.calmaceleste.app.system.SoundKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Pruebas rápidas que se ejecutan en GitHub antes de cada compilación. Protegen el contenido de la app. */
class CatalogTest {

    @Test
    fun bankHasAtLeast60Steps() {
        assertTrue("El banco debe tener 60 pasos o más, tiene ${Catalog.all.size}", Catalog.all.size >= 60)
    }

    @Test
    fun stepIdsAreUnique() {
        val ids = Catalog.all.map { it.id }
        assertEquals("Hay ids de pasos repetidos", ids.size, ids.toSet().size)
    }

    @Test
    fun everyPhaseHasEnoughSteps() {
        Phase.entries.forEach { phase ->
            val n = Catalog.all.count { it.phase == phase }
            assertTrue("La fase $phase tiene solo $n pasos", n >= 8)
        }
    }

    @Test
    fun breathingStepsUseKnownPatterns() {
        Catalog.all.filter { it.type == StepType.BREATH }.forEach { step ->
            assertTrue("Patrón desconocido: ${step.arg}", BREATH_PATTERNS.any { it.key == step.arg })
        }
    }

    @Test
    fun soundStepsUseKnownSounds() {
        Catalog.all.filter { it.type == StepType.SOUND }.forEach { step ->
            SoundKind.valueOf(step.arg) // lanza error si el nombre no existe
        }
    }

    @Test
    fun jokesFileHasAtLeast300Jokes() {
        val file = File("src/main/assets/chistes.txt")
        assertTrue("No se encontró ${file.absolutePath}", file.exists())
        val jokes = file.readLines(Charsets.UTF_8).filter { it.isNotBlank() && !it.startsWith("#") }
        assertTrue("Debe haber 300 chistes o más, hay ${jokes.size}", jokes.size >= 300)
        jokes.forEach { line ->
            assertTrue("Línea sin categoría: $line", line.contains('|'))
            assertEquals("Hay más de un | en: $line", 1, line.count { it == '|' })
        }
    }
}
