package com.calmaceleste.app.data

data class HelpLine(
    val countryCode: String,
    val country: String,
    val label: String,
    val number: String
)

/**
 * Números de ayuda. IMPORTANTE: los números cambian con el tiempo. Revísalos en cada versión
 * (ver docs/ACTUALIZAR.md) y la pantalla de ayuda ya avisa a la persona de que los verifique.
 */
object HelpData {
    val lines = listOf(
        HelpLine("VE", "Venezuela", "Emergencias", "171"),
        HelpLine("MX", "México", "Emergencias", "911"),
        HelpLine("MX", "México", "Línea de la Vida (salud mental)", "8009112000"),
        HelpLine("CO", "Colombia", "Emergencias", "123"),
        HelpLine("CO", "Colombia", "Línea 106 (salud mental, Bogotá)", "106"),
        HelpLine("AR", "Argentina", "Emergencias", "911"),
        HelpLine("AR", "Argentina", "Prevención del suicidio (Buenos Aires)", "135"),
        HelpLine("CL", "Chile", "Salud Responde", "6003607777"),
        HelpLine("CL", "Chile", "Prevención del suicidio", "*4141"),
        HelpLine("ES", "España", "Emergencias", "112"),
        HelpLine("ES", "España", "Línea de atención a la conducta suicida", "024"),
        HelpLine("UY", "Uruguay", "Emergencias", "911"),
        HelpLine("UY", "Uruguay", "Prevención del suicidio", "08000767"),
        HelpLine("EC", "Ecuador", "Emergencias", "911"),
        HelpLine("US", "Estados Unidos", "Emergencias", "911"),
        HelpLine("US", "Estados Unidos", "Línea 988 de crisis y suicidio (hay opción en español)", "988")
    )
}
