package com.calmaceleste.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmaceleste.app.data.HelpData
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.dial

// ---------------------------------------------------------------------------------------------
// Mi plan de crisis personal
// ---------------------------------------------------------------------------------------------

@Composable
fun CrisisPlanScreen(onBack: () -> Unit) {
    val steps = remember { mutableStateListOf<String>().apply { addAll(Repo.prefs.crisisPlan) } }
    var saved by remember { mutableStateOf(false) }
    val contacts by Repo.contacts.items.collectAsState()

    ScreenScaffold("Mi plan de crisis", onBack) {
        SoftCard {
            Text(
                "Escríbelo ahora, con calma. Cuando te sientas mal, no tendrás que pensar: solo seguir tus propios pasos.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        steps.forEachIndexed { i, s ->
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.width(32.dp))
                    OutlinedTextField(
                        value = s,
                        onValueChange = { steps[i] = it; saved = false },
                        modifier = Modifier.weight(1f),
                        minLines = 1
                    )
                    IconButton(onClick = { steps.removeAt(i); saved = false }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar paso")
                    }
                }
            }
        }
        if (steps.size < 8) {
            OutlinedButton(onClick = { steps.add(""); saved = false }, modifier = Modifier.fillMaxWidth()) { Text("Añadir un paso") }
        }
        BigButton(if (saved) "Guardado ✓" else "Guardar mi plan", enabled = !saved) {
            Repo.prefs.crisisPlan = steps.map { it.trim() }.filter { it.isNotEmpty() }
            saved = true
        }
        if (contacts.isNotEmpty()) {
            SectionTitle("Mi gente")
            ContactsQuick(onAdd = {})
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Ayuda urgente
// ---------------------------------------------------------------------------------------------

@Composable
fun HelpScreen(onBack: () -> Unit, go: (String) -> Unit) {
    val ctx = LocalContext.current
    val countries = remember { HelpData.lines.map { it.country }.distinct() }
    var sel by remember { mutableIntStateOf(0) }
    val country = countries[sel]

    ScreenScaffold("Ayuda urgente", onBack) {
        SoftCard(tint = Color(0xFFFFE6E3)) {
            Text(
                "Si estás en peligro o piensas en hacerte daño, llama ahora a emergencias o ve al lugar más cercano donde puedan ayudarte. No tienes que pasar por esto solo.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5A1F1B),
                fontWeight = FontWeight.Medium
            )
        }
        SectionTitle("Elige tu país")
        ChipRow(countries, sel) { sel = it }
        HelpData.lines.filter { it.country == country }.forEach { l ->
            SoftCard {
                Text(l.label, style = MaterialTheme.typography.titleMedium)
                SoftText(l.number)
                Spacer(Modifier.height(8.dp))
                BigButton("Llamar al ${l.number}") { ctx.dial(l.number) }
            }
        }
        SoftCard {
            Text("Importante", style = MaterialTheme.typography.titleMedium)
            SoftText(
                "Los números pueden cambiar con el tiempo, así que verifícalos. Si tu país no aparece, busca el número local de emergencias en tu teléfono o pregunta a alguien cercano."
            )
        }
        BigButton("Llamar a mi persona de confianza", soft = true) { go(Routes.CONTACTS) }
    }
}
