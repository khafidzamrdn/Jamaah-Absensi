package com.absensi.jamaah

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.komoui.themes.BoxShadow
import com.komoui.themes.KomoStyles

val WarnaUtama = Color(0xFF00695C)
val WarnaAksen = Color(0xFFF5A623)
val WarnaBg = Color(0xFFF4F8F6)
val WarnaPermukaan = Color(0xFFFFFFFF)

abstract class GayaKomoDasar : KomoStyles {
    @Composable
    override fun shadow2xs(): List<BoxShadow> = listOf(
        BoxShadow(offsetX = 0.dp, offsetY = 1.dp, blurRadius = 3.dp, spread = 0.dp, color = border)
    )

    @Composable
    override fun shadowXs(): List<BoxShadow> = shadow2xs()

    @Composable
    override fun shadowSm(): List<BoxShadow> = listOf(
        BoxShadow(offsetX = 0.dp, offsetY = 1.dp, blurRadius = 3.dp, spread = 0.dp, color = border),
        BoxShadow(offsetX = 0.dp, offsetY = 1.dp, blurRadius = 2.dp, spread = (-1).dp, color = border)
    )

    @Composable
    override fun shadow(): List<BoxShadow> = shadowSm()

    @Composable
    override fun shadowMd(): List<BoxShadow> = listOf(
        BoxShadow(offsetX = 0.dp, offsetY = 1.dp, blurRadius = 3.dp, spread = 0.dp, color = border),
        BoxShadow(offsetX = 0.dp, offsetY = 2.dp, blurRadius = 4.dp, spread = (-1).dp, color = border)
    )

    @Composable
    override fun shadowLg(): List<BoxShadow> = listOf(
        BoxShadow(offsetX = 0.dp, offsetY = 1.dp, blurRadius = 3.dp, spread = 0.dp, color = border),
        BoxShadow(offsetX = 0.dp, offsetY = 4.dp, blurRadius = 6.dp, spread = (-1).dp, color = border)
    )

    @Composable
    override fun shadowXl(): List<BoxShadow> = listOf(
        BoxShadow(offsetX = 0.dp, offsetY = 1.dp, blurRadius = 3.dp, spread = 0.dp, color = border),
        BoxShadow(offsetX = 0.dp, offsetY = 8.dp, blurRadius = 10.dp, spread = (-1).dp, color = border)
    )

    @Composable
    override fun shadow2xl(): List<BoxShadow> = shadow2xs()
}

object KomoTerang : GayaKomoDasar() {
    override val background: Color = WarnaBg
    override val foreground: Color = Color(0xFF13201D)
    override val card: Color = WarnaPermukaan
    override val cardForeground: Color = Color(0xFF13201D)
    override val popover: Color = WarnaPermukaan
    override val popoverForeground: Color = Color(0xFF13201D)
    override val primary: Color = WarnaUtama
    override val primaryForeground: Color = Color.White
    override val secondary: Color = Color(0xFFE6F2EF)
    override val secondaryForeground: Color = Color(0xFF004D40)
    override val muted: Color = Color(0xFFEDF3F1)
    override val mutedForeground: Color = Color(0xFF5F6B68)
    override val accent: Color = Color(0xFFFFF3E0)
    override val accentForeground: Color = Color(0xFF8A5A00)
    override val destructive: Color = Color(0xFFC62828)
    override val destructiveForeground: Color = Color.White
    override val border: Color = Color(0xFFDCE5E2)
    override val input: Color = Color(0xFFCBD8D4)
    override val ring: Color = WarnaUtama
    override val chart1: Color = Color(0xFF2E7D32)
    override val chart2: Color = Color(0xFF1565C0)
    override val chart3: Color = Color(0xFFF57F17)
    override val chart4: Color = Color(0xFFC62828)
    override val chart5: Color = WarnaAksen
    override val sidebar: Color = Color(0xFFF4F8F6)
    override val sidebarForeground: Color = Color(0xFF13201D)
    override val sidebarPrimary: Color = WarnaUtama
    override val sidebarPrimaryForeground: Color = Color.White
    override val sidebarAccent: Color = Color(0xFFE6F2EF)
    override val sidebarAccentForeground: Color = Color(0xFF004D40)
    override val sidebarBorder: Color = Color(0xFFDCE5E2)
    override val sidebarRing: Color = WarnaUtama
    override val snackbar: Color = WarnaPermukaan
}

object KomoGelap : GayaKomoDasar() {
    override val background: Color = Color(0xFF0E1413)
    override val foreground: Color = Color(0xFFE0E3E1)
    override val card: Color = Color(0xFF161D1C)
    override val cardForeground: Color = Color(0xFFE0E3E1)
    override val popover: Color = Color(0xFF1E2826)
    override val popoverForeground: Color = Color(0xFFE0E3E1)
    override val primary: Color = Color(0xFF80CBC4)
    override val primaryForeground: Color = Color(0xFF003731)
    override val secondary: Color = Color(0xFF1C2A27)
    override val secondaryForeground: Color = Color(0xFFB2DFDB)
    override val muted: Color = Color(0xFF1F2A28)
    override val mutedForeground: Color = Color(0xFF9AA8A5)
    override val accent: Color = Color(0xFF2A2418)
    override val accentForeground: Color = Color(0xFFFFD180)
    override val destructive: Color = Color(0xFFEF5350)
    override val destructiveForeground: Color = Color.White
    override val border: Color = Color(0x33FFFFFF)
    override val input: Color = Color(0x40FFFFFF)
    override val ring: Color = Color(0xFF80CBC4)
    override val chart1: Color = Color(0xFF66BB6A)
    override val chart2: Color = Color(0xFF64B5F6)
    override val chart3: Color = Color(0xFFFFB74D)
    override val chart4: Color = Color(0xFFEF5350)
    override val chart5: Color = WarnaAksen
    override val sidebar: Color = Color(0xFF0E1413)
    override val sidebarForeground: Color = Color(0xFFE0E3E1)
    override val sidebarPrimary: Color = Color(0xFF80CBC4)
    override val sidebarPrimaryForeground: Color = Color(0xFF003731)
    override val sidebarAccent: Color = Color(0xFF1C2A27)
    override val sidebarAccentForeground: Color = Color(0xFFB2DFDB)
    override val sidebarBorder: Color = Color(0x33FFFFFF)
    override val sidebarRing: Color = Color(0xFF80CBC4)
    override val snackbar: Color = Color(0xFF161D1C)
}

val SkemaTerang = lightColorScheme(
    primary = WarnaUtama,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00251F),
    secondary = WarnaAksen,
    onSecondary = Color(0xFF2A1A00),
    background = WarnaBg,
    onBackground = Color(0xFF13201D),
    surface = WarnaPermukaan,
    onSurface = Color(0xFF13201D),
    surfaceVariant = Color(0xFFE6F2EF),
    onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFF6F7977)
)

val SkemaGelap = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005046),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = WarnaAksen,
    onSecondary = Color(0xFF2A1A00),
    background = Color(0xFF0E1413),
    onBackground = Color(0xFFE0E3E1),
    surface = Color(0xFF161D1C),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF1C2A27),
    onSurfaceVariant = Color(0xFFBFC9C7),
    outline = Color(0xFF899390)
)
