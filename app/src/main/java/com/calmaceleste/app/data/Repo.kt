package com.calmaceleste.app.data

import android.content.Context
import android.util.Base64
import com.calmaceleste.app.system.Scheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom

/** Lista persistente en un archivo JSON privado de la app (nunca sale del teléfono). */
class ListStore<T>(
    private val file: File,
    private val serializer: KSerializer<List<T>>
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val _items = MutableStateFlow(read())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    /** Vuelve a leer el archivo (se usa después de importar una copia de seguridad). */
    fun reload() { _items.value = read() }

    private fun read(): List<T> = try {
        if (file.exists()) json.decodeFromString(serializer, file.readText()) else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    @Synchronized
    fun update(change: (List<T>) -> List<T>) {
        val next = change(_items.value)
        _items.value = next
        try {
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(json.encodeToString(serializer, next))
            if (!tmp.renameTo(file)) file.writeText(json.encodeToString(serializer, next))
        } catch (e: Exception) {
            // Si falla el disco, al menos queda en memoria durante esta sesión.
        }
    }

    fun clear() = update { emptyList() }
}

/** Preferencias simples (PIN con sal + hash, favoritos, aprendizaje de pasos que ayudan...). */
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("calma_prefs", Context.MODE_PRIVATE)

    var onboarded: Boolean
        get() = sp.getBoolean("onboarded", false)
        set(v) { sp.edit().putBoolean("onboarded", v).apply() }

    var biometric: Boolean
        get() = sp.getBoolean("biometric", false)
        set(v) { sp.edit().putBoolean("biometric", v).apply() }

    var soundVolume: Float
        get() = sp.getFloat("sound_volume", 0.6f)
        set(v) { sp.edit().putFloat("sound_volume", v).apply() }

    // ---- PIN ----
    fun hasPin(): Boolean = sp.contains("pin_hash")

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        sp.edit()
            .putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("pin_hash", hash(salt, pin))
            .apply()
    }

    fun checkPin(pin: String): Boolean {
        val saltB64 = sp.getString("pin_salt", null) ?: return false
        val stored = sp.getString("pin_hash", null) ?: return false
        val calc = hash(Base64.decode(saltB64, Base64.NO_WRAP), pin)
        return MessageDigest.isEqual(calc.toByteArray(), stored.toByteArray())
    }

    fun clearPin() {
        sp.edit().remove("pin_hash").remove("pin_salt").putBoolean("biometric", false).apply()
    }

    private fun hash(salt: ByteArray, pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val out = md.digest(pin.toByteArray(Charsets.UTF_8))
        return out.joinToString("") { "%02x".format(it) }
    }

    // ---- Chistes favoritos ----
    fun favoriteJokes(): Set<String> = sp.getStringSet("fav_jokes", emptySet()) ?: emptySet()

    fun toggleFavorite(id: String) {
        val set = favoriteJokes().toMutableSet()
        if (!set.add(id)) set.remove(id)
        sp.edit().putStringSet("fav_jokes", set).apply()
    }

    // ---- Aprendizaje: qué pasos le ayudaron a la persona ----
    fun helpfulCount(stepId: String): Int = sp.getInt("h_$stepId", 0)

    fun markHelpful(stepId: String) {
        sp.edit().putInt("h_$stepId", helpfulCount(stepId) + 1).apply()
    }

    // ---- Plan de crisis personal ----
    var crisisPlan: List<String>
        get() = sp.getString("crisis_plan", null)
            ?.split("\u0001")?.filter { it.isNotBlank() }
            ?: DEFAULT_PLAN
        set(v) { sp.edit().putString("crisis_plan", v.joinToString("\u0001")).apply() }

    fun wipe() {
        sp.edit().clear().apply()
    }

    /** Preferencias que entran en la copia de seguridad (nunca el PIN ni la huella). */
    private val skip = setOf("pin_hash", "pin_salt", "biometric", "onboarded")

    fun exportJson(): String {
        val o = org.json.JSONObject()
        for ((k, v) in sp.all) {
            if (k in skip || v == null) continue
            val e = org.json.JSONObject()
            when (v) {
                is String -> e.put("t", "s").put("v", v)
                is Int -> e.put("t", "i").put("v", v)
                is Float -> e.put("t", "f").put("v", v.toDouble())
                is Boolean -> e.put("t", "b").put("v", v)
                is Set<*> -> e.put("t", "ss").put("v", org.json.JSONArray(v.map { it.toString() }))
                else -> continue
            }
            o.put(k, e)
        }
        return o.toString()
    }

    fun importJson(text: String) {
        val o = org.json.JSONObject(text)
        val ed = sp.edit()
        for (k in o.keys()) {
            if (k in skip) continue
            val e = o.getJSONObject(k)
            when (e.getString("t")) {
                "s" -> ed.putString(k, e.getString("v"))
                "i" -> ed.putInt(k, e.getInt("v"))
                "f" -> ed.putFloat(k, e.getDouble("v").toFloat())
                "b" -> ed.putBoolean(k, e.getBoolean("v"))
                "ss" -> {
                    val a = e.getJSONArray("v")
                    ed.putStringSet(k, (0 until a.length()).map { a.getString(it) }.toSet())
                }
            }
        }
        ed.apply()
    }

    companion object {
        val DEFAULT_PLAN = listOf(
            "Respirar 4-7-8 tres veces.",
            "Escuchar mi canción favorita.",
            "Llamar a mi persona de confianza.",
            "Recordarme: esto va a pasar."
        )
    }
}

