package com.absensi.jamaah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("pengaturan", MODE_PRIVATE)
        setContent {
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean("mode_gelap", false)) }
            val tema = if (isDarkMode) TemaGelap else TemaTerang
            val skema = if (isDarkMode) {
                darkColorScheme(
                    primary = tema.utama,
                    onPrimary = Color(0xFF003731),
                    secondary = tema.aksen,
                    onSecondary = Color(0xFF1B1B1F),
                    background = tema.latar,
                    onBackground = tema.teksUtama,
                    surface = tema.permukaan,
                    onSurface = tema.teksUtama
                )
            } else {
                lightColorScheme(
                    primary = tema.utama,
                    onPrimary = Color.White,
                    secondary = tema.aksen,
                    onSecondary = Color(0xFF1B1B1F),
                    background = tema.latar,
                    onBackground = tema.teksUtama,
                    surface = tema.permukaan,
                    onSurface = tema.teksUtama
                )
            }
            CompositionLocalProvider(LokalTema provides tema) {
                MaterialTheme(colorScheme = skema) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainAppScreen(
                            viewModel = viewModel,
                            isDarkMode = isDarkMode,
                            onToggleTheme = {
                                isDarkMode = !isDarkMode
                                prefs.edit().putBoolean("mode_gelap", isDarkMode).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}
