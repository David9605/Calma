package com.calmaceleste.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val message: String
)

@Serializable
data class MoodEntry(
    val id: Long,
    val time: Long,
    val mood: Int, // 1 (muy mal) .. 5 (muy bien)
    val note: String = "",
    val tags: List<String> = emptyList()
)

@Serializable
data class ThoughtRecord(
    val id: Long,
    val time: Long,
    val situation: String,
    val thought: String,
    val evidenceFor: String,
    val evidenceAgainst: String,
    val alternative: String,
    val before: Int,
    val after: Int
)

@Serializable
data class Reminder(
    val id: Long,
    val title: String,
    val hour: Int,
    val minute: Int,
    val days: List<Int> = emptyList(), // Calendar.DAY_OF_WEEK (1=domingo..7=sábado). Vacío = todos los días
    val enabled: Boolean = true
) {
    val code: Int get() = (id and 0x3fffffffL).toInt()
}

@Serializable
data class MediaItem(
    val id: Long,
    val title: String,
    val uri: String,
    val isVideo: Boolean
) {
    val isLink: Boolean get() = uri.startsWith("http")
}

@Serializable
data class GoodItem(
    val id: Long,
    val text: String,
    val imageUri: String? = null,
    val time: Long
)

@Serializable
data class SessionLog(
    val id: Long,
    val time: Long,
    val before: Int,
    val after: Int,
    val rounds: Int
)

data class Joke(
    val id: String,
    val category: String,
    val text: String,
    val punchline: String?
)
