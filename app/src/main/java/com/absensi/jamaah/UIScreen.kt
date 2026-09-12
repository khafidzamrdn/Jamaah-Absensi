@file:OptIn(ExperimentalMaterial3Api::class)
package com.absensi.jamaah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        Text("Form Absensi", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = tanggal, onValueChange = { tanggal = it }, label = { Text("Tanggal (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            waktuList.forEach { w ->
                FilterChip(selected = waktu == w, onClick = { waktu = w }, label = { Text(w) })
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        } else if (successMessage.isNotEmpty()) {
            Text(successMessage, color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(jamaahList) { jamaah ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("${jamaah.nama} (${jamaah.kelas})", style = MaterialTheme.typography.titleMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("Mengikuti", "Ijin", "Tidak Mengikuti", "Telat").forEach { status ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = absensiState[jamaah.id] == status,
                                            onClick = { absensiState[jamaah.id] = status }
                                        )
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
fun RekapScreen(viewModel: AppViewModel) {
    val absensi = viewModel.absensiList.collectAsState().value
    val jamaah = viewModel.jamaahList.collectAsState().value
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Harian", "Statistik (Total)")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (selectedTab == 0) {
            RekapHarian(absensi, jamaah)
        } else {
            RekapStatistik(absensi, jamaah)
        }
    }
}

@Composable
fun RekapHarian(absensi: List<Absensi>, jamaah: List<Jamaah>) {
    var tanggal by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        OutlinedTextField(
            value = tanggal,
            onValueChange = { tanggal = it },
            label = { Text("Filter Tanggal (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        val filteredAbsensi = absensi.filter { it.tanggal == tanggal }

        if (filteredAbsensi.isEmpty()) {
            Text("Belum ada data absensi pada tanggal ini.", modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(jamaah) { j ->
                    val dataJamaah = filteredAbsensi.filter { it.jamaahId == j.id }
                    // Ambil inisial status, contoh: "Mengikuti" -> "M", "Telat" -> "T", "Ijin" -> "I", "Tidak Mengikuti" -> "X"
                    fun getInitial(waktu: String): String {
                        return when(dataJamaah.find { it.waktuShalat == waktu }?.status) {
                            "Mengikuti" -> "Hadir"
                            "Ijin" -> "Ijin"
                            "Telat" -> "Telat"
                            "Tidak Mengikuti" -> "Alpa"
                            else -> "-"
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(j.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Subuh: ${getInitial("Subuh")} | Dzuhur: ${getInitial("Dzuhur")} | Ashar: ${getInitial("Ashar")}", style = MaterialTheme.typography.bodySmall)
                            Text("Maghrib: ${getInitial("Maghrib")} | Isya: ${getInitial("Isya")}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RekapStatistik(absensi: List<Absensi>, jamaah: List<Jamaah>) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Statistik Kehadiran Jamaah", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (absensi.isEmpty()) {
            Text("Belum ada data absensi.")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(jamaah) { j ->
                    val dataJamaah = absensi.filter { it.jamaahId == j.id }
                    val total = dataJamaah.size
                    val mengikuti = dataJamaah.count { it.status == "Mengikuti" }
                    val telat = dataJamaah.count { it.status == "Telat" }
                    val ijin = dataJamaah.count { it.status == "Ijin" }
                    val tidakMengikuti = dataJamaah.count { it.status == "Tidak Mengikuti" }
                    
                    val persentase = if (total > 0) ((mengikuti + telat).toFloat() / total * 100).toInt() else 0

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(j.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("$persentase%", color = if (persentase >= 75) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Hadir: $mengikuti | Telat: $telat | Ijin: $ijin | Alpa: $tidakMengikuti", style = MaterialTheme.typography.bodySmall)
                            Text("Total Tercatat: $total shalat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
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
    var nama by remember { mutableStateOf("") }
    var kelas by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Data Jamaah", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
                OutlinedTextField(value = kelas, onValueChange = { kelas = it }, label = { Text("Kelas") })
                OutlinedTextField(value = alamat, onValueChange = { alamat = it }, label = { Text("Alamat") })
                Button(onClick = { 
                    if(nama.isNotBlank() && kelas.isNotBlank()) {
                        viewModel.tambahJamaah(nama, kelas, alamat)
                        nama = ""; kelas = ""; alamat = ""
                    }
                }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Tambah Jamaah")
                }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(jamaahList) { jamaah ->
                ListItem(
                    headlineContent = { Text(jamaah.nama) },
                    supportingContent = { Text("Kelas: ${jamaah.kelas} | ${jamaah.alamat}") },
                    trailingContent = {
                        Button(onClick = { viewModel.hapusJamaah(jamaah) }) { Text("Hapus") }
                    }
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
        Text("Versi Aplikasi: 1.0.1 (Update Rekap)")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Reset Semua Data")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Konfirmasi") },
                text = { Text("Apakah Anda yakin ingin mereset semua data? (Termasuk data absensi dan jamaah). Data dummy akan dimuat ulang.") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.resetData()
                        showDialog = false 
                    }) { Text("Ya, Reset") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Batal") }
                }
            )
        }
    }
}
