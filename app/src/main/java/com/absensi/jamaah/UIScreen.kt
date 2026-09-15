@file:OptIn(ExperimentalMaterial3Api::class)
package com.absensi.jamaah

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val WarnaUtama = Color(0xFF00695C)
val WarnaAksen = Color(0xFFF5A623)
val WarnaBg = Color(0xFFF1F8E9)
val WarnaPermukaan = Color(0xFFFFFFFF)

@Composable
fun MainAppScreen(viewModel: AppViewModel) {
    var currentScreen by remember { mutableStateOf("Dashboard") }
    val screens = listOf("Dashboard", "Absensi", "Rekap", "Jamaah", "Pengaturan")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Edit, Icons.Filled.DateRange, Icons.Filled.Person, Icons.Filled.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = WarnaPermukaan, tonalElevation = 8.dp) {
                screens.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        selected = currentScreen == title,
                        onClick = { currentScreen = title },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WarnaPermukaan,
                            selectedTextColor = WarnaUtama,
                            indicatorColor = WarnaUtama,
                            unselectedIconColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(WarnaBg)) {
            when (currentScreen) {
                "Dashboard" -> DashboardScreen(viewModel) { currentScreen = "Absensi" }
                "Absensi" -> AbsensiScreen(viewModel)
                "Rekap" -> RekapScreen(viewModel)
                "Jamaah" -> JamaahScreen(viewModel)
                "Pengaturan" -> PengaturanScreen(viewModel)
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, count: Int, bgColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DashboardScreen(viewModel: AppViewModel, onMulaiAbsensi: () -> Unit) {
    val jamaah = viewModel.jamaahList.collectAsState().value
    val absensi = viewModel.absensiList.collectAsState().value
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    val todayAbsensi = absensi.filter { it.tanggal == today }
    val mengikuti = todayAbsensi.count { it.status == "Mengikuti" }
    val ijin = todayAbsensi.count { it.status == "Ijin" }
    val tidakMengikuti = todayAbsensi.count { it.status == "Tidak Mengikuti" }
    val telat = todayAbsensi.count { it.status == "Telat" }

    val totalPutra = jamaah.count { it.kategori == "Putra" }
    val totalPutri = jamaah.count { it.kategori == "Putri" }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Aplikasi",
                modifier = Modifier.size(65.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Assalamualaikum,", fontSize = 16.sp, color = Color.Gray)
                Text("Ringkasan Hari Ini", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(today, fontSize = 14.sp, color = WarnaAksen, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardCard("Hadir", mengikuti, Color(0xFF2E7D32), Modifier.weight(1f))
            DashboardCard("Ijin", ijin, Color(0xFF1565C0), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardCard("Telat", telat, Color(0xFFF57F17), Modifier.weight(1f))
            DashboardCard("Alpha", tidakMengikuti, Color(0xFFC62828), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaUtama),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Jamaah Terdaftar", color = WarnaAksen, fontSize = 14.sp)
                Text("${jamaah.size} Orang", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text("( Putra: $totalPutra | Putri: $totalPutri )", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onMulaiAbsensi, 
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarnaAksen)
                ) {
                    Text("MULAI ABSENSI SEKARANG", color = WarnaUtama, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AbsensiScreen(viewModel: AppViewModel) {
    var modeAbsensi by remember { mutableStateOf(0) }
    val tabs = listOf("Manual", "Scan Kamera")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = modeAbsensi, containerColor = WarnaPermukaan, contentColor = WarnaUtama) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = modeAbsensi == index, onClick = { modeAbsensi = index }, text = { Text(title, fontWeight = FontWeight.Bold) })
            }
        }
        if (modeAbsensi == 0) AbsensiManualView(viewModel) else AbsensiScanView(viewModel)
    }
}

@Composable
fun AbsensiManualView(viewModel: AppViewModel) {
    val jamaahList = viewModel.jamaahList.collectAsState().value
    var tanggal by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var waktu by remember { mutableStateOf("Subuh") }
    val waktuList = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    
    var tabKategori by remember { mutableStateOf("Putra") }
    val absensiState = remember { mutableStateMapOf<String, String>() }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(jamaahList, waktu, tanggal) {
        val sudahAda = viewModel.cekAbsensiAda(tanggal, waktu)
        if (sudahAda) {
            errorMessage = "Absensi untuk sesi ini sudah dilakukan."
        } else {
            errorMessage = ""
            jamaahList.forEach { absensiState[it.id] = "Mengikuti" }
        }
        successMessage = ""
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        OutlinedTextField(value = tanggal, onValueChange = { tanggal = it }, label = { Text("Tanggal") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            waktuList.forEach { w -> 
                FilterChip(
                    selected = waktu == w, 
                    onClick = { waktu = w }, 
                    label = { Text(w, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarnaAksen, selectedLabelColor = WarnaUtama)
                ) 
            }
        }

        TabRow(selectedTabIndex = if (tabKategori == "Putra") 0 else 1, containerColor = WarnaPermukaan, contentColor = WarnaUtama, modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            Tab(selected = tabKategori == "Putra", onClick = { tabKategori = "Putra" }, text = { Text("👦 Putra") })
            Tab(selected = tabKategori == "Putri", onClick = { tabKategori = "Putri" }, text = { Text("🧕 Putri") })
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        } else if (successMessage.isNotEmpty()) {
            Text(successMessage, color = WarnaUtama, fontWeight = FontWeight.Bold)
        } else {
            val filteredJamaah = jamaahList.filter { it.kategori == tabKategori }
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredJamaah) { jamaah ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = WarnaPermukaan)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${jamaah.nama} (${jamaah.kelas})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Mengikuti", "Ijin").forEach { status ->
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = absensiState[jamaah.id] == status, onClick = { absensiState[jamaah.id] = status })
                                        Text(status, fontSize = 14.sp)
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Tidak Mengikuti", "Telat").forEach { status ->
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = absensiState[jamaah.id] == status, onClick = { absensiState[jamaah.id] = status })
                                        Text(status, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Simpan akan mengeksekusi semua data (Putra & Putri sekaligus)
                        val dataToSave = jamaahList.map {
                            Absensi(jamaahId = it.id, tanggal = tanggal, waktuShalat = waktu, status = absensiState[it.id] ?: "Mengikuti")
                        }
                        viewModel.simpanAbsensi(dataToSave)
                        successMessage = "✅ Data Absensi (Putra & Putri) Berhasil Disimpan!"
                    }
                }, 
                modifier = Modifier.fillMaxWidth().height(55.dp).padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Semua Absensi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AbsensiScanView(viewModel: AppViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var tanggal by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var waktu by remember { mutableStateOf("Subuh") }
    val waktuList = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    
    var isSessionReady by remember { mutableStateOf(false) }
    var scanResultMsg by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    var lastScanTime by remember { mutableStateOf(0L) }

    val context = LocalContext.current
    var hasCameraPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) 
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { 
        hasCameraPermission = it 
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(value = tanggal, onValueChange = { tanggal = it }, label = { Text("Tanggal") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                waktuList.forEach { w -> 
                    FilterChip(
                        selected = waktu == w, 
                        onClick = { waktu = w; isSessionReady = false }, 
                        label = { Text(w, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarnaAksen, selectedLabelColor = WarnaUtama)
                    ) 
                }
            }
            
            if (!isSessionReady) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.siapkanAbsensiScan(tanggal, waktu)
                            isSessionReady = true
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)
                ) {
                    Text("Siapkan Sesi Scan Kamera", fontSize = 16.sp)
                }
            } else {
                if (!hasCameraPermission) {
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Izinkan Akses Kamera")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                DecoratedBarcodeView(ctx).apply {
                                    setStatusText("")
                                    resume()
                                }
                            },
                            update = { view ->
                                view.decodeContinuous(object : BarcodeCallback {
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
                                                        if (pesan.contains("✅")) toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                                        else if (pesan.contains("⚠️")) toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                                                        else toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
                                                    } catch (e: Exception) { e.printStackTrace() }

                                                    delay(2000)
                                                    showPopup = false
                                                }
                                            }
                                        }
                                    }
                                    override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
                                })
                            },
                            onRelease = { view -> view.pause() }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { isSessionReady = false }, 
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hentikan Kamera")
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
            Card(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (scanResultMsg.contains("✅")) Color(0xFFE8F5E9) else if (scanResultMsg.contains("⚠️")) Color(0xFFFFFDE7) else Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(if (scanResultMsg.contains("✅")) Color(0xFF2E7D32) else if (scanResultMsg.contains("⚠️")) Color(0xFFF57F17) else Color(0xFFC62828)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = if (scanResultMsg.contains("✅")) Icons.Filled.Check else if (scanResultMsg.contains("⚠️")) Icons.Filled.Warning else Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = scanResultMsg, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = if (scanResultMsg.contains("✅")) Color(0xFF1B5E20) else if (scanResultMsg.contains("⚠️")) Color(0xFFF57F17) else Color(0xFFB71C1C))
                }
            }
        }
    }
}

