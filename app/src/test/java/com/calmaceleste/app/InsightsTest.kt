package com.calmaceleste.app

import com.calmaceleste.app.data.Insights
import com.calmaceleste.app.data.MoodEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsTest {

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun noInsightsWithFewEntries() {
        assertTrue(Insights.compute(emptyList()).isEmpty())
    }

    @Test
    fun weekChartHasSevenDays() {
        assertEquals(7, Insights.weekChart(emptyList()).size)
    }

    @Test
    fun weekChartAveragesTodaysMood() {
        val now = System.currentTimeMillis()
        val chart = Insights.weekChart(
            listOf(MoodEntry(1, now, 2), MoodEntry(2, now, 4))
        )
        assertEquals(3f, chart.last().second ?: -1f, 0.001f)
    }

    @Test
    fun lowMoodTagIsReported() {
        val now = System.currentTimeMillis()
        val entries = (0 until 10).map { i ->
            MoodEntry(
                id = i.toLong(),
                time = now - i * day,
                mood = if (i % 2 == 0) 1 else 4,
                tags = if (i % 2 == 0) listOf("Trabajo") else emptyList()
            )
        }
        val result = Insights.compute(entries)
        assertTrue("No se detectó el desencadenante", result.any { it.text.contains("Trabajo") })
    }
}
