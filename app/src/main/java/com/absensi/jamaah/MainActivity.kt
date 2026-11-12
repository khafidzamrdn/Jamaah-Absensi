package com.absensi.jamaah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.komoui.themes.KomoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("pengaturan", MODE_PRIVATE)
        setContent {
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean("mode_gelap", false)) }
            KomoTheme(
                isDarkTheme = isDarkMode,
                komoLightColors = KomoTerang,
                komoDarkColors = KomoGelap,
                materialLightColors = SkemaTerang,
                materialDarkColors = SkemaGelap
            ) {
                LatarGradasiIslami {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
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
