package com.doctorapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val LOGIN = "login"
    const val PATIENT_HOME = "patient_home"
    const val SPECIALIST_INBOX = "specialist_inbox"
    const val ADMIN = "admin"
    const val CHAT = "chat/{requestId}"
    fun chat(requestId: Int) = "chat/$requestId"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreenRoute(navController)
        }
        composable(Routes.PATIENT_HOME) {
            PatientHomeScreen()
        }
        composable(Routes.SPECIALIST_INBOX) {
            SpecialistInboxScreen(onOpenChat = { id -> navController.navigate(Routes.chat(id)) })
        }
        composable(Routes.ADMIN) {
            AdminScreen()
        }
        composable(Routes.CHAT) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId")?.toIntOrNull() ?: 0
            ChatScreen(requestId)
        }
    }
}

@Composable
fun LoginScreenRoute(navController: NavHostController) {
    LoginScreen(onLoggedIn = { role ->
        val destination = when (role) {
            "patient" -> Routes.PATIENT_HOME
            "specialist" -> Routes.SPECIALIST_INBOX
            "admin" -> Routes.ADMIN
            else -> Routes.PATIENT_HOME
        }
        navController.navigate(destination) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    })
}
