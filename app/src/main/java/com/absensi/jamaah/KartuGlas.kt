package com.absensi.jamaah

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.komoui.themes.komoTypography
import com.komoui.themes.styles

@Composable
fun Card(
    modifier: Modifier = Modifier,
    radius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val gelap = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bentuk = RoundedCornerShape(radius)
    val isi = if (gelap) {
        listOf(Color(0xFF10231F).copy(alpha = 0.86f), Color(0xFF0B1714).copy(alpha = 0.58f))
    } else {
        listOf(Color.White.copy(alpha = 0.82f), Color.White.copy(alpha = 0.48f))
    }
    val garis = if (gelap) {
        listOf(
            WarnaAksen.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.16f),
            WarnaAksen.copy(alpha = 0.18f)
        )
    } else {
        listOf(
            WarnaAksen.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.9f),
            Color(0xFF8FBFB2).copy(alpha = 0.4f)
        )
    }
    val bayangan = if (gelap) Color(0x99000000) else Color(0x33004D40)

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = bentuk,
                clip = false,
                ambientColor = bayangan,
                spotColor = bayangan
            )
            .clip(bentuk)
            .background(Brush.verticalGradient(isi))
            .border(width = 1.dp, brush = Brush.linearGradient(garis), shape = bentuk)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (gelap) 0.10f else 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun CardHeader(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun CardTitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ProvideTextStyle(
        value = MaterialTheme.komoTypography.titleLarge.copy(
            color = MaterialTheme.styles.cardForeground
        )
    ) {
        Column(modifier = modifier) {
            content()
        }
    }
}

@Composable
fun CardDescription(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ProvideTextStyle(
        value = MaterialTheme.komoTypography.body.copy(
            color = MaterialTheme.styles.mutedForeground
        )
    ) {
        Column(modifier = modifier) {
            content()
        }
    }
}

@Composable
fun CardContent(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun CardFooter(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
