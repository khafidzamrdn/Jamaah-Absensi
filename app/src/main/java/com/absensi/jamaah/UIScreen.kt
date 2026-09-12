@file:OptIn(ExperimentalMaterial3Api::class)
package com.absensi.jamaah

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val total = todayAbsensi.size
    val persentase = if (total > 0) ((mengikuti + telat).toFloat() / total * 100).toInt() else 0

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tanggal: $today")
                Text("Total Jamaah: ${jamaah.size}")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Mengikuti: $mengikuti")
                Text("Ijin: $ijin")
                Text("Tidak Mengikuti: $tidakMengikuti")
                Text("Telat: $telat")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Persentase Kehadiran Hari Ini: $persentase%", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onMulaiAbsensi, modifier = Modifier.fillMaxWidth()) {
            Text("+ MULAI ABSENSI")
        }
    }
}

@Composable
fun AbsensiScreen(viewModel: AppViewModel) {
    var modeAbsensi by remember { mutableStateOf(0) }
    val tabs = listOf("Manual", "Scan Kamera")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = modeAbsensi) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = modeAbsensi == index, onClick = { modeAbsensi = index }, text = { Text(title) })
            }
        }
        if (modeAbsensi == 0) {
            AbsensiManualView(viewModel)
        } else {
            AbsensiScanView(viewModel)
        }
    }
}

