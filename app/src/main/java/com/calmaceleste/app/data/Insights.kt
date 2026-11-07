package com.calmaceleste.app.data

import java.util.Calendar

data class Insight(val emoji: String, val text: String)

val MOOD_EMOJIS = listOf("😞", "😟", "😐", "🙂", "😄")
val MOOD_LABELS = listOf("Muy mal", "Mal", "Regular", "Bien", "Muy bien")

val MOOD_TAGS = listOf(
    "Trabajo", "Estudios", "Familia", "Pareja", "Amigos", "Dinero",
    "Salud", "Sueño", "Cansancio", "Cafeína", "Noticias", "Soledad"
)

object Insights {

    private val dayNames = arrayOf("", "domingos", "lunes", "martes", "miércoles", "jueves", "viernes", "sábados")

    /** Promedio de ánimo de los últimos 7 días (null = sin registro ese día). */
    fun weekChart(entries: List<MoodEntry>): List<Pair<String, Float?>> {
        val labels = arrayOf("D", "L", "M", "X", "J", "V", "S")
        val out = mutableListOf<Pair<String, Float?>>()
        for (back in 6 downTo 0) {
            val c = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -back)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = c.timeInMillis
            val end = start + 24L * 60 * 60 * 1000
            val day = entries.filter { it.time in start until end }
            val avg = if (day.isEmpty()) null else day.map { it.mood }.average().toFloat()
            out += labels[c.get(Calendar.DAY_OF_WEEK) - 1] to avg
        }
        return out
    }

    /** Busca patrones sencillos: días más difíciles y desencadenantes. Todo se calcula en el teléfono. */
    fun compute(entries: List<MoodEntry>): List<Insight> {
        if (entries.size < 6) return emptyList()
        val result = mutableListOf<Insight>()
        val overall = entries.map { it.mood }.average()

        // Día de la semana
        val byDay = entries.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.time }.get(Calendar.DAY_OF_WEEK)
        }.filter { it.value.size >= 2 }
        val worst = byDay.minByOrNull { e -> e.value.map { it.mood }.average() }
        if (worst != null) {
            val avg = worst.value.map { it.mood }.average()
            if (overall - avg >= 0.4) {
                result += Insight("📅", "Tu ánimo suele ser más bajo los ${dayNames[worst.key]}. Puedes prepararte con una pausa de respiración ese día.")
            }
        }
        val best = byDay.maxByOrNull { e -> e.value.map { it.mood }.average() }
        if (best != null) {
            val avg = best.value.map { it.mood }.average()
            if (avg - overall >= 0.4) {
                result += Insight("🌤️", "Los ${dayNames[best.key]} te sientes mejor que el resto de la semana.")
            }
        }

        // Etiquetas (desencadenantes)
        val diffs = MOOD_TAGS.mapNotNull { tag ->
            val with = entries.filter { tag in it.tags }
            val without = entries.filter { tag !in it.tags }
            if (with.size >= 3 && without.size >= 2) {
                tag to (with.map { it.mood }.average() - without.map { it.mood }.average())
            } else null
        }
        diffs.filter { it.second <= -0.4 }.sortedBy { it.second }.take(2).forEach { (tag, d) ->
            result += Insight("⚠️", "Cuando anotas «$tag», tu ánimo baja en promedio ${"%.1f".format(-d)} puntos.")
        }
        diffs.filter { it.second >= 0.4 }.sortedByDescending { it.second }.take(1).forEach { (tag, d) ->
            result += Insight("💙", "Cuando anotas «$tag», tu ánimo sube en promedio ${"%.1f".format(d)} puntos.")
        }
        return result
    }
}