object Repo {
    lateinit var appContext: Context
        private set
    lateinit var prefs: Prefs
        private set
    lateinit var contacts: ListStore<Contact>
        private set
    lateinit var mood: ListStore<MoodEntry>
        private set
    lateinit var thoughts: ListStore<ThoughtRecord>
        private set
    lateinit var reminders: ListStore<Reminder>
        private set
    lateinit var media: ListStore<MediaItem>
        private set
    lateinit var good: ListStore<GoodItem>
        private set
    lateinit var sessions: ListStore<SessionLog>
        private set
    lateinit var love: ListStore<LoveItem>
        private set

    @Volatile
    private var seq = 0L

    fun init(ctx: Context) {
        appContext = ctx.applicationContext
        prefs = Prefs(appContext)
        val dir = appContext.filesDir
        contacts = ListStore(File(dir, "contacts.json"), ListSerializer(Contact.serializer()))
        mood = ListStore(File(dir, "mood.json"), ListSerializer(MoodEntry.serializer()))
        thoughts = ListStore(File(dir, "thoughts.json"), ListSerializer(ThoughtRecord.serializer()))
        reminders = ListStore(File(dir, "reminders.json"), ListSerializer(Reminder.serializer()))
        media = ListStore(File(dir, "media.json"), ListSerializer(MediaItem.serializer()))
        good = ListStore(File(dir, "good.json"), ListSerializer(GoodItem.serializer()))
        sessions = ListStore(File(dir, "sessions.json"), ListSerializer(SessionLog.serializer()))
        love = ListStore(File(dir, "love.json"), ListSerializer(LoveItem.serializer()))
    }

    @Synchronized
    fun newId(): Long {
        seq = (seq + 1) % 1000
        return System.currentTimeMillis() * 1000 + seq
    }

    /** Chistes: se cargan una sola vez desde assets/chistes.txt (formato: categoria|texto##remate). */
    val jokes: List<Joke> by lazy {
        try {
            appContext.assets.open("chistes.txt").bufferedReader(Charsets.UTF_8).readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") && it.contains('|') }
                .mapIndexed { i, line ->
                    val cat = line.substringBefore('|').trim()
                    val body = line.substringAfter('|').trim()
                    val text = body.substringBefore("##").trim()
                    val punch = if (body.contains("##")) body.substringAfter("##").trim() else null
                    Joke(id = "j$i", category = cat, text = text, punchline = punch)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val jokeCategories: List<String> by lazy { jokes.map { it.category }.distinct() }

    /** Borra absolutamente todo lo que la persona guardó. */
    fun wipeAll() {
        reminders.items.value.forEach { Scheduler.cancel(appContext, it) }
        contacts.clear(); mood.clear(); thoughts.clear(); reminders.clear()
        media.clear(); good.clear(); sessions.clear(); love.clear()
        listOf("voice", "videos").forEach { File(appContext.filesDir, it).deleteRecursively() }
        prefs.wipe()
    }
}
