package com.absensi.jamaah

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.komoui.components.Badge
import com.komoui.components.BadgeVariant
import com.komoui.components.Input
import com.komoui.components.Select
import com.komoui.themes.komoTypography
import com.komoui.themes.styles
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun bunyiHasilNfc(pesan: String) {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        when {
            pesan.contains("✅") -> tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            pesan.contains("⚠️") -> tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
            else -> tone.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun AbsensiNfcView(
    viewModel: AppViewModel,
    nfcUidFlow: SharedFlow<String>,
    onNfcActiveChange: (Boolean) -> Unit,
    onBukaPengaturanNfc: () -> Unit,
    nfcTersedia: Boolean,
    nfcAktif: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val jamaahList by viewModel.jamaahList.collectAsState()
    var tanggal by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var waktu by remember { mutableStateOf("Subuh") }
    val waktuList = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    var isSessionReady by remember { mutableStateOf(false) }
    var hasil by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    var tapTerakhir by remember { mutableStateOf(0L) }
    var uidTerakhir by remember { mutableStateOf("") }

    val mendengarkan = nfcTersedia && nfcAktif && isSessionReady

    DisposableEffect(mendengarkan) {
        onNfcActiveChange(mendengarkan)
        onDispose { onNfcActiveChange(false) }
    }

    LaunchedEffect(mendengarkan, tanggal, waktu) {
        if (!mendengarkan) return@LaunchedEffect
        nfcUidFlow.collect { uid ->
            val sekarang = System.currentTimeMillis()
            // Hindari kartu yang sama terbaca berulang dalam 2 detik.
            if (uid.equals(uidTerakhir, ignoreCase = true) && sekarang - tapTerakhir < 2000) {
                return@collect
            }
            tapTerakhir = sekarang
            uidTerakhir = uid
            hasil = viewModel.prosesTapNfc(uid, tanggal, waktu)
            showPopup = true
            bunyiHasilNfc(hasil)
            delay(2200)
            showPopup = false
        }
    }

    val kartuTerdaftar = jamaahList.count { it.nfcUid.isNotBlank() }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    CardContent(modifier = Modifier.padding(vertical = 16.dp)) {
                        Input(
                            value = tanggal,
                            onValueChange = { tanggal = it },
                            placeholder = "Tanggal (yyyy-MM-dd)",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        Select(
                            options = waktuList,
                            selectedOption = waktu,
                            onOptionSelected = {
                                waktu = it
                                isSessionReady = false
                            },
                            label = { it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "Pilih waktu shalat"
                        )
                    }
                }
            }

            if (!nfcTersedia) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        CardContent(modifier = Modifier.padding(vertical = 20.dp)) {
                            Text(
                                "Perangkat ini tidak memiliki hardware NFC.",
                                style = MaterialTheme.komoTypography.title,
                                color = MaterialTheme.styles.foreground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Gunakan perangkat Android yang mendukung NFC untuk absensi dengan kartu RFID 13,56 MHz.",
                                style = MaterialTheme.komoTypography.body,
                                color = MaterialTheme.styles.mutedForeground
                            )
                        }
                    }
                }
            } else if (!nfcAktif) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        CardContent(modifier = Modifier.padding(vertical = 20.dp)) {
                            Text(
                                "NFC belum aktif.",
                                style = MaterialTheme.komoTypography.title,
                                color = MaterialTheme.styles.foreground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Aktifkan NFC di pengaturan perangkat agar kartu bisa dibaca.",
                                style = MaterialTheme.komoTypography.body,
                                color = MaterialTheme.styles.mutedForeground
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onBukaPengaturanNfc, fullWidth = true) {
                                Text("Buka Pengaturan NFC")
                            }
                        }
                    }
                }
            } else if (!isSessionReady) {
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.siapkanAbsensiScan(tanggal, waktu)
                                isSessionReady = true
                            }
                        },
                        fullWidth = true,
                        size = ButtonSize.Lg
                    ) {
                        Text("Siapkan Sesi Scan NFC")
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        CardContent(modifier = Modifier.padding(vertical = 32.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.styles.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CreditCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.styles.primary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Tempelkan kartu RFID",
                                    style = MaterialTheme.komoTypography.titleLarge,
                                    color = MaterialTheme.styles.foreground,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Arahkan kartu ke bagian belakang HP dan tahan sampai bunyi notifikasi.",
                                    style = MaterialTheme.komoTypography.body,
                                    color = MaterialTheme.styles.mutedForeground,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Badge(variant = BadgeVariant.Secondary) {
                                    Text("$kartuTerdaftar kartu terdaftar")
                                }
                            }
                        }
                    }
                }
                item {
                    if (kartuTerdaftar > 0) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            CardHeader { CardTitle { Text("Kartu Terdaftar") } }
                            CardContent(modifier = Modifier.padding(bottom = 16.dp)) {
                                jamaahList.filter { it.nfcUid.isNotBlank() }.forEach { j ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(j.nama, style = MaterialTheme.komoTypography.body)
                                        Text(
                                            "UID: ${j.nfcUid}",
                                            style = MaterialTheme.komoTypography.label,
                                            color = MaterialTheme.styles.mutedForeground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = { isSessionReady = false },
                        fullWidth = true,
                        variant = ButtonVariant.Destructive
                    ) {
                        Text("Hentikan Sesi NFC")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showPopup,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(modifier = Modifier.padding(32.dp).fillMaxWidth()) {
                CardContent(modifier = Modifier.padding(vertical = 24.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val sukses = hasil.contains("✅")
                        val peringatan = hasil.contains("⚠️")
                        val komposisiCentang by rememberLottieComposition(
                            LottieCompositionSpec.Asset("lottie/check_pop.json")
                        )
                        if (sukses) {
                            LottieAnimation(
                                composition = komposisiCentang,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(110.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (peringatan) MaterialTheme.styles.chart3
                                        else MaterialTheme.styles.destructive
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (peringatan) Icons.Filled.Warning else Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(hasil, style = MaterialTheme.komoTypography.titleLarge, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
