package com.calmaceleste.app.data

import kotlin.random.Random

enum class Phase { BODY, DISTRACT, CONNECT }

enum class StepType { BREATH, GROUND, TIP, JOKE, SONG, VIDEO, GAME, MATH, HUM, GOODBOX, SOUND, CONTACT }

data class Step(
    val id: String,
    val type: StepType,
    val phase: Phase,
    val title: String,
    val text: String = "",
    val seconds: Int = 60,
    val arg: String = ""
)

data class BreathPattern(
    val key: String,
    val name: String,
    val desc: String,
    val inhale: Int,
    val hold1: Int,
    val exhale: Int,
    val hold2: Int
)

val BREATH_PATTERNS = listOf(
    BreathPattern("478", "4-7-8", "Inhala 4, sostén 7 y exhala 8. Muy calmante, ideal antes de dormir.", 4, 7, 8, 0),
    BreathPattern("box", "En caja", "Inhala, sostén, exhala y sostén, todo en 4 tiempos.", 4, 4, 4, 4),
    BreathPattern("long", "Exhalación larga", "Inhala 4 y exhala 6. Alargar la salida del aire activa la calma del cuerpo.", 4, 0, 6, 0),
    BreathPattern("coh", "Coherente", "Inhala 5 y exhala 5. Un ritmo suave y constante.", 5, 0, 5, 0),
    BreathPattern("soft", "Suave", "Inhala 3 y exhala 3. Ideal si te cuesta sostener el aire.", 3, 0, 3, 0)
)

fun patternByKey(key: String): BreathPattern =
    BREATH_PATTERNS.firstOrNull { it.key == key } ?: BREATH_PATTERNS[2]

/** Banco de más de 60 pasos. Para añadir más en el futuro, solo agrega líneas aquí. */
object Catalog {

    private fun tip(id: String, phase: Phase, title: String, text: String, seconds: Int = 45) =
        Step(id, StepType.TIP, phase, title, text, seconds)

