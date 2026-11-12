@file:OptIn(ExperimentalMaterial3Api::class)
package com.absensi.jamaah

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.komoui.components.AlertDialog
import com.komoui.components.AlertDialogAction
import com.komoui.components.AlertDialogCancel
import com.komoui.components.AlertDialogDescription
import com.komoui.components.AlertDialogTitle
import com.komoui.components.Avatar
import com.komoui.components.Badge
import com.komoui.components.BadgeVariant
import com.komoui.components.Button
import com.komoui.components.ButtonSize
import com.komoui.components.ButtonVariant
import com.komoui.components.Dialog
import com.komoui.components.DialogDescription
import com.komoui.components.DialogTitle
import com.komoui.components.Empty
import com.komoui.components.EmptyDescription
import com.komoui.components.EmptyHeader
import com.komoui.components.EmptyTitle
import com.komoui.components.Input
import com.komoui.components.LayoutOrientation
import com.komoui.components.Progress
import com.komoui.components.RadioButtonWithLabel
import com.komoui.components.RadioGroup
import com.komoui.components.Select
import com.komoui.components.Switch
import com.komoui.components.TabsList
import com.komoui.components.charts.BarChart
import com.komoui.components.charts.BarPoint
import com.komoui.components.charts.BarSeries
import com.komoui.components.charts.ChartAxisOptions
import com.komoui.components.charts.LineChart
import com.komoui.components.charts.LinePoint
import com.komoui.components.charts.LineSeries
import com.komoui.themes.komoTypography
import com.komoui.themes.styles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val screens = listOf("Dashboard", "Absensi", "Rekap", "Jamaah", "Pengaturan")

private fun inisial(nama: String): String =
    nama.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

private fun formatTanggalSingkat(tanggal: String): String =
    if (tanggal.length >= 10) tanggal.substring(5).replace("-", "/") else tanggal

@Composable
fun MainAppScreen(viewModel: AppViewModel, isDarkMode: Boolean, onToggleTheme: () -> Unit) {
    var currentScreen by remember { mutableStateOf("Dashboard") }
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Edit,
        Icons.Filled.DateRange,
        Icons.Filled.Person,
        Icons.Filled.Settings
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Absensi Jamaah", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(id = if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon),
                            contentDescription = if (isDarkMode) "Ubah ke mode terang" else "Ubah ke mode gelap",
                            tint = MaterialTheme.styles.foreground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.styles.card.copy(alpha = 0.72f),
                    titleContentColor = MaterialTheme.styles.foreground,
                    actionIconContentColor = MaterialTheme.styles.foreground
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.styles.card.copy(alpha = 0.78f), tonalElevation = 0.dp) {
                screens.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        selected = currentScreen == title,
                        onClick = { currentScreen = title },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.styles.primaryForeground,
                            selectedTextColor = MaterialTheme.styles.primary,
                            indicatorColor = MaterialTheme.styles.primary,
                            unselectedIconColor = MaterialTheme.styles.mutedForeground,
                            unselectedTextColor = MaterialTheme.styles.mutedForeground
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            when (currentScreen) {
                "Dashboard" -> DashboardScreen(viewModel) { currentScreen = "Absensi" }
                "Absensi" -> AbsensiScreen(viewModel)
                "Rekap" -> RekapScreen(viewModel)
                "Jamaah" -> JamaahScreen(viewModel)
                "Pengaturan" -> PengaturanScreen(viewModel, isDarkMode, onToggleTheme)
            }
        }
    }
}