@Composable
fun RekapScreen(viewModel: AppViewModel) {
    val absensi = viewModel.absensiList.collectAsState().value
    val jamaah = viewModel.jamaahList.collectAsState().value
    
    fun getWeek(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return "Lainnya"
            val cal = java.util.Calendar.getInstance().apply { time = date }
            "Minggu ke-${cal.get(java.util.Calendar.WEEK_OF_YEAR)} (${cal.get(java.util.Calendar.YEAR)})"
        } catch (e: Exception) { "Lainnya" }
    }

    var selectedWeek by remember { mutableStateOf<String?>(null) }
    var absensiToEdit by remember { mutableStateOf<Absensi?>(null) }

    if (absensiToEdit != null) {
        var editStatus by remember { mutableStateOf(absensiToEdit!!.status) }
        val jamaahName = jamaah.find { it.id == absensiToEdit!!.jamaahId }?.nama ?: "Jamaah"

        AlertDialog(
            onDismissRequest = { absensiToEdit = null },
            title = { Text("Edit Absensi", color = WarnaUtama, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Nama: $jamaahName", fontWeight = FontWeight.Bold)
                    Text("Waktu: ${absensiToEdit!!.tanggal} | ${absensiToEdit!!.waktuShalat}")
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("Mengikuti", "Ijin", "Tidak Mengikuti", "Telat").forEach { status ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { editStatus = status }) {
                            RadioButton(selected = editStatus == status, onClick = { editStatus = status })
                            Text(status)
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { viewModel.perbaruiAbsensi(absensiToEdit!!.copy(status = editStatus)); absensiToEdit = null }, colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)) { Text("Simpan") } },
            dismissButton = { TextButton(onClick = { absensiToEdit = null }) { Text("Batal", color = Color.Gray) } }
        )
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        if (selectedWeek == null) {
            Text("Rekap & Peringkat Kedisiplinan", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
            Text("Pilih minggu untuk melihat poin jamaah", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            
            val grouped = absensi.groupBy { getWeek(it.tanggal) }
            if (grouped.isEmpty()) {
                Text("Belum ada data absensi.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(grouped.keys.toList().sorted().reversed()) { week ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { selectedWeek = week },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = WarnaPermukaan)
                        ) {
                            Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(week, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
                                Icon(Icons.Filled.DateRange, contentDescription = null, tint = WarnaAksen)
                            }
                        }
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedWeek = null }) { Icon(Icons.Filled.Home, contentDescription = "Kembali", tint = WarnaUtama) }
                Text(selectedWeek!!, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
            }
            Spacer(modifier = Modifier.height(12.dp))

            var tabKategori by remember { mutableStateOf("Putra") }
            TabRow(selectedTabIndex = if (tabKategori == "Putra") 0 else 1, containerColor = WarnaPermukaan, contentColor = WarnaUtama, modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
                Tab(selected = tabKategori == "Putra", onClick = { tabKategori = "Putra" }, text = { Text("👦 Putra") })
                Tab(selected = tabKategori == "Putri", onClick = { tabKategori = "Putri" }, text = { Text("🧕 Putri") })
            }
            Spacer(modifier = Modifier.height(8.dp))

            val weekData = absensi.filter { getWeek(it.tanggal) == selectedWeek }
            // Filter berdasarkan kategori yang sedang dipilih di tab
            val jamaahInWeek = jamaah.filter { j -> j.kategori == tabKategori && weekData.any { it.jamaahId == j.id } }

            val rankedJamaah = jamaahInWeek.map { j ->
                val dataJamaah = weekData.filter { it.jamaahId == j.id }
                val alpha = dataJamaah.count { it.status == "Tidak Mengikuti" }
                val telat = dataJamaah.count { it.status == "Telat" }
                val finalPoint = 70 - (alpha * 2) - (telat * 1)
                Triple(j, dataJamaah.sortedBy { it.tanggal }, finalPoint)
            }.sortedByDescending { it.third }

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(rankedJamaah) { index, (j, dataJamaah, point) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = WarnaPermukaan)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp))
                                            .background(if (index == 0) WarnaAksen else if (index == 1) Color(0xFFB0BEC5) else if (index == 2) Color(0xFFCD7F32) else WarnaUtama),
                                        contentAlignment = Alignment.Center
                                    ) { Text("#${index + 1}", color = Color.White, fontWeight = FontWeight.Bold) }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(j.nama, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
                                        Text("Poin Disiplin: $point", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(point < 50) Color.Red else WarnaAksen)
                                    }
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            dataJamaah.forEach { ab ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${ab.tanggal} - ${ab.waktuShalat}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Status: ${ab.status}", fontSize = 12.sp, color = if(ab.status == "Mengikuti") Color(0xFF2E7D32) else Color.Red)
                                    }
                                    OutlinedButton(onClick = { absensiToEdit = ab }, shape = RoundedCornerShape(8.dp)) { Text("Edit") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JamaahScreen(viewModel: AppViewModel) {
    val jamaahList = viewModel.jamaahList.collectAsState().value
    var idSantri by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var kelas by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Putra") } // Form Kategori
    
    var tabKategori by remember { mutableStateOf("Putra") } // Tab List
    var jamaahToEdit by remember { mutableStateOf<Jamaah?>(null) }

    if (jamaahToEdit != null) {
        var editId by remember { mutableStateOf(jamaahToEdit!!.idSantri) }
        var editNama by remember { mutableStateOf(jamaahToEdit!!.nama) }
        var editKelas by remember { mutableStateOf(jamaahToEdit!!.kelas) }
        var editAlamat by remember { mutableStateOf(jamaahToEdit!!.alamat) }
        var editKategori by remember { mutableStateOf(jamaahToEdit!!.kategori) }

        AlertDialog(
            onDismissRequest = { jamaahToEdit = null },
            title = { Text("Edit / Hapus Jamaah", color = WarnaUtama, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = editId, onValueChange = { editId = it }, label = { Text("ID QR") })
                    OutlinedTextField(value = editNama, onValueChange = { editNama = it }, label = { Text("Nama") })
                    OutlinedTextField(value = editKelas, onValueChange = { editKelas = it }, label = { Text("Kelas") })
                    OutlinedTextField(value = editAlamat, onValueChange = { editAlamat = it }, label = { Text("Alamat") })
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Kategori: ", fontWeight = FontWeight.Bold)
                        RadioButton(selected = editKategori == "Putra", onClick = { editKategori = "Putra" })
                        Text("Putra")
                        RadioButton(selected = editKategori == "Putri", onClick = { editKategori = "Putri" })
                        Text("Putri")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.perbaruiJamaah(jamaahToEdit!!.copy(idSantri = editId, nama = editNama, kelas = editKelas, alamat = editAlamat, kategori = editKategori))
                    jamaahToEdit = null
                }, colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hapusJamaah(jamaahToEdit!!); jamaahToEdit = null }) { Text("Hapus", color = Color.Red) }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Kelola Data Jamaah", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaPermukaan)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = idSantri, onValueChange = { idSantri = it }, label = { Text("ID QR Code (Contoh: QR001)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = kelas, onValueChange = { kelas = it }, label = { Text("Kamar/Kelas") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = alamat, onValueChange = { alamat = it }, label = { Text("Asal") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                // --- Kategori Radio Button ---
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Kategori: ", fontWeight = FontWeight.Bold, color = WarnaUtama)
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { kategori = "Putra" }.padding(4.dp)) {
                        RadioButton(selected = kategori == "Putra", onClick = { kategori = "Putra" })
                        Text("Putra")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { kategori = "Putri" }.padding(4.dp)) {
                        RadioButton(selected = kategori == "Putri", onClick = { kategori = "Putri" })
                        Text("Putri")
                    }
                }
                
                Button(
                    onClick = { 
                        if(nama.isNotBlank() && idSantri.isNotBlank()) {
                            viewModel.tambahJamaah(idSantri, nama, kelas, alamat, kategori)
                            idSantri = ""; nama = ""; kelas = ""; alamat = ""
                        }
                    }, 
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarnaAksen)
                ) {
                    Text("Tambahkan Jamaah Baru", color = WarnaUtama, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // --- Tab Filter List ---
        TabRow(selectedTabIndex = if (tabKategori == "Putra") 0 else 1, containerColor = WarnaPermukaan, contentColor = WarnaUtama, modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            Tab(selected = tabKategori == "Putra", onClick = { tabKategori = "Putra" }, text = { Text("👦 Daftar Putra") })
            Tab(selected = tabKategori == "Putri", onClick = { tabKategori = "Putri" }, text = { Text("🧕 Daftar Putri") })
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        val filteredJamaah = jamaahList.filter { it.kategori == tabKategori }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredJamaah) { jamaah ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { jamaahToEdit = jamaah },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = WarnaPermukaan),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(jamaah.nama, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
                            Text("ID: ${jamaah.idSantri} | Kamar: ${jamaah.kelas}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = WarnaAksen)
                    }
                }
            }
        }
    }
}

@Composable
fun PengaturanScreen(viewModel: AppViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Pengaturan", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarnaPermukaan),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Informasi Aplikasi", fontWeight = FontWeight.Bold, color = WarnaUtama)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Versi: 4.0 (Pemisahan Putra & Putri)")
                Text("Database: Cloud Realtime Firestore")
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Sistem Database", fontWeight = FontWeight.Bold, color = WarnaUtama)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    onClick = { showDialog = true }, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset & Masukkan Data Dummy Awal")
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Konfirmasi Reset") },
                text = { Text("Yakin ingin mereset Cloud Database? Semua data saat ini akan terhapus diganti dengan data dummy awal.") },
                confirmButton = {
                    Button(onClick = { viewModel.resetData(); showDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)) { Text("Ya, Reset") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal", color = Color.Gray) } }
            )
        }
    }
}
