package com.calmaceleste.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.data.ThoughtRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fmt = SimpleDateFormat("d MMM yyyy", Locale("es"))

/** Registro de pensamientos (basado en ideas de la terapia cognitivo-conductual). No sustituye a un terapeuta. */
@Composable
fun ThoughtsScreen(onBack: () -> Unit) {
    val records by Repo.thoughts.items.collectAsState()
    var situation by remember { mutableStateOf("") }
    var thought by remember { mutableStateOf("") }
    var forText by remember { mutableStateOf("") }
    var againstText by remember { mutableStateOf("") }
    var alternative by remember { mutableStateOf("") }
    var before by remember { mutableIntStateOf(6) }
    var after by remember { mutableIntStateOf(4) }

    ScreenScaffold("Registro de pensamientos", onBack) {
        SoftText("Cuando un pensamiento te angustia, escribirlo paso a paso ayuda a verlo con más distancia. Responde con calma, sin juzgarte.")
        SoftCard {
            Field("1. ¿Qué pasó? ¿Dónde estabas?", situation) { situation = it }
            Field("2. ¿Qué pensamiento apareció?", thought) { thought = it }
            Text("¿Cuánta ansiedad sentías? $before/10", style = MaterialTheme.typography.bodyMedium)
            ScoreSlider(before) { before = it }
            Field("3. ¿Qué hechos apoyan ese pensamiento?", forText) { forText = it }
            Field("4. ¿Qué hechos van en su contra?", againstText) { againstText = it }
            Field("5. ¿Qué otra forma de verlo hay? ¿Qué le dirías a un amigo?", alternative) { alternative = it }
            Text("¿Cuánta ansiedad sientes ahora? $after/10", style = MaterialTheme.typography.bodyMedium)
            ScoreSlider(after) { after = it }
            Spacer(Modifier.height(8.dp))
            BigButton("Guardar registro", enabled = thought.isNotBlank()) {
                Repo.thoughts.update {
                    it + ThoughtRecord(
                        Repo.newId(), System.currentTimeMillis(), situation.trim(), thought.trim(),
                        forText.trim(), againstText.trim(), alternative.trim(), before, after
                    )
                }
                situation = ""; thought = ""; forText = ""; againstText = ""; alternative = ""
                before = 6; after = 4
            }
        }
        if (records.isNotEmpty()) {
            SectionTitle("Mis registros")
            records.sortedByDescending { it.time }.forEach { r ->
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SoftText("${fmt.format(Date(r.time))} · ansiedad ${r.before} → ${r.after}")
                            Text("«${r.thought}»", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { Repo.thoughts.update { l -> l.filter { it.id != r.id } } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar registro")
                        }
                    }
                    if (r.alternative.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Otra mirada: ${r.alternative}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        minLines = 2
    )
}