@Composable
fun AbsensiManualView(viewModel: AppViewModel) {
    val jamaahList = viewModel.jamaahList.collectAsState().value
    var tanggal by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var waktu by remember { mutableStateOf("Subuh") }
    val waktuList = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")
    
    var absensiState = remember { mutableStateMapOf<Int, String>() }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(jamaahList, waktu) {
        val sudahAda = viewModel.cekAbsensiAda(tanggal, waktu)
        if (sudahAda) {
            errorMessage = "Absensi untuk waktu shalat ini sudah dilakukan."
        } else {
            errorMessage = ""
            jamaahList.forEach { absensiState[it.id] = "Mengikuti" }
        }
        successMessage = ""
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        OutlinedTextField(value = tanggal, onValueChange = { tanggal = it }, label = { Text("Tanggal") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            waktuList.forEach { w -> FilterChip(selected = waktu == w, onClick = { waktu = w }, label = { Text(w) }) }
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        } else if (successMessage.isNotEmpty()) {
            Text(successMessage, color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(jamaahList) { jamaah ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${jamaah.nama} (${jamaah.kelas})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Mengikuti", "Ijin").forEach { status ->
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = absensiState[jamaah.id] == status, onClick = { absensiState[jamaah.id] = status })
                                        Text(status, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Tidak Mengikuti", "Telat").forEach { status ->
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = absensiState[jamaah.id] == status, onClick = { absensiState[jamaah.id] = status })
                                        Text(status, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(onClick = {
                val dataToSave = jamaahList.map {
                    Absensi(jamaahId = it.id, tanggal = tanggal, waktuShalat = waktu, status = absensiState[it.id] ?: "Mengikuti")
                }
                viewModel.simpanAbsensi(dataToSave)
                successMessage = "Absensi Berhasil Disimpan!"
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Simpan Absensi")
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
    var scanResultMsg by remember { mutableStateOf("Tentukan Tanggal & Waktu, lalu klik 'Siapkan Sesi'.") }
    var lastScanTime by remember { mutableStateOf(0L) }

    // Meminta izin kamera secara langsung di dalam aplikasi
    val context = LocalContext.current
    var hasCameraPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) 
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { 
        hasCameraPermission = it 
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(value = tanggal, onValueChange = { tanggal = it }, label = { Text("Tanggal") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            waktuList.forEach { w -> FilterChip(selected = waktu == w, onClick = { waktu = w; isSessionReady = false }, label = { Text(w) }) }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        if (!isSessionReady) {
            Button(onClick = {
                coroutineScope.launch {
                    viewModel.siapkanAbsensiScan(tanggal, waktu)
                    isSessionReady = true
                    scanResultMsg = "Sesi Siap! Arahkan kamera ke QR Santri."
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Siapkan Sesi Scan")
            }
        } else {
            if (!hasCameraPermission) {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Izinkan Akses Kamera")
                }
            } else {
                // Kamera yang Tertanam di Layar (Continuous Scan)
                Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    AndroidView(
                        factory = { ctx ->
                            DecoratedBarcodeView(ctx).apply {
                                setStatusText("") // Menghapus tulisan default
                                resume() // Menyalakan kamera
                            }
                        },
                        update = { view ->
                            view.decodeContinuous(object : BarcodeCallback {
                                override fun barcodeResult(result: BarcodeResult?) {
                                    result?.text?.let { scannedId ->
                                        val currentTime = System.currentTimeMillis()
                                        // Jeda 2 detik (2000 ms) agar tidak nge-spam
                                        if (currentTime - lastScanTime > 2000) {
                                            lastScanTime = currentTime
                                            coroutineScope.launch {
                                                scanResultMsg = viewModel.prosesScanQr(scannedId, tanggal, waktu)
                                            }
                                        }
                                    }
                                }
                                override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
                            })
                        },
                        onRelease = { view ->
                            view.pause() // Mematikan kamera jika halaman ditutup
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { isSessionReady = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Hentikan Kamera")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        // Menampilkan Notifikasi Hasil Scan
        Text(
            text = scanResultMsg, 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold, 
            color = if(scanResultMsg.contains("✅")) MaterialTheme.colorScheme.primary else if(scanResultMsg.contains("⚠️")) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        )
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
            "Tahun ${cal.get(java.util.Calendar.YEAR)} - Minggu ke-${cal.get(java.util.Calendar.WEEK_OF_YEAR)}"
        } catch (e: Exception) {
            "Lainnya"
        }
    }

    var selectedWeek by remember { mutableStateOf<String?>(null) }
    var absensiToEdit by remember { mutableStateOf<Absensi?>(null) }

    if (absensiToEdit != null) {
        var editStatus by remember { mutableStateOf(absensiToEdit!!.status) }
        val jamaahName = jamaah.find { it.id == absensiToEdit!!.jamaahId }?.nama ?: "Jamaah"

        AlertDialog(
            onDismissRequest = { absensiToEdit = null },
            title = { Text("Edit Absensi") },
            text = {
                Column {
                    Text("Nama: $jamaahName", fontWeight = FontWeight.Bold)
                    Text("Tanggal: ${absensiToEdit!!.tanggal} | Waktu: ${absensiToEdit!!.waktuShalat}")
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("Mengikuti", "Ijin", "Tidak Mengikuti", "Telat").forEach { status ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { editStatus = status }) {
                            RadioButton(selected = editStatus == status, onClick = { editStatus = status })
                            Text(status)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.perbaruiAbsensi(absensiToEdit!!.copy(status = editStatus))
                    absensiToEdit = null
                }) { Text("Simpan Perubahan") }
            },
            dismissButton = { TextButton(onClick = { absensiToEdit = null }) { Text("Batal") } }
        )
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        if (selectedWeek == null) {
            Text("Rekap Mingguan", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            val grouped = absensi.groupBy { getWeek(it.tanggal) }
            if (grouped.isEmpty()) {
                Text("Belum ada data absensi.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(grouped.keys.toList().sorted().reversed()) { week ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedWeek = week }) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(week, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { selectedWeek = null }) { Text("Kembali") }
                Spacer(modifier = Modifier.width(12.dp))
                Text(selectedWeek!!, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            val weekData = absensi.filter { getWeek(it.tanggal) == selectedWeek }
            val jamaahInWeek = jamaah.filter { j -> weekData.any { it.jamaahId == j.id } }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(jamaahInWeek) { j ->
                    val dataJamaah = weekData.filter { it.jamaahId == j.id }.sortedBy { it.tanggal }
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(j.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            dataJamaah.forEach { ab ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${ab.tanggal} - ${ab.waktuShalat}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("Status: ${ab.status}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    OutlinedButton(onClick = { absensiToEdit = ab }) { Text("Edit") }
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
    var jamaahToEdit by remember { mutableStateOf<Jamaah?>(null) }

    if (jamaahToEdit != null) {
        var editId by remember { mutableStateOf(jamaahToEdit!!.idSantri) }
        var editNama by remember { mutableStateOf(jamaahToEdit!!.nama) }
        var editKelas by remember { mutableStateOf(jamaahToEdit!!.kelas) }
        var editAlamat by remember { mutableStateOf(jamaahToEdit!!.alamat) }

        AlertDialog(
            onDismissRequest = { jamaahToEdit = null },
            title = { Text("Edit / Hapus Jamaah") },
            text = {
                Column {
                    OutlinedTextField(value = editId, onValueChange = { editId = it }, label = { Text("ID Santri (QR)") })
                    OutlinedTextField(value = editNama, onValueChange = { editNama = it }, label = { Text("Nama") })
                    OutlinedTextField(value = editKelas, onValueChange = { editKelas = it }, label = { Text("Kelas") })
                    OutlinedTextField(value = editAlamat, onValueChange = { editAlamat = it }, label = { Text("Alamat") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.perbaruiJamaah(jamaahToEdit!!.copy(idSantri = editId, nama = editNama, kelas = editKelas, alamat = editAlamat))
                    jamaahToEdit = null
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.hapusJamaah(jamaahToEdit!!)
                    jamaahToEdit = null 
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Hapus") }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Data Jamaah (Sentuh untuk Edit)", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = idSantri, onValueChange = { idSantri = it }, label = { Text("ID Santri (QR Code)") })
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                OutlinedTextField(value = kelas, onValueChange = { kelas = it }, label = { Text("Kelas") })
                OutlinedTextField(value = alamat, onValueChange = { alamat = it }, label = { Text("Alamat") })
                Button(onClick = { 
                    if(nama.isNotBlank() && idSantri.isNotBlank()) {
                        viewModel.tambahJamaah(idSantri, nama, kelas, alamat)
                        idSantri = ""; nama = ""; kelas = ""; alamat = ""
                    }
                }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Tambah Jamaah")
                }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(jamaahList) { jamaah ->
                ListItem(
                    modifier = Modifier.clickable { jamaahToEdit = jamaah },
                    headlineContent = { Text(jamaah.nama) },
                    supportingContent = { Text("ID: ${jamaah.idSantri} | Kelas: ${jamaah.kelas}") }
                )
                Divider()
            }
        }
    }
}

@Composable
fun PengaturanScreen(viewModel: AppViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Pengaturan", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Versi Aplikasi: 2.1 (Continuous Scanner)")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Reset Semua Data")
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Konfirmasi") },
                text = { Text("Apakah Anda yakin ingin mereset semua data? Data dummy akan dimuat ulang.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetData(); showDialog = false }) { Text("Ya, Reset") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
            )
        }
    }
}