    val all: List<Step> by lazy {
        buildList {
            // ---------- CUERPO (calmar el cuerpo) ----------
            add(Step("br_478", StepType.BREATH, Phase.BODY, "Respiración 4-7-8", "Sigue el círculo. Inhala por la nariz, exhala despacio por la boca.", 60, "478"))
            add(Step("br_box", StepType.BREATH, Phase.BODY, "Respiración en caja", "Cuatro tiempos para cada parte. Sin prisa.", 60, "box"))
            add(Step("br_long", StepType.BREATH, Phase.BODY, "Exhalación larga", "Que el aire salga más lento de lo que entró.", 60, "long"))
            add(Step("br_coh", StepType.BREATH, Phase.BODY, "Respiración coherente", "Un vaivén suave, como una ola.", 60, "coh"))
            add(Step("br_soft", StepType.BREATH, Phase.BODY, "Respiración suave", "Respira cómodo, sin forzar nada.", 45, "soft"))
            add(Step("ground", StepType.GROUND, Phase.BODY, "Anclaje 5-4-3-2-1", "Vuelve al presente usando tus sentidos.", 90))
            add(Step("hum", StepType.HUM, Phase.BODY, "Tararea para calmarte", "Tararear hace vibrar el pecho y calma el cuerpo.", 60))
            add(Step("snd_rain", StepType.SOUND, Phase.BODY, "Sonido de lluvia", "Cierra los ojos y escucha la lluvia.", 60, "RAIN"))
            add(Step("snd_sea", StepType.SOUND, Phase.BODY, "Sonido del mar", "Deja que las olas lleguen y se vayan.", 60, "SEA"))
            add(Step("snd_wind", StepType.SOUND, Phase.BODY, "Sonido del viento", "Un viento suave que se lleva la tensión.", 60, "WIND"))

            add(tip("b1", Phase.BODY, "Suelta los hombros", "Sube los hombros hacia las orejas, sostén 3 segundos y suéltalos de golpe. Repite 5 veces, despacio."))
            add(tip("b2", Phase.BODY, "Estira el cuello", "Inclina la cabeza hacia un lado y respira hondo. Cambia de lado. Sin forzar."))
            add(tip("b3", Phase.BODY, "Aprieta y suelta", "Cierra los puños con fuerza 5 segundos y ábrelos soltando la tensión. Repite 4 veces."))
            add(tip("b4", Phase.BODY, "Sacude las manos", "Sacude manos y brazos como si te quitaras el agua. Luego déjalos sueltos."))
            add(tip("b5", Phase.BODY, "Un sorbo de agua", "Bebe agua a sorbos pequeños, notando cómo baja. Si no tienes cerca, imagina agua fresca."))
            add(tip("b6", Phase.BODY, "Mira lejos", "Mira por una ventana o hacia el punto más lejano que veas. Deja que tus ojos descansen."))
            add(tip("b7", Phase.BODY, "Manos tibias", "Frota tus manos hasta que estén tibias y apóyalas sobre tu pecho. Respira hondo."))
            add(tip("b8", Phase.BODY, "Camina despacio", "Camina por la habitación sintiendo cada paso: talón, planta, dedos."))
            add(tip("b9", Phase.BODY, "Abraza algo suave", "Abraza un cojín o una manta con fuerza suave. Siente su peso y su calor."))
            add(tip("b10", Phase.BODY, "Relaja la mandíbula", "Separa los dientes, deja la lengua suelta y los labios flojos. Suelta la cara."))
            add(tip("b11", Phase.BODY, "Siente tus pies", "Presiona los pies contra el suelo. Nota su firmeza. Estás sostenido."))
            add(tip("b12", Phase.BODY, "Escanea tu cuerpo", "Recorre mentalmente de la cabeza a los pies y suelta cada zona donde notes tensión.", 60))
            add(tip("b13", Phase.BODY, "Estira hacia el cielo", "Entrelaza las manos, estira los brazos hacia arriba y respira hondo. Baja soltando el aire."))
            add(tip("b14", Phase.BODY, "Masajea tus manos", "Con el pulgar, masajea la palma de la otra mano en círculos suaves. Cambia de mano."))
            add(tip("b15", Phase.BODY, "Aire por la nariz", "Cierra los ojos y nota el aire fresco al entrar y tibio al salir."))
            add(tip("b16", Phase.BODY, "Suelta el peso", "Apoya bien la espalda y deja que el asiento o la cama sostenga todo tu peso."))
            add(tip("b17", Phase.BODY, "Gira los tobillos", "Gira cada tobillo despacio, 5 veces hacia cada lado."))
            add(tip("b18", Phase.BODY, "Ojos cerrados", "Cierra los ojos 30 segundos y solo escucha los sonidos a tu alrededor."))

            // ---------- DISTRACCIÓN (sacar la mente del bucle) ----------
            add(Step("joke_1", StepType.JOKE, Phase.DISTRACT, "Un chiste", "Una sonrisa también cuenta.", 45))
            add(Step("joke_2", StepType.JOKE, Phase.DISTRACT, "Otro chiste", "Un poco de risa para aflojar.", 45))
            add(Step("joke_3", StepType.JOKE, Phase.DISTRACT, "Chiste sorpresa", "A ver si este te saca una sonrisa.", 45))
            add(Step("song", StepType.SONG, Phase.DISTRACT, "Tu canción favorita", "Dale play y déjate llevar.", 90))
            add(Step("video", StepType.VIDEO, Phase.DISTRACT, "Tu video divertido", "Algo que te haga sonreír.", 90))
            add(Step("game_bub", StepType.GAME, Phase.DISTRACT, "Burbujas", "Toca las burbujas y suéltalas.", 60, "bubbles"))
            add(Step("game_zen", StepType.GAME, Phase.DISTRACT, "Jardín zen", "Dibuja con el dedo en la arena.", 60, "zen"))
            add(Step("game_fly", StepType.GAME, Phase.DISTRACT, "Luciérnagas", "Toca la pantalla y mira cómo se acercan.", 60, "fireflies"))
            add(Step("math_1", StepType.MATH, Phase.DISTRACT, "Mates suaves", "Un ejercicio mental sencillo, sin presión.", 60, "1"))
            add(Step("math_2", StepType.MATH, Phase.DISTRACT, "Series de números", "Completa las series con calma.", 60, "2"))

            add(tip("d1", Phase.DISTRACT, "5 cosas celestes", "Busca 5 cosas azules o celestes a tu alrededor y nómbralas en voz baja."))
            add(tip("d2", Phase.DISTRACT, "Cuenta hacia atrás", "Cuenta de 50 hacia atrás, de 3 en 3: 50, 47, 44… Sin prisa."))
            add(tip("d3", Phase.DISTRACT, "Tu lugar feliz", "Imagina un lugar donde te sientas tranquilo. ¿Qué ves, qué oyes, a qué huele?", 60))
            add(tip("d4", Phase.DISTRACT, "Tu comida favorita", "Piensa en tu comida favorita: su olor, su sabor y con quién la compartes."))
            add(tip("d5", Phase.DISTRACT, "Animales de la A a la Z", "Nombra un animal por cada letra del abecedario. ¿Hasta qué letra llegas?", 60))
            add(tip("d6", Phase.DISTRACT, "Describe tu cuarto", "Describe en voz baja tu habitación con todos los detalles que puedas."))
            add(tip("d7", Phase.DISTRACT, "Tres gracias", "Piensa en 3 cosas pequeñas por las que estás agradecido hoy."))
            add(tip("d8", Phase.DISTRACT, "Dibuja en el aire", "Con el dedo, dibuja en el aire tu nombre y luego una casa, un sol, una flor."))
            add(tip("d9", Phase.DISTRACT, "Tu película favorita", "Recuerda tu escena favorita de una película o serie y cuéntatela con detalle."))
            add(tip("d10", Phase.DISTRACT, "Luz celeste", "Imagina una luz celeste suave que te envuelve y se lleva la tensión.", 60))
            add(tip("d11", Phase.DISTRACT, "Cinco ciudades", "Nombra 5 lugares del mundo que te gustaría visitar y qué harías en cada uno.", 60))
            add(tip("d12", Phase.DISTRACT, "Nombre para una mascota", "Inventa nombres graciosos para una mascota imaginaria. ¡Cuanto más raros, mejor!"))
            add(tip("d13", Phase.DISTRACT, "Palabras con M", "Piensa en 10 palabras que empiecen con la letra M."))
            add(tip("d14", Phase.DISTRACT, "Canción en tu mente", "Recorre mentalmente tu canción favorita desde el principio, verso a verso."))
            add(tip("d15", Phase.DISTRACT, "Colores del arcoíris", "Nombra algo de cada color del arcoíris: rojo, naranja, amarillo, verde, azul, violeta."))

            // ---------- CONEXIÓN (volver a sentirte acompañado) ----------
            add(Step("contact", StepType.CONTACT, Phase.CONNECT, "Habla con alguien", "Una voz amiga puede ayudar mucho.", 60))
            add(Step("goodbox", StepType.GOODBOX, Phase.CONNECT, "Tu caja de cosas buenas", "Mira algo que guardaste para este momento.", 60))
            add(tip("c1", Phase.CONNECT, "Alguien que te da seguridad", "Piensa en una persona que te hace sentir seguro. Imagina que está contigo ahora."))
            add(tip("c2", Phase.CONNECT, "Mensaje para ti", "Dite lo que le dirías a un buen amigo: «Estás haciendo lo mejor que puedes»."))
            add(tip("c3", Phase.CONNECT, "Esto va a pasar", "Repite despacio: «Esto es incómodo, pero va a pasar». Respira entre cada repetición."))
            add(tip("c4", Phase.CONNECT, "Sonríe suave", "Deja que las comisuras de tus labios suban un poquito. Sin forzar. Nota cómo se siente."))
            add(tip("c5", Phase.CONNECT, "Algo bonito para mañana", "Piensa en una cosa pequeña y agradable que harás mañana."))
            add(tip("c6", Phase.CONNECT, "Un buen recuerdo", "Recuerda un momento feliz con alguien querido y quédate con esa imagen.", 60))
        }
    }
}

