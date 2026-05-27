package com.illareklab.data_demo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.illareklab.data_demo.ui.screens.AlertasScreen
import com.illareklab.data_demo.ui.screens.AudioScreen
import com.illareklab.data_demo.ui.screens.CamaraScreen
import com.illareklab.data_demo.ui.screens.LoginScreen
import com.illareklab.data_demo.ui.screens.SensoresScreen
import com.illareklab.data_demo.ui.screens.SyncScreen

@Composable
fun Navigation() {
    val globalNavController = rememberNavController()

    NavHost(
        navController = globalNavController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                globalNavController.navigate("main_app") {
                    // Destruye el login del historial → "atrás" cierra la app
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("main_app") { MainNavigationScreen() }
    }
}

@Composable
fun MainNavigationScreen() {
    val tabsNavController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        tabsNavController.navigate("sensores")
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Sensores") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        tabsNavController.navigate("camara")
                    },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    label = { Text("Cámara") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        tabsNavController.navigate("audio")
                    },
                    icon = { Icon(Icons.Default.Call, contentDescription = null) },
                    label = { Text("Audio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        tabsNavController.navigate("sync")
                    },
                    icon = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                    label = { Text("Sincronizar") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        tabsNavController.navigate("alertas")
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("Alertas") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = tabsNavController,
            startDestination = "sensores",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("sensores") { SensoresScreen() }
            composable("camara") { CamaraScreen() }
            composable("audio") { AudioScreen() }
            composable("sync") { SyncScreen() }
            composable("alertas") { AlertasScreen() }
        }
    }
}