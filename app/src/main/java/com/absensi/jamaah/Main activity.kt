package com.absensi.jamaah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppMainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMainScreen(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val screens = listOf("Dashboard", "Absensi", "Rekap", "Jamaah", "Pengaturan")
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Text(item.first().toString()) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item) {
                                popUpTo("Dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "Dashboard", Modifier.padding(innerPadding)) {
            composable("Dashboard") { DashboardScreen(viewModel) { navController.navigate("Absensi"); selectedItem = 1 } }
            composable("Absensi") { AbsensiScreen(viewModel) }
            composable("Rekap") { RekapScreen(viewModel) }
            composable("Jamaah") { JamaahScreen(viewModel) }
            composable("Pengaturan") { PengaturanScreen(viewModel) }
        }
    }
}
