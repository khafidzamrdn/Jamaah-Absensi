package com.absensi.jamaah

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.komoui.themes.komoTypography
import com.komoui.themes.styles

enum class ButtonVariant {
    Default,
    Destructive,
    Outline,
    Secondary,
    Ghost,
    Link
}

enum class ButtonSize {
    Default,
    Sm,
    Xs,
    Lg,
    Icon,
    IconSm
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Default,
    size: ButtonSize = ButtonSize.Default,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    shape: Shape = RoundedCornerShape(50),
    contentPadding: PaddingValues? = null,
    @Suppress("UNUSED_PARAMETER") color: ButtonColors? = null,
    content: @Composable RowScope.() -> Unit
) {
    val gaya = MaterialTheme.styles
    val gelap = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val interaksi = remember { MutableInteractionSource() }
    val ditekan by interaksi.collectIsPressedAsState()
    val skala by animateFloatAsState(
        targetValue = if (ditekan) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 900f),
        label = "skalaTombol"
    )
    val aktif = enabled && !loading

    val isi: Brush? = when (variant) {
        ButtonVariant.Default -> Brush.verticalGradient(
            if (gelap) listOf(Color(0xFF179E87), Color(0xFF0B6B5B))
            else listOf(Color(0xFF0F8A78), Color(0xFF00695C))
        )
        ButtonVariant.Destructive -> Brush.verticalGradient(
            if (gelap) listOf(Color(0xFFEF5350), Color(0xFFB71C1C))
            else listOf(Color(0xFFE53935), Color(0xFFB71C1C))
        )
        ButtonVariant.Secondary -> Brush.verticalGradient(
            if (gelap) listOf(Color(0xFF1D3A34), Color(0xFF142A25))
            else listOf(Color(0xFFDDF2EC), Color(0xFFC8E8DF))
        )
        ButtonVariant.Outline -> Brush.verticalGradient(
            if (gelap) listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.05f))
            else listOf(Color.White.copy(alpha = 0.62f), Color.White.copy(alpha = 0.32f))
        )
        ButtonVariant.Ghost, ButtonVariant.Link -> null
    }

    val warnaTeks = when (variant) {
        ButtonVariant.Default, ButtonVariant.Destructive -> Color.White
        ButtonVariant.Secondary -> if (gelap) Color(0xFFB2DFDB) else Color(0xFF004D40)
        ButtonVariant.Outline -> gaya.foreground
        ButtonVariant.Ghost -> gaya.foreground
        ButtonVariant.Link -> gaya.primary
    }

    val tepi = when (variant) {
        ButtonVariant.Default, ButtonVariant.Destructive -> Brush.linearGradient(
            listOf(
                WarnaAksen.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.55f),
                WarnaAksen.copy(alpha = 0.35f)
            )
        )
        ButtonVariant.Outline, ButtonVariant.Secondary -> Brush.linearGradient(
            listOf(
                WarnaAksen.copy(alpha = 0.6f),
                Color.White.copy(alpha = if (gelap) 0.25f else 0.85f),
                WarnaAksen.copy(alpha = 0.25f)
            )
        )
        ButtonVariant.Ghost, ButtonVariant.Link -> Brush.linearGradient(
            listOf(Color.Transparent, Color.Transparent)
        )
    }

    val tinggi = when (size) {
        ButtonSize.Default -> 44.dp
        ButtonSize.Sm -> 36.dp
        ButtonSize.Xs -> 30.dp
        ButtonSize.Lg -> 52.dp
        ButtonSize.Icon -> 44.dp
        ButtonSize.IconSm -> 32.dp
    }
    val paddingHorizontal = when (size) {
        ButtonSize.Default -> 20.dp
        ButtonSize.Sm -> 14.dp
        ButtonSize.Xs -> 10.dp
        ButtonSize.Lg -> 28.dp
        ButtonSize.Icon, ButtonSize.IconSm -> 0.dp
    }

    val bayangan = when (variant) {
        ButtonVariant.Default -> if (gelap) Color(0x88000000) else Color(0x44004D40)
        ButtonVariant.Destructive -> if (gelap) Color(0x88000000) else Color(0x44B71C1C)
        ButtonVariant.Secondary, ButtonVariant.Outline -> if (gelap) Color(0x66000000) else Color(0x33004D40)
        ButtonVariant.Ghost, ButtonVariant.Link -> Color.Transparent
    }

    val teksGaya = MaterialTheme.komoTypography.bodyMedium.copy(
        fontWeight = FontWeight.SemiBold,
        textDecoration = if (variant == ButtonVariant.Link) TextDecoration.Underline else TextDecoration.None
    )

    val dasar = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .graphicsLayer {
            scaleX = skala
            scaleY = skala
            alpha = if (enabled) 1f else 0.55f
        }
        .then(
            if (bayangan != Color.Transparent) {
                Modifier.shadow(
                    elevation = if (ditekan) 4.dp else 10.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = bayangan,
                    spotColor = bayangan
                )
            } else Modifier
        )
        .clip(shape)
        .then(
            if (isi != null) Modifier.background(isi, shape)
            else Modifier.background(Color.Transparent, shape)
        )
        .then(
            if (ditekan && variant in listOf(ButtonVariant.Ghost, ButtonVariant.Link)) {
                Modifier.background(gaya.primary.copy(alpha = 0.10f), shape)
            } else Modifier
        )
        .border(width = if (variant in listOf(ButtonVariant.Ghost, ButtonVariant.Link)) 0.dp else 1.dp, brush = tepi, shape = shape)
        .clickable(
            interactionSource = interaksi,
            indication = ripple(color = warnaTeks),
            enabled = aktif,
            role = Role.Button,
            onClick = onClick
        )
        .defaultMinSize(minHeight = tinggi)
        .then(if (size == ButtonSize.Icon || size == ButtonSize.IconSm) Modifier.width(tinggi) else Modifier)
        .then(contentPadding?.let { Modifier.padding(it) } ?: Modifier.padding(horizontal = paddingHorizontal))

    CompositionLocalProvider(LocalContentColor provides warnaTeks) {
        ProvideTextStyle(teksGaya) {
            Box(modifier = dasar, contentAlignment = Alignment.Center) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    content()
                }
            }
        }
    }
}
