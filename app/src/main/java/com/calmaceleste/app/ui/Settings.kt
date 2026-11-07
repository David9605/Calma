package com.calmaceleste.app.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.calmaceleste.app.BuildConfig
import com.calmaceleste.app.data.Repo
import kotlinx.coroutines.delay

private fun canUseBiometric(ctx: android.content.Context): Boolean =
    BiometricManager.from(ctx).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
        BiometricManager.BIOMETRIC_SUCCESS

// ---------------------------------------------------------------------------------------------
// Ajustes y privacidad
// ---------------------------------------------------------------------------------------------

@Composable
fun SettingsScreen(onBack: () -> Unit, onWiped: () -> Unit) {
    val ctx = LocalContext.current
    var hasPin by remember { mutableStateOf(Repo.prefs.hasPin()) }
    var bio by remember { mutableStateOf(Repo.prefs.biometric) }
    var showPinDialog by remember { mutableStateOf(false) }
    var confirmRemovePin by remember { mutableStateOf(false) }
    var confirmWipe by remember { mutableStateOf(false) }
    val bioAvailable = remember { canUseBiometric(ctx) }

    ScreenScaffold("Privacidad y ajustes", onBack) {
        SoftCard {
            Text("🔒 Tus datos son solo tuyos", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "Todo lo que escribes y guardas se queda en este teléfono. La app no tiene cuenta, no envía nada a Internet y no usa tu micrófono ni tu ubicación.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        SoftCard {
            Text("Bloqueo con PIN", style = MaterialTheme.typography.titleMedium)
            SoftText(if (hasPin) "Tu app está protegida con PIN." else "Pon un PIN para que nadie más pueda abrir tu diario.")
            Spacer(Modifier.height(8.dp))
            if (!hasPin) {
                BigButton("Crear PIN") { showPinDialog = true }
            } else {
                BigButton("Cambiar PIN", soft = true) { showPinDialog = true }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { confirmRemovePin = true }, modifier = Modifier.fillMaxWidth()) { Text("Quitar PIN") }
                if (bioAvailable) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Desbloquear con huella o rostro", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = bio, onCheckedChange = { bio = it; Repo.prefs.biometric = it })
                    }
                }
            }
        }

        SoftCard {
            Text("Borrar todos mis datos", style = MaterialTheme.typography.titleMedium)
            SoftText("Elimina diario, contactos, canciones añadidas, recordatorios, recuerdos y todo lo demás. No se puede deshacer.")
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { confirmWipe = true }, modifier = Modifier.fillMaxWidth()) { Text("Borrar todo") }
        }

        SoftCard {
            Text("Importante", style = MaterialTheme.typography.titleMedium)
            SoftText(
                "Calma Celeste es una herramienta de apoyo y bienestar. No es un servicio médico, no diagnostica ni trata ningún trastorno y no sustituye a un profesional de la salud. " +
                    "Si tu ansiedad es frecuente o te limita en tu vida diaria, hablar con un psicólogo o un médico puede ayudarte mucho."
            )
        }

        SoftText("Calma Celeste · versión ${BuildConfig.VERSION_NAME}")
    }

    if (showPinDialog) {
        PinDialog(
            onDismiss = { showPinDialog = false },
            onSave = {
                Repo.prefs.setPin(it)
                hasPin = true
                showPinDialog = false
            }
        )
    }
    if (confirmRemovePin) {
        ConfirmDialog(
            title = "¿Quitar el PIN?",
            text = "Cualquiera que tenga tu teléfono desbloqueado podrá abrir la app.",
            confirmLabel = "Quitar",
            onConfirm = {
                Repo.prefs.clearPin()
                hasPin = false
                bio = false
                confirmRemovePin = false
            },
            onDismiss = { confirmRemovePin = false }
        )
    }
    if (confirmWipe) {
        ConfirmDialog(
            title = "¿Borrar todo?",
            text = "Se eliminarán todos tus datos de este teléfono para siempre.",
            onConfirm = {
                confirmWipe = false
                Repo.wipeAll()
                onWiped()
            },
            onDismiss = { confirmWipe = false }
        )
    }
}

