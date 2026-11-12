package com.absensi.jamaah

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private val GradasiTerang = listOf(
    Color(0xFFE7F5EE),
    Color(0xFFCDE8DF),
    Color(0xFFF7E8C6),
    Color(0xFFE3F1EB)
)

private val GradasiGelap = listOf(
    Color(0xFF041B16),
    Color(0xFF0A2E27),
    Color(0xFF15263A),
    Color(0xFF07110F)
)

@Composable
fun LatarGradasiIslami(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val gelap = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val gradasi = if (gelap) GradasiGelap else GradasiTerang
    val pola = if (gelap) Color(0x33D4AF37) else Color(0x336B8F7E)
    val glowEmas = if (gelap) Color(0x33D4AF37) else Color(0x40F5A623)
    val glowHijau = if (gelap) Color(0x3300A88A) else Color(0x3300695C)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradasi))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.6f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowEmas, Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.08f),
                    radius = radius
                ),
                radius = radius,
                center = Offset(size.width * 0.85f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowHijau, Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.85f),
                    radius = radius
                ),
                radius = radius,
                center = Offset(size.width * 0.1f, size.height * 0.85f)
            )
            gambarPolaIslami(pola)
        }
        content()
    }
}

private fun DrawScope.gambarPolaIslami(warna: Color) {
    val jarak = 68.dp.toPx()
    val ukuran = 24.dp.toPx()
    val tebal = 0.9.dp.toPx()
    var y = 0f
    var baris = 0
    while (y < size.height + jarak) {
        var x = if (baris % 2 == 0) 0f else jarak / 2f
        while (x < size.width + jarak) {
            val pusat = Offset(x, y)
            drawBintangDelapan(warna, pusat, ukuran, tebal)
            x += jarak
        }
        y += jarak
        baris++
    }
}

private fun DrawScope.drawBintangDelapan(
    warna: Color,
    pusat: Offset,
    ukuran: Float,
    tebal: Float
) {
    val setengah = ukuran / 2f
    val sudut = Offset(pusat.x - setengah, pusat.y - setengah)
    val dimensi = Size(ukuran, ukuran)
    drawRect(
        color = warna,
        topLeft = sudut,
        size = dimensi,
        style = Stroke(width = tebal)
    )
    rotate(degrees = 45f, pivot = pusat) {
        drawRect(
            color = warna,
            topLeft = sudut,
            size = dimensi,
            style = Stroke(width = tebal)
        )
    }
    drawCircle(color = warna, radius = tebal, center = pusat)
}
