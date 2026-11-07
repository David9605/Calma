package com.calmaceleste.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.calmaceleste.app.data.GoodItem
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.system.keepReadAccess

@Composable
private fun GoodContent(item: GoodItem) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (item.imageUri != null) {
            AsyncImage(
                model = item.imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(18.dp))
            )
        }
        if (item.text.isNotBlank()) Text(item.text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun GoodBoxScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val items by Repo.good.items.collectAsState()
    var text by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<Uri?>(null) }
    var toDelete by remember { mutableStateOf<GoodItem?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            ctx.keepReadAccess(uri)
            image = uri
        }
    }

    ScreenScaffold("Caja de cosas buenas", onBack) {
        SoftCard {
            Text(
                "Guarda aquí fotos y mensajes que te hagan bien: un abrazo, un logro, algo que te dijeron. " +
                    "La app te los mostrará en los momentos difíciles.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        SoftCard {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Un mensaje o recuerdo bonito") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(8.dp))
            if (image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(18.dp))
                )
                Spacer(Modifier.height(8.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { picker.launch(arrayOf("image/*")) }, modifier = Modifier.weight(1f)) {
                    Text(if (image == null) "Añadir foto" else "Cambiar foto")
                }
                Button(
                    enabled = text.isNotBlank() || image != null,
                    onClick = {
                        Repo.good.update { it + GoodItem(Repo.newId(), text.trim(), image?.toString(), System.currentTimeMillis()) }
                        text = ""
                        image = null
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Guardar") }
            }
        }
        if (items.isEmpty()) SoftText("Tu caja está vacía. Empieza con algo pequeño.")
        items.sortedByDescending { it.time }.forEach { item ->
            SoftCard {
                GoodContent(item)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { toDelete = item }) { Icon(Icons.Filled.Delete, contentDescription = "Borrar") }
                }
            }
        }
    }

    toDelete?.let { item ->
        ConfirmDialog(
            title = "¿Quitar de la caja?",
            text = "Se borra solo de esta app.",
            confirmLabel = "Quitar",
            onConfirm = {
                Repo.good.update { l -> l.filter { it.id != item.id } }
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

/** Para los 10 pasos: una cosa buena al azar. */
@Composable
fun GoodBoxRandomCard() {
    val items by Repo.good.items.collectAsState()
    var current by remember { mutableStateOf(items.randomOrNull()) }
    val item = current
    if (item == null) {
        SoftText("Tu caja está vacía. Cuando guardes algo, aparecerá aquí.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
        GoodContent(item)
        if (items.size > 1) {
            OutlinedButton(
                onClick = { current = items.filter { it.id != item.id }.randomOrNull() ?: item },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Otra") }
        }
    }
}