@Composable
private fun PinDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var again by remember { mutableStateOf("") }
    val valid = pin.length in 4..6 && pin == again
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elige un PIN de 4 a 6 números") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    pin, { pin = it.filter(Char::isDigit).take(6) },
                    label = { Text("PIN") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(
                    again, { again = it.filter(Char::isDigit).take(6) },
                    label = { Text("Repite el PIN") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                if (again.isNotEmpty() && pin != again) Text("Los PIN no coinciden", color = MaterialTheme.colorScheme.error)
                Text("Si lo olvidas, tendrás que borrar los datos de la app para volver a entrar.", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onSave(pin) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ---------------------------------------------------------------------------------------------
// Pantalla de bloqueo
// ---------------------------------------------------------------------------------------------

@Composable
fun LockScreen(activity: FragmentActivity, onForgot: () -> Unit, onUnlock: () -> Unit) {
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var fails by remember { mutableIntStateOf(0) }
    var cooldown by remember { mutableIntStateOf(0) }
    var confirmForgot by remember { mutableStateOf(false) }

    fun launchBiometric() {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUnlock()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear Calma Celeste")
            .setNegativeButtonText("Usar PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        try { prompt.authenticate(info) } catch (_: Exception) { }
    }

    LaunchedEffect(Unit) {
        if (Repo.prefs.biometric && canUseBiometric(activity)) launchBiometric()
    }
    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown--
        }
    }

    fun press(d: String) {
        if (cooldown > 0) return
        error = false
        if (entered.length >= 6) return
        entered += d
        // Se comprueba cuando coincide con el PIN guardado (admite de 4 a 6 dígitos).
        if (entered.length >= 4 && Repo.prefs.checkPin(entered)) {
            onUnlock()
        } else if (entered.length == 6) {
            error = true
            entered = ""
            fails++
            if (fails % 5 == 0) cooldown = 30
        }
    }

    CalmBackground {
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("💙", fontSize = 44.sp)
            Spacer(Modifier.height(8.dp))
            Text("Tu espacio privado", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    cooldown > 0 -> "Espera $cooldown s para intentar de nuevo"
                    error -> "PIN incorrecto"
                    else -> "Escribe tu PIN"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { i ->
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < entered.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                    row.forEach { k ->
                        if (k.isEmpty()) {
                            Spacer(Modifier.size(72.dp))
                        } else {
                            Box(
                                Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    .clickable {
                                        if (k == "⌫") { entered = entered.dropLast(1); error = false } else press(k)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(k, fontSize = 26.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (Repo.prefs.biometric && canUseBiometric(activity)) {
                TextButton(onClick = { launchBiometric() }) { Text("Usar huella o rostro") }
            }
            TextButton(onClick = { confirmForgot = true }) { Text("Olvidé mi PIN") }
        }
    }

    if (confirmForgot) {
        ConfirmDialog(
            title = "¿Olvidaste tu PIN?",
            text = "Para proteger tu privacidad, la única forma de entrar es borrar todos los datos de la app. No se puede deshacer.",
            confirmLabel = "Borrar y entrar",
            onConfirm = { confirmForgot = false; onForgot() },
            onDismiss = { confirmForgot = false }
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Bienvenida
// ---------------------------------------------------------------------------------------------

private data class OnboardPage(val emoji: String, val title: String, val text: String)

private val onboardPages = listOf(
    OnboardPage("💙", "Bienvenido a tu rincón de calma", "Un espacio hecho con tus cosas: tu música, tus videos, tus personas y tus propios ejercicios."),
    OnboardPage("🌬️", "Cuando la ansiedad llegue", "Toca el botón grande «Me siento ansioso». Te guiaré con 10 pasos cortos: respirar, distraerte y sentirte acompañado. Si hace falta, te daré más."),
    OnboardPage("🔒", "Privado y sin Internet", "Todo se guarda solo en tu teléfono. No hay cuentas ni anuncios, y todo funciona sin conexión."),
    OnboardPage("🤝", "Una cosa importante", "Calma Celeste es un apoyo, no un tratamiento médico ni un sustituto de un profesional. En una emergencia, usa la sección «Ayuda urgente» o llama a los servicios de tu país.")
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    CalmBackground {
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(250)) },
                label = "onboard"
            ) { p ->
                val d = onboardPages[p]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(d.emoji, fontSize = 72.sp)
                    Spacer(Modifier.height(20.dp))
                    Text(d.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        d.text,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onboardPages.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == page) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == page) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            BigButton(if (page == onboardPages.lastIndex) "Entiendo, empezar" else "Siguiente") {
                if (page == onboardPages.lastIndex) onDone() else page++
            }
        }
    }
}
