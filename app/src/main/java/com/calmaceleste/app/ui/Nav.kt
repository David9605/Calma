package com.calmaceleste.app.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val HOME = "home"
    const val TOOLS = "tools"
    const val SESSION = "session"
    const val BREATHING = "breathing"
    const val GROUNDING = "grounding"
    const val SONGS = "songs"
    const val VIDEOS = "videos"
    const val CONTACTS = "contacts"
    const val REMINDERS = "reminders"
    const val DIARY = "diary"
    const val GOODBOX = "goodbox"
    const val SOUNDS = "sounds"
    const val THOUGHTS = "thoughts"
    const val CRISIS = "crisis"
    const val HELP = "help"
    const val GAMES = "games"
    const val BUBBLES = "game_bubbles"
    const val ZEN = "game_zen"
    const val FIREFLIES = "game_fireflies"
    const val MATH = "math"
    const val HUM = "hum"
    const val JOKES = "jokes"
    const val SETTINGS = "settings"
}

@Composable
fun AppNav(onWiped: () -> Unit) {
    val nav = rememberNavController()
    val go: (String) -> Unit = { route -> nav.navigate(route) { launchSingleTop = true } }
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(
        navController = nav,
        startDestination = Routes.HOME,
        // Transiciones suaves: fundido + pequeño deslizamiento.
        enterTransition = { fadeIn(tween(380)) + slideInHorizontally(tween(380)) { it / 12 } },
        exitTransition = { fadeOut(tween(260)) },
        popEnterTransition = { fadeIn(tween(380)) },
        popExitTransition = { fadeOut(tween(260)) + slideOutHorizontally(tween(320)) { it / 12 } }
    ) {
        composable(Routes.HOME) { HomeScreen(go) }
        composable(Routes.TOOLS) { ToolsScreen(go, back) }
        composable(Routes.SESSION) { SessionScreen(onExit = back, go = go) }
        composable(Routes.BREATHING) { BreathingScreen(back) }
        composable(Routes.GROUNDING) { GroundingScreen(back) }
        composable(Routes.SONGS) { SongsScreen(back) }
        composable(Routes.VIDEOS) { VideosScreen(back) }
        composable(Routes.CONTACTS) { ContactsScreen(back) }
        composable(Routes.REMINDERS) { RemindersScreen(back) }
        composable(Routes.DIARY) { DiaryScreen(back) }
        composable(Routes.GOODBOX) { GoodBoxScreen(back) }
        composable(Routes.SOUNDS) { SoundsScreen(back) }
        composable(Routes.THOUGHTS) { ThoughtsScreen(back) }
        composable(Routes.CRISIS) { CrisisPlanScreen(back) }
        composable(Routes.HELP) { HelpScreen(back, go) }
        composable(Routes.GAMES) { GamesScreen(go, back) }
        composable(Routes.BUBBLES) { GameScreen("Burbujas", back) { BubblesGame(it) } }
        composable(Routes.ZEN) { GameScreen("Jardín zen", back) { ZenGarden(it) } }
        composable(Routes.FIREFLIES) { GameScreen("Luciérnagas", back) { FirefliesGame(it) } }
        composable(Routes.MATH) { MathScreen(back) }
        composable(Routes.HUM) { HumScreen(back) }
        composable(Routes.JOKES) { JokesScreen(back) }
        composable(Routes.SETTINGS) { SettingsScreen(back, onWiped) }
    }
}
