package com.calmaceleste.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calmaceleste.app.data.Contact
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.dial
import com.calmaceleste.app.system.sms
import com.calmaceleste.app.system.whatsapp

const val DEFAULT_CONTACT_MESSAGE =
    "Hola, me siento con ansiedad y me vendría bien hablar. ¿Puedes llamarme o escribirme cuando puedas?"

@Composable
fun ContactsScreen(onBack: () -> Unit) {
    val contacts by Repo.contacts.items.collectAsState()
    var editing by remember { mutableStateOf<Contact?>(null) }
    var adding by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Contact?>(null) }

    ScreenScaffold("Mi gente de confianza", onBack) {
        SoftCard {
            Text(
                "Las personas a las que puedes acudir. Con un toque llamas o envías un mensaje ya escrito.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        BigButton("Añadir persona") { adding = true }
        if (contacts.isEmpty()) SoftText("Todavía no has añadido a nadie. Piensa en alguien que te haga sentir seguro.")
        contacts.forEach { c ->
            ContactCard(c, onEdit = { editing = c }, onDelete = { toDelete = c })
        }
        SoftText("La app no lee tu agenda: solo guarda lo que tú escribes aquí, en tu teléfono.")
    }

    if (adding || editing != null) {
        ContactDialog(
            initial = editing,
            onSave = { name, phone, msg ->
                val e = editing
                if (e == null) {
                    Repo.contacts.update { it + Contact(Repo.newId(), name, phone, msg) }
                } else {
                    Repo.contacts.update { l -> l.map { if (it.id == e.id) it.copy(name = name, phone = phone, message = msg) else it } }
                }
                adding = false
                editing = null
            },
            onDismiss = { adding = false; editing = null }
        )
    }
    toDelete?.let { c ->
        ConfirmDialog(
            title = "¿Quitar a ${c.name}?",
            text = "Se borra solo de esta app.",
            confirmLabel = "Quitar",
            onConfirm = {
                Repo.contacts.update { l -> l.filter { it.id != c.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun ContactCard(c: Contact, onEdit: () -> Unit, onDelete: () -> Unit) {
    val ctx = LocalContext.current
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SoftText(c.phone)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Editar") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Quitar") }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = { ctx.dial(c.phone) }, modifier = Modifier.weight(1f)) { Text("Llamar") }
            FilledTonalButton(onClick = { ctx.sms(c.phone, c.message) }, modifier = Modifier.weight(1f)) { Text("SMS") }
            FilledTonalButton(onClick = { ctx.whatsapp(c.phone, c.message) }, modifier = Modifier.weight(1f)) { Text("WhatsApp") }
        }
    }
}

@Composable
private fun ContactDialog(initial: Contact?, onSave: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var msg by remember { mutableStateOf(initial?.message ?: DEFAULT_CONTACT_MESSAGE) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Añadir persona" else "Editar persona") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre (ej. Mamá)") }, singleLine = true)
                OutlinedTextField(
                    phone, { phone = it },
                    label = { Text("Teléfono (con código de país)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(msg, { msg = it }, label = { Text("Mensaje listo para enviar") }, minLines = 3)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && phone.filter { it.isDigit() }.length >= 3,
                onClick = { onSave(name.trim(), phone.trim(), msg.ifBlank { DEFAULT_CONTACT_MESSAGE }) }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Versión compacta para los 10 pasos. */
@Composable
fun ContactsQuick(onAdd: () -> Unit) {
    val ctx = LocalContext.current
    val contacts by Repo.contacts.items.collectAsState()
    if (contacts.isEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SoftText("Aún no tienes a nadie guardado. Añade a alguien de confianza para tenerlo a un toque.")
            OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Añadir persona") }
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        contacts.take(3).forEach { c ->
            Text(c.name, style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { ctx.dial(c.phone) }, modifier = Modifier.weight(1f)) { Text("Llamar") }
                FilledTonalButton(onClick = { ctx.sms(c.phone, c.message) }, modifier = Modifier.weight(1f)) { Text("Mensaje") }
            }
        }
    }
}
