package com.calmaceleste.app.data

import java.util.Calendar

/** Una promesa de la Biblia. Texto adaptado de la Reina-Valera (dominio público). */
data class Promise(val ref: String, val text: String, val category: Int)

object Promises {
    const val PROTECTION = 0
    const val HEALING = 1
    const val NEVER_LEAVES = 2

    val categories = listOf("Protección", "Sanidad y paz", "Dios nunca me deja")

    val all: List<Promise> = listOf(
        // ---- Protección ----
        Promise("Salmos 91:1-2", "El que habita al abrigo del Altísimo morará bajo la sombra del Omnipotente. Diré yo a Jehová: Esperanza mía, y castillo mío; mi Dios, en quien confiaré.", PROTECTION),
        Promise("Salmos 91:11", "A sus ángeles mandará acerca de ti, que te guarden en todos tus caminos.", PROTECTION),
        Promise("Salmos 121:7-8", "Jehová te guardará de todo mal; él guardará tu alma. Jehová guardará tu salida y tu entrada desde ahora y para siempre.", PROTECTION),
        Promise("Salmos 46:1", "Dios es nuestro amparo y fortaleza, nuestro pronto auxilio en las tribulaciones.", PROTECTION),
        Promise("Isaías 41:10", "No temas, porque yo estoy contigo; no desmayes, porque yo soy tu Dios que te esfuerzo; siempre te ayudaré, siempre te sustentaré con la diestra de mi justicia.", PROTECTION),
        Promise("Isaías 43:2", "Cuando pasares por las aguas, yo seré contigo; y por los ríos, no te anegarán. Cuando pasares por el fuego, no te quemarás, ni la llama arderá en ti.", PROTECTION),
        Promise("Salmos 27:1", "Jehová es mi luz y mi salvación; ¿de quién temeré? Jehová es la fortaleza de mi vida; ¿de quién he de atemorizarme?", PROTECTION),
        Promise("Salmos 56:3", "En el día que temo, yo en ti confío.", PROTECTION),
        Promise("Salmos 4:8", "En paz me acostaré, y asimismo dormiré; porque solo tú, Jehová, me haces vivir confiado.", PROTECTION),
        Promise("Proverbios 18:10", "Torre fuerte es el nombre de Jehová; a él correrá el justo, y será levantado.", PROTECTION),
        // ---- Sanidad y paz ----
        Promise("Salmos 147:3", "Él sana a los quebrantados de corazón, y venda sus heridas.", HEALING),
        Promise("Jeremías 30:17", "Yo te restauraré la salud, y sanaré tus heridas, dice Jehová.", HEALING),
        Promise("Éxodo 15:26", "Yo soy Jehová, tu sanador.", HEALING),
        Promise("Salmos 103:2-3", "Bendice, alma mía, a Jehová, y no olvides ninguno de sus beneficios. Él es quien perdona todas tus iniquidades, el que sana todas tus dolencias.", HEALING),
        Promise("Salmos 34:18", "Cercano está Jehová a los quebrantados de corazón; y salva a los contritos de espíritu.", HEALING),
        Promise("Filipenses 4:6-7", "Por nada estéis afanosos, sino sean conocidas vuestras peticiones delante de Dios en toda oración y ruego, con acción de gracias. Y la paz de Dios, que sobrepasa todo entendimiento, guardará vuestros corazones y vuestros pensamientos en Cristo Jesús.", HEALING),
        Promise("1 Pedro 5:7", "Echad toda vuestra ansiedad sobre él, porque él tiene cuidado de vosotros.", HEALING),
        Promise("Mateo 11:28", "Venid a mí todos los que estáis trabajados y cargados, y yo os haré descansar.", HEALING),
        Promise("Juan 14:27", "La paz os dejo, mi paz os doy; yo no os la doy como el mundo la da. No se turbe vuestro corazón, ni tenga miedo.", HEALING),
        Promise("Salmos 94:19", "En la multitud de mis pensamientos dentro de mí, tus consolaciones alegraban mi alma.", HEALING),
        Promise("Salmos 55:22", "Echa sobre Jehová tu carga, y él te sustentará; no dejará para siempre caído al justo.", HEALING),
        Promise("Isaías 26:3", "Tú guardarás en completa paz a aquel cuyo pensamiento en ti persevera; porque en ti ha confiado.", HEALING),
        // ---- Dios nunca me deja ----
        Promise("Deuteronomio 31:6", "Esforzaos y cobrad ánimo; no temáis, ni tengáis miedo, porque Jehová tu Dios es el que va contigo; no te dejará, ni te desamparará.", NEVER_LEAVES),
        Promise("Josué 1:9", "Mira que te mando que te esfuerces y seas valiente; no temas ni desmayes, porque Jehová tu Dios estará contigo en dondequiera que vayas.", NEVER_LEAVES),
        Promise("Hebreos 13:5", "No te desampararé, ni te dejaré.", NEVER_LEAVES),
        Promise("Mateo 28:20", "Yo estoy con vosotros todos los días, hasta el fin del mundo.", NEVER_LEAVES),
        Promise("Salmos 23:4", "Aunque ande en valle de sombra de muerte, no temeré mal alguno, porque tú estarás conmigo; tu vara y tu callado me infundirán aliento.", NEVER_LEAVES),
        Promise("Romanos 8:38-39", "Estoy seguro de que ni la muerte, ni la vida, ni lo presente, ni lo por venir, ni lo alto, ni lo profundo, ni ninguna otra cosa creada nos podrá separar del amor de Dios, que es en Cristo Jesús Señor nuestro.", NEVER_LEAVES),
        Promise("Salmos 139:7-8", "¿A dónde me iré de tu Espíritu? ¿Y a dónde huiré de tu presencia? Si subiere a los cielos, allí estás tú; y si en el abismo hiciere mi cama, he aquí, allí tú estás.", NEVER_LEAVES),
        Promise("Isaías 49:15", "Aunque la madre se olvide de su hijo, yo nunca me olvidaré de ti.", NEVER_LEAVES)
    )

    /** Una promesa distinta cada día. */
    fun today(): Promise {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return all[day % all.size]
    }
}
