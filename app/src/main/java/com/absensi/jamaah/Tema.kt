package com.absensi.jamaah

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val WarnaUtama = Color(0xFF00695C)
val WarnaAksen = Color(0xFFF5A623)
val WarnaBg = Color(0xFFF1F8E9)
val WarnaPermukaan = Color(0xFFFFFFFF)

data class TemaAplikasi(
    val latar: Color,
    val permukaan: Color,
    val utama: Color,
    val aksen: Color,
    val teksUtama: Color,
    val teksSekunder: Color
)

val TemaTerang = TemaAplikasi(
    latar = WarnaBg,
    permukaan = WarnaPermukaan,
    utama = WarnaUtama,
    aksen = WarnaAksen,
    teksUtama = Color(0xFF1C1B1F),
    teksSekunder = Color.Gray
)

val TemaGelap = TemaAplikasi(
    latar = Color(0xFF121212),
    permukaan = Color(0xFF1E1E1E),
    utama = Color(0xFF80CBC4),
    aksen = WarnaAksen,
    teksUtama = Color(0xFFE6E1E5),
    teksSekunder = Color(0xFF9E9E9E)
)

val LokalTema = staticCompositionLocalOf { TemaTerang }
