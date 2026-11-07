package com.calmaceleste.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.calmaceleste.app.data.Repo
import com.calmaceleste.app.ui.AppNav
import com.calmaceleste.app.ui.LockScreen
import com.calmaceleste.app.ui.OnboardingScreen
import com.calmaceleste.app.ui.theme.CalmaTheme

// FragmentActivity (en vez de ComponentActivity) para poder usar la huella digital.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CalmaTheme {
                AppRoot(this)
            }
        }
    }
}

@Composable
private fun AppRoot(activity: FragmentActivity) {
    var onboarded by remember { mutableStateOf(Repo.prefs.onboarded) }
    var unlocked by remember { mutableStateOf(!Repo.prefs.hasPin()) }

    // Si hay PIN, se vuelve a pedir cuando la app estuvo más de 30 segundos en segundo plano.
    DisposableEffect(activity) {
        var stoppedAt = 0L
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                stoppedAt = System.currentTimeMillis()
            } else if (event == Lifecycle.Event.ON_START) {
                val away = System.currentTimeMillis() - stoppedAt
                if (stoppedAt > 0L && Repo.prefs.hasPin() && away > 30_000L) {
                    unlocked = false
                }
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    val stage = when {
        !onboarded -> 0
        !unlocked -> 1
        else -> 2
    }

    Crossfade(targetState = stage, animationSpec = tween(500), label = "root") { s ->
        when (s) {
            0 -> OnboardingScreen {
                Repo.prefs.onboarded = true
                onboarded = true
            }
            1 -> LockScreen(
                activity = activity,
                onForgot = {
                    Repo.wipeAll()
                    onboarded = false
                    unlocked = true
                },
                onUnlock = { unlocked = true }
            )
            else -> AppNav(onWiped = {
                onboarded = false
                unlocked = true
            })
        }
    }
}