/** Arma las rondas de 10 pasos: primero cuerpo, luego distracción, al final conexión. */
object SessionEngine {

    private const val BODY_COUNT = 3
    private const val DISTRACT_COUNT = 5
    private const val CONNECT_COUNT = 2

    fun buildRound(exclude: Set<String>): List<Step> {
        val hasSongs = Repo.media.items.value.any { !it.isVideo }
        val hasVideos = Repo.media.items.value.any { it.isVideo }
        val hasGood = Repo.good.items.value.isNotEmpty()
        val hasContacts = Repo.contacts.items.value.isNotEmpty()

        val available = Catalog.all.filter {
            when (it.type) {
                StepType.SONG -> hasSongs
                StepType.VIDEO -> hasVideos
                StepType.GOODBOX -> hasGood
                else -> true
            }
        }

        fun weight(step: Step, already: List<Step>): Double {
            var w = 1.0 + Repo.prefs.helpfulCount(step.id) * 2.0
            if (step.type == StepType.CONTACT && hasContacts) w *= 3.0
            // Variedad: si ya hay pasos del mismo tipo, es menos probable repetir.
            w /= (1 + already.count { it.type == step.type })
            return w
        }

        fun pick(phase: Phase, n: Int): List<Step> {
            val chosen = mutableListOf<Step>()
            val fresh = available.filter { it.phase == phase && it.id !in exclude }.toMutableList()
            takeWeighted(fresh, n, chosen, ::weight)
            if (chosen.size < n) {
                val rest = available.filter { it.phase == phase && it !in chosen }.toMutableList()
                takeWeighted(rest, n - chosen.size, chosen, ::weight)
            }
            return chosen
        }

        val body = pick(Phase.BODY, BODY_COUNT)
        val distract = pick(Phase.DISTRACT, DISTRACT_COUNT).shuffled()
        val connect = pick(Phase.CONNECT, CONNECT_COUNT)
        return body + distract + connect
    }

    private fun takeWeighted(
        pool: MutableList<Step>,
        n: Int,
        out: MutableList<Step>,
        weight: (Step, List<Step>) -> Double
    ) {
        repeat(n) {
            if (pool.isEmpty()) return
            val ws = pool.map { weight(it, out) }
            var r = Random.nextDouble() * ws.sum()
            var idx = ws.lastIndex
            for (i in ws.indices) {
                r -= ws[i]
                if (r <= 0) { idx = i; break }
            }
            out.add(pool.removeAt(idx))
        }
    }
}