@Composable
private fun DashboardStat(title: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        CardContent(modifier = Modifier.padding(vertical = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                Column {
                    Text(
                        count.toString(),
                        style = MaterialTheme.komoTypography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        title,
                        style = MaterialTheme.komoTypography.label,
                        color = MaterialTheme.styles.mutedForeground
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: AppViewModel, onMulaiAbsensi: () -> Unit) {
    val jamaah by viewModel.jamaahList.collectAsState()
    val absensi by viewModel.absensiList.collectAsState()
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val todayAbsensi = remember(absensi, today) { absensi.filter { it.tanggal == today } }
    val mengikuti = todayAbsensi.count { it.status == "Mengikuti" }
    val ijin = todayAbsensi.count { it.status == "Ijin" }
    val tidakMengikuti = todayAbsensi.count { it.status == "Tidak Mengikuti" }
    val telat = todayAbsensi.count { it.status == "Telat" }
    val totalPutra = jamaah.count { it.kategori == "Putra" }
    val totalPutri = jamaah.count { it.kategori == "Putri" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Aplikasi",
                    modifier = Modifier.size(65.dp).clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Assalamualaikum,",
                        style = MaterialTheme.komoTypography.body,
                        color = MaterialTheme.styles.mutedForeground
                    )
                    Text(
                        "Ringkasan Hari Ini",
                        style = MaterialTheme.komoTypography.titleLarge,
                        color = MaterialTheme.styles.foreground
                    )
                }
            }
        }
        item {
            Badge(variant = BadgeVariant.Secondary) { Text(today) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStat("Hadir", mengikuti, MaterialTheme.styles.chart1, Modifier.weight(1f))
                DashboardStat("Ijin", ijin, MaterialTheme.styles.chart2, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardStat("Telat", telat, MaterialTheme.styles.chart3, Modifier.weight(1f))
                DashboardStat("Alpha", tidakMengikuti, MaterialTheme.styles.chart4, Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                CardHeader {
                    CardTitle { Text("Total Jamaah Terdaftar") }
                    CardDescription { Text("Putra: $totalPutra  |  Putri: $totalPutri") }
                }
                CardContent {
                    Text(
                        "${jamaah.size} Orang",
                        style = MaterialTheme.komoTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.styles.primary
                    )
                }
                CardFooter {
                    Button(onClick = onMulaiAbsensi, fullWidth = true, size = ButtonSize.Lg) {
                        Text("Mulai Absensi Sekarang")
                    }
                }
            }
        }
        if (todayAbsensi.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    CardHeader {
                        CardTitle { Text("Status Hari Ini") }
                        CardDescription { Text("Rekap seluruh sesi absensi hari ini") }
                    }
                    CardContent {
                        BarChart(
                            series = listOf(
                                BarSeries(
                                    key = "hari-ini",
                                    label = "Jamaah",
                                    color = MaterialTheme.styles.primary,
                                    points = listOf(
                                        BarPoint("Hadir", mengikuti.toFloat()),
                                        BarPoint("Ijin", ijin.toFloat()),
                                        BarPoint("Telat", telat.toFloat()),
                                        BarPoint("Alpha", tidakMengikuti.toFloat())
                                    )
                                )
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            chartHeight = 180.dp,
                            showValueLabels = true,
                            axisOptions = ChartAxisOptions(
                                yTickCount = 3,
                                yLabelFormatter = { it.toInt().toString() }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AbsensiScreen(viewModel: AppViewModel) {
    var modeAbsensi by remember { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        TabsList(
            selectedTabIndex = modeAbsensi,
            onTabSelected = { modeAbsensi = it },
            tabs = listOf("Manual", "Scan Kamera"),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (modeAbsensi == 0) {
            AbsensiManualView(viewModel, Modifier.weight(1f))
        } else {
            AbsensiScanView(viewModel, Modifier.weight(1f))
        }
    }
}

@Composable
fun AbsensiManualView(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val jamaahList by viewModel.jamaahList.collectAsState()
    var tanggal by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var waktu by remember { mutableStateOf("Subuh") }
    val waktuList = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    var tabKategori by remember { mutableStateOf("Putra") }
    val absensiState = remember { mutableStateMapOf<String, String>() }
    var successMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(jamaahList, waktu, tanggal) {
        successMessage = ""
        val statusAda = viewModel.ambilAbsensiSesi(tanggal, waktu)
        jamaahList.forEach { absensiState[it.id] = statusAda[it.id] ?: "Mengikuti" }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
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
                        onOptionSelected = { waktu = it },
                        label = { it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Pilih waktu shalat"
                    )
                    Spacer(Modifier.height(12.dp))
                    TabsList(
                        selectedTabIndex = if (tabKategori == "Putra") 0 else 1,
                        onTabSelected = { tabKategori = if (it == 0) "Putra" else "Putri" },
                        tabs = listOf("Putra", "Putri"),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (successMessage.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            successMessage,
                            color = MaterialTheme.styles.primary,
                            style = MaterialTheme.komoTypography.bodyMedium
                        )
                    }
                }
            }
        }

        val filtered = jamaahList.filter { it.kategori == tabKategori }
        if (filtered.isEmpty()) {
            item {
                Empty {
                    EmptyHeader {
                        EmptyTitle { Text("Belum ada jamaah") }
                        EmptyDescription { Text("Tambahkan data jamaah terlebih dahulu.") }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { jamaah ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    CardContent(modifier = Modifier.padding(vertical = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Avatar(model = null, fallbackText = inisial(jamaah.nama), size = 40.dp)
                            Column {
                                Text(
                                    jamaah.nama,
                                    style = MaterialTheme.komoTypography.title,
                                    color = MaterialTheme.styles.foreground
                                )
                                Text(
                                    "${jamaah.kelas} • ${jamaah.alamat}",
                                    style = MaterialTheme.komoTypography.label,
                                    color = MaterialTheme.styles.mutedForeground
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        RadioGroup(
                            selectedValue = absensiState[jamaah.id] ?: "Mengikuti",
                            onValueChange = { absensiState[jamaah.id] = it },
                            orientation = LayoutOrientation.Vertical
                        ) {
                            listOf("Mengikuti", "Ijin", "Tidak Mengikuti", "Telat").forEach { status ->
                                RadioButtonWithLabel(value = status, label = status)
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val dataToSave = jamaahList.map {
                            Absensi(
                                jamaahId = it.id,
                                tanggal = tanggal,
                                waktuShalat = waktu,
                                status = absensiState[it.id] ?: "Mengikuti"
                            )
                        }
                        viewModel.simpanAbsensi(dataToSave)
                        successMessage = "Data absensi Putra & Putri berhasil disimpan."
                    }
                },
                fullWidth = true,
                size = ButtonSize.Lg
            ) {
                Text("Simpan Semua Absensi")
            }
        }
    }
}

@Composable
fun AbsensiScanView(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var tanggal by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var waktu by remember { mutableStateOf("Subuh") }
    val waktuList = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    var isSessionReady by remember { mutableStateOf(false) }
    var scanResultMsg by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    var lastScanTime by remember { mutableStateOf(0L) }

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

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

            if (!isSessionReady) {
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
                        Text("Siapkan Sesi Scan Kamera")
                    }
                }
            } else if (!hasCameraPermission) {
                item {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        fullWidth = true
                    ) {
                        Text("Izinkan Akses Kamera")
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    DecoratedBarcodeView(ctx).apply {
                                        setStatusText("")
                                        resume()
                                        decodeContinuous(object : BarcodeCallback {
                                            override fun barcodeResult(result: BarcodeResult?) {
                                                result?.text?.let { scannedId ->
                                                    val currentTime = System.currentTimeMillis()
                                                    if (currentTime - lastScanTime > 2500) {
                                                        lastScanTime = currentTime
                                                        coroutineScope.launch {
                                                            val pesan = viewModel.prosesScanQr(scannedId, tanggal, waktu)
                                                            scanResultMsg = pesan
                                                            showPopup = true
                                                            try {
                                                                val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                                                                if (pesan.contains("✅")) {
                                                                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                                                } else if (pesan.contains("⚠️")) {
                                                                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                                                                } else {
                                                                    toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
                                                                }
                                                            } catch (e: Exception) {
                                                                e.printStackTrace()
                                                            }
                                                            delay(2000)
                                                            showPopup = false
                                                        }
                                                    }
                                                }
                                            }

                                            override fun possibleResultPoints(
                                                resultPoints: MutableList<com.google.zxing.ResultPoint>?
                                            ) {
                                            }
                                        })
                                    }
                                },
                                onRelease = { view -> view.pause() }
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = { isSessionReady = false },
                        fullWidth = true,
                        variant = ButtonVariant.Destructive
                    ) {
                        Text("Hentikan Kamera")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showPopup,
            enter = fadeIn(animationSpec = tween(300)) +
                scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) +
                scaleOut(targetScale = 0.8f, animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(modifier = Modifier.padding(32.dp).fillMaxWidth()) {
                CardContent(modifier = Modifier.padding(vertical = 24.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val sukses = scanResultMsg.contains("✅")
                        val peringatan = scanResultMsg.contains("⚠️")
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        sukses -> MaterialTheme.styles.chart1
                                        peringatan -> MaterialTheme.styles.chart3
                                        else -> MaterialTheme.styles.destructive
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    sukses -> Icons.Filled.Check
                                    peringatan -> Icons.Filled.Warning
                                    else -> Icons.Filled.Close
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            scanResultMsg,
                            style = MaterialTheme.komoTypography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RekapScreen(viewModel: AppViewModel) {
    val absensi by viewModel.absensiList.collectAsState()
    val jamaah by viewModel.jamaahList.collectAsState()

    fun getWeek(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return "Lainnya"
            val cal = Calendar.getInstance().apply { time = date }
            "Minggu ke-${cal.get(Calendar.WEEK_OF_YEAR)} (${cal.get(Calendar.YEAR)})"
        } catch (e: Exception) {
            "Lainnya"
        }
    }

    var tabKategori by remember { mutableStateOf("Putra") }
    var selectedWeek by remember { mutableStateOf<String?>(null) }
    var absensiToEdit by remember { mutableStateOf<Absensi?>(null) }

    absensiToEdit?.let { current ->
        var editStatus by remember(current.id) { mutableStateOf(current.status) }
        val jamaahName = jamaah.find { it.id == current.jamaahId }?.nama ?: "Jamaah"

        AlertDialog(
            open = true,
            onDismissRequest = { absensiToEdit = null },
            title = { AlertDialogTitle { Text("Edit Absensi") } },
            description = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nama: $jamaahName", fontWeight = FontWeight.Bold)
                    Text(
                        "${current.tanggal} • ${current.waktuShalat}",
                        color = MaterialTheme.styles.mutedForeground
                    )
                    RadioGroup(
                        selectedValue = editStatus,
                        onValueChange = { editStatus = it },
                        orientation = LayoutOrientation.Vertical
                    ) {
                        listOf("Mengikuti", "Ijin", "Tidak Mengikuti", "Telat").forEach { status ->
                            RadioButtonWithLabel(value = status, label = status)
                        }
                    }
                }
            },
            actions = {
                AlertDialogCancel(onClick = { absensiToEdit = null }) { Text("Batal") }
                Spacer(Modifier.width(8.dp))
                AlertDialogAction(
                    onClick = {
                        viewModel.perbaruiAbsensi(current.copy(status = editStatus))
                        absensiToEdit = null
                    }
                ) { Text("Simpan") }
            }
        )
    }

    val jamaahKategori = jamaah.filter { it.kategori == tabKategori }
    val idsKategori = jamaahKategori.map { it.id }.toSet()
    val absensiKategori = absensi.filter { it.jamaahId in idsKategori }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Rekap & Peringkat", style = MaterialTheme.komoTypography.titleLarge)
        Text(
            "Pisahkan pengurus putra dan putri",
            style = MaterialTheme.komoTypography.body,
            color = MaterialTheme.styles.mutedForeground
        )
        Spacer(Modifier.height(12.dp))
        TabsList(
            selectedTabIndex = if (tabKategori == "Putra") 0 else 1,
            onTabSelected = {
                tabKategori = if (it == 0) "Putra" else "Putri"
                selectedWeek = null
            },
            tabs = listOf("Putra", "Putri"),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        if (selectedWeek == null) {
            val grouped = absensiKategori.groupBy { getWeek(it.tanggal) }
            if (grouped.isEmpty()) {
                Empty(modifier = Modifier.weight(1f)) {
                    EmptyHeader {
                        EmptyTitle { Text("Belum ada data absensi") }
                        EmptyDescription { Text("Data akan muncul setelah absensi disimpan.") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(grouped.keys.toList().sorted().reversed()) { week ->
                        val jumlahAnak = jamaahKategori.count { j ->
                            absensiKategori.any { it.jamaahId == j.id && getWeek(it.tanggal) == week }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedWeek = week }
                        ) {
                            CardContent(modifier = Modifier.padding(vertical = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(week, style = MaterialTheme.komoTypography.title)
                                        Text(
                                            "$jumlahAnak jamaah",
                                            style = MaterialTheme.komoTypography.label,
                                            color = MaterialTheme.styles.mutedForeground
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.styles.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedWeek = null }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Text(selectedWeek!!, style = MaterialTheme.komoTypography.titleLarge)
            }
            Spacer(Modifier.height(8.dp))

            val weekData = absensiKategori.filter { getWeek(it.tanggal) == selectedWeek }
            val jamaahInWeek = jamaahKategori.filter { j -> weekData.any { it.jamaahId == j.id } }
            val ranked = jamaahInWeek.map { j ->
                val data = weekData.filter { it.jamaahId == j.id }
                val alpha = data.count { it.status == "Tidak Mengikuti" }
                val telat = data.count { it.status == "Telat" }
                Triple(j, data.sortedBy { it.tanggal }, 70 - (alpha * 2) - telat)
            }.sortedByDescending { it.third }

            if (ranked.isEmpty()) {
                Empty(modifier = Modifier.weight(1f)) {
                    EmptyHeader {
                        EmptyTitle { Text("Belum ada jamaah") }
                        EmptyDescription { Text("Belum ada absensi pada minggu ini.") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(ranked, key = { _, item -> item.first.id }) { index, item ->
                        val (j, dataJamaah, point) = item
                        RekapJamaahCard(
                            index = index,
                            jamaah = j,
                            dataJamaah = dataJamaah,
                            point = point,
                            onEdit = { absensiToEdit = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RekapJamaahCard(
    index: Int,
    jamaah: Jamaah,
    dataJamaah: List<Absensi>,
    point: Int,
    onEdit: (Absensi) -> Unit
) {
    var detailOpen by remember(jamaah.id) { mutableStateOf(false) }
    val statusCounts = listOf(
        "Mengikuti" to "Hadir",
        "Ijin" to "Ijin",
        "Telat" to "Telat",
        "Tidak Mengikuti" to "Alpha"
    ).map { (status, label) ->
        BarPoint(label, dataJamaah.count { it.status == status }.toFloat())
    }
    val daily = dataJamaah.groupBy { it.tanggal }.toSortedMap().map { (tanggal, list) ->
        LinePoint(formatTanggalSingkat(tanggal), list.count { it.status == "Mengikuti" }.toFloat())
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        CardContent(modifier = Modifier.padding(vertical = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (index) {
                                0 -> MaterialTheme.styles.chart3
                                1 -> MaterialTheme.styles.mutedForeground
                                2 -> MaterialTheme.styles.chart5
                                else -> MaterialTheme.styles.primary
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#${index + 1}",
                        color = Color.White,
                        style = MaterialTheme.komoTypography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Avatar(model = null, fallbackText = inisial(jamaah.nama), size = 40.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(jamaah.nama, style = MaterialTheme.komoTypography.title)
                    Text(
                        "Poin disiplin: $point",
                        style = MaterialTheme.komoTypography.label,
                        color = if (point < 50) {
                            MaterialTheme.styles.destructive
                        } else {
                            MaterialTheme.styles.mutedForeground
                        }
                    )
                }
                Badge(
                    variant = if (point < 50) BadgeVariant.Destructive else BadgeVariant.Secondary
                ) {
                    Text("$point")
                }
            }
            Spacer(Modifier.height(8.dp))
            Progress(progress = point.coerceIn(0, 70) / 70f, height = 8.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Status Mingguan",
                style = MaterialTheme.komoTypography.labelMedium,
                color = MaterialTheme.styles.mutedForeground
            )
            BarChart(
                series = listOf(
                    BarSeries(
                        key = "status-${jamaah.id}",
                        label = "Jumlah",
                        color = MaterialTheme.styles.primary,
                        points = statusCounts
                    )
                ),
                modifier = Modifier.fillMaxWidth(),
                chartHeight = 160.dp,
                showTooltip = false,
                showValueLabels = true,
                axisOptions = ChartAxisOptions(
                    yTickCount = 3,
                    yLabelFormatter = { it.toInt().toString() }
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Tren Harian (Mengikuti)",
                style = MaterialTheme.komoTypography.labelMedium,
                color = MaterialTheme.styles.mutedForeground
            )
            LineChart(
                series = listOf(
                    LineSeries(
                        key = "harian-${jamaah.id}",
                        label = "Mengikuti",
                        color = MaterialTheme.styles.chart2,
                        points = daily
                    )
                ),
                modifier = Modifier.fillMaxWidth(),
                chartHeight = 160.dp,
                showTooltip = false,
                axisOptions = ChartAxisOptions(
                    yDomain = 0f..5f,
                    yTickCount = 3,
                    yLabelFormatter = { it.toInt().toString() }
                )
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { detailOpen = !detailOpen },
                variant = ButtonVariant.Outline,
                size = ButtonSize.Sm
            ) {
                Text(if (detailOpen) "Sembunyikan Detail" else "Lihat Detail Absensi")
            }
            if (detailOpen) {
                Spacer(Modifier.height(8.dp))
                dataJamaah.forEach { ab ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${ab.tanggal} • ${ab.waktuShalat}",
                                style = MaterialTheme.komoTypography.labelMedium
                            )
                            Text(
                                ab.status,
                                style = MaterialTheme.komoTypography.label,
                                color = MaterialTheme.styles.mutedForeground
                            )
                        }
                        Button(
                            onClick = { onEdit(ab) },
                            variant = ButtonVariant.Ghost,
                            size = ButtonSize.Sm
                        ) {
                            Text("Edit")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun JamaahScreen(viewModel: AppViewModel) {
    val jamaahList by viewModel.jamaahList.collectAsState()
    var idSantri by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var kelas by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Putra") }
    var tabKategori by remember { mutableStateOf("Putra") }
    var jamaahToEdit by remember { mutableStateOf<Jamaah?>(null) }

    jamaahToEdit?.let { current ->
        var editId by remember(current.id) { mutableStateOf(current.idSantri) }
        var editNama by remember(current.id) { mutableStateOf(current.nama) }
        var editKelas by remember(current.id) { mutableStateOf(current.kelas) }
        var editAlamat by remember(current.id) { mutableStateOf(current.alamat) }
        var editKategori by remember(current.id) { mutableStateOf(current.kategori) }

        Dialog(
            open = true,
            onDismissRequest = { jamaahToEdit = null },
            header = {
                DialogTitle { Text("Edit / Hapus Jamaah") }
                DialogDescription { Text("Perbarui data atau hapus jamaah ini.") }
            },
            body = {
                Input(
                    value = editId,
                    onValueChange = { editId = it },
                    placeholder = "ID QR",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Input(
                    value = editNama,
                    onValueChange = { editNama = it },
                    placeholder = "Nama",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Input(
                    value = editKelas,
                    onValueChange = { editKelas = it },
                    placeholder = "Kamar/Kelas",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Input(
                    value = editAlamat,
                    onValueChange = { editAlamat = it },
                    placeholder = "Asal",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Kategori",
                    style = MaterialTheme.komoTypography.labelMedium,
                    color = MaterialTheme.styles.mutedForeground
                )
                RadioGroup(
                    selectedValue = editKategori,
                    onValueChange = { editKategori = it },
                    orientation = LayoutOrientation.Horizontal
                ) {
                    RadioButtonWithLabel(value = "Putra", label = "Putra")
                    RadioButtonWithLabel(value = "Putri", label = "Putri")
                }
            },
            footer = {
                Button(
                    onClick = {
                        viewModel.hapusJamaah(current)
                        jamaahToEdit = null
                    },
                    variant = ButtonVariant.Destructive
                ) {
                    Text("Hapus")
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { jamaahToEdit = null }, variant = ButtonVariant.Outline) {
                    Text("Batal")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.perbaruiJamaah(
                            current.copy(
                                idSantri = editId,
                                nama = editNama,
                                kelas = editKelas,
                                alamat = editAlamat,
                                kategori = editKategori
                            )
                        )
                        jamaahToEdit = null
                    }
                ) {
                    Text("Simpan")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                CardHeader {
                    CardTitle { Text("Kelola Data Jamaah") }
                    CardDescription { Text("Daftarkan santri dan pisahkan berdasarkan kategori.") }
                }
                CardContent {
                    Input(
                        value = idSantri,
                        onValueChange = { idSantri = it },
                        placeholder = "ID QR (contoh: QR001)",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Input(
                        value = nama,
                        onValueChange = { nama = it },
                        placeholder = "Nama lengkap",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Input(
                            value = kelas,
                            onValueChange = { kelas = it },
                            placeholder = "Kamar/Kelas",
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Input(
                            value = alamat,
                            onValueChange = { alamat = it },
                            placeholder = "Asal",
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Kategori",
                        style = MaterialTheme.komoTypography.labelMedium,
                        color = MaterialTheme.styles.mutedForeground
                    )
                    RadioGroup(
                        selectedValue = kategori,
                        onValueChange = { kategori = it },
                        orientation = LayoutOrientation.Horizontal
                    ) {
                        RadioButtonWithLabel(value = "Putra", label = "Putra")
                        RadioButtonWithLabel(value = "Putri", label = "Putri")
                    }
                }
                CardFooter {
                    Button(
                        onClick = {
                            if (nama.isNotBlank() && idSantri.isNotBlank()) {
                                viewModel.tambahJamaah(idSantri, nama, kelas, alamat, kategori)
                                idSantri = ""
                                nama = ""
                                kelas = ""
                                alamat = ""
                            }
                        },
                        fullWidth = true
                    ) {
                        Text("Tambahkan Jamaah Baru")
                    }
                }
            }
        }
        item {
            TabsList(
                selectedTabIndex = if (tabKategori == "Putra") 0 else 1,
                onTabSelected = { tabKategori = if (it == 0) "Putra" else "Putri" },
                tabs = listOf("Daftar Putra", "Daftar Putri"),
                modifier = Modifier.fillMaxWidth()
            )
        }

        val filtered = jamaahList.filter { it.kategori == tabKategori }
        if (filtered.isEmpty()) {
            item {
                Empty {
                    EmptyHeader {
                        EmptyTitle { Text("Belum ada jamaah") }
                        EmptyDescription { Text("Tambahkan data pada formulir di atas.") }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { jamaah ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { jamaahToEdit = jamaah }
                ) {
                    CardContent(modifier = Modifier.padding(vertical = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Avatar(model = null, fallbackText = inisial(jamaah.nama), size = 40.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(jamaah.nama, style = MaterialTheme.komoTypography.title)
                                Text(
                                    "ID ${jamaah.idSantri} • ${jamaah.kelas}",
                                    style = MaterialTheme.komoTypography.label,
                                    color = MaterialTheme.styles.mutedForeground
                                )
                            }
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.styles.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PengaturanScreen(
    viewModel: AppViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                CardHeader {
                    CardTitle { Text("Informasi Aplikasi") }
                    CardDescription { Text("Absensi Jamaah • KomoUI") }
                }
                CardContent {
                    Text("Versi 4.0 (Pemisahan Putra & Putri)", style = MaterialTheme.komoTypography.body)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Database: Cloud Realtime Firestore",
                        style = MaterialTheme.komoTypography.body,
                        color = MaterialTheme.styles.mutedForeground
                    )
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                CardContent(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mode Gelap", style = MaterialTheme.komoTypography.title)
                            Text(
                                "Ikuti preferensi tampilan aplikasi",
                                style = MaterialTheme.komoTypography.label,
                                color = MaterialTheme.styles.mutedForeground
                            )
                        }
                        Switch(checked = isDarkMode, onCheckedChange = { onToggleTheme() })
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                CardHeader {
                    CardTitle { Text("Sistem Database") }
                    CardDescription {
                        Text("Tindakan ini menghapus seluruh data dan mengisi data dummy.")
                    }
                }
                CardFooter {
                    Button(
                        onClick = { showDialog = true },
                        variant = ButtonVariant.Destructive,
                        fullWidth = true
                    ) {
                        Text("Reset & Masukkan Data Dummy Awal")
                    }
                }
            }
        }
    }

    AlertDialog(
        open = showDialog,
        onDismissRequest = { showDialog = false },
        title = { AlertDialogTitle { Text("Konfirmasi Reset") } },
        description = {
            AlertDialogDescription {
                Text("Yakin ingin mereset Cloud Database? Semua data saat ini akan terhapus diganti dengan data dummy awal.")
            }
        },
        actions = {
            AlertDialogCancel(onClick = { showDialog = false }) { Text("Batal") }
            Spacer(Modifier.width(8.dp))
            AlertDialogAction(
                onClick = {
                    viewModel.resetData()
                    showDialog = false
                }
            ) { Text("Ya, Reset") }
        }
    )
}
