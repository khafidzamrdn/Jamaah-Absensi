package com.absensi.jamaah

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val jamaahList: StateFlow<List<Jamaah>> = dao.getAllJamaah()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val absensiList: StateFlow<List<Absensi>> = dao.getAllAbsensi()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun tambahJamaah(idSantri: String, nama: String, kelas: String, alamat: String) {
        viewModelScope.launch {
            dao.insertJamaah(Jamaah(idSantri = idSantri, nama = nama, kelas = kelas, alamat = alamat))
        }
    }

    fun perbaruiJamaah(jamaah: Jamaah) {
        viewModelScope.launch { dao.updateJamaah(jamaah) }
    }

    fun hapusJamaah(jamaah: Jamaah) {
        viewModelScope.launch { dao.deleteJamaah(jamaah) }
    }

    suspend fun cekAbsensiAda(tanggal: String, waktu: String): Boolean {
        return dao.getAbsensiByTanggalWaktu(tanggal, waktu).isNotEmpty()
    }

    fun simpanAbsensi(absensi: List<Absensi>) {
        viewModelScope.launch { dao.insertAbsensiList(absensi) }
    }

    fun perbaruiAbsensi(absensi: Absensi) {
        viewModelScope.launch { dao.updateAbsensi(absensi) }
    }

    suspend fun siapkanAbsensiScan(tanggal: String, waktu: String) {
        val sudahAda = dao.getAbsensiByTanggalWaktu(tanggal, waktu)
        if (sudahAda.isEmpty()) {
            val dataToSave = jamaahList.value.map {
                Absensi(jamaahId = it.id, tanggal = tanggal, waktuShalat = waktu, status = "Tidak Mengikuti")
            }
            dao.insertAbsensiList(dataToSave)
        }
    }

    // UPDATE: Penambahan fitur deteksi jika santri sudah absen
    suspend fun prosesScanQr(idHasilScan: String, tanggal: String, waktu: String): String {
        val jamaah = jamaahList.value.find { it.idSantri == idHasilScan }
        if (jamaah == null) return "❌ QR '$idHasilScan' tidak terdaftar."

        val absensiSesi = dao.getAbsensiByTanggalWaktu(tanggal, waktu)
        val absenJamaah = absensiSesi.find { it.jamaahId == jamaah.id }

        return if (absenJamaah != null) {
            if (absenJamaah.status == "Mengikuti") {
                "⚠️ ${jamaah.nama} SUDAH absen."
            } else {
                dao.updateAbsensi(absenJamaah.copy(status = "Mengikuti"))
                "✅ ${jamaah.nama} HADIR!"
            }
        } else {
            "❌ Sesi belum disiapkan."
        }
    }

    fun resetData() {
        viewModelScope.launch {
            dao.clearAbsensi()
            dao.clearJamaah()
            val dummies = listOf(
                Jamaah(idSantri = "QR001", nama = "Ahmad", kelas = "A", alamat = "Pondok 1"),
                Jamaah(idSantri = "QR002", nama = "Muhammad Rizky", kelas = "A", alamat = "Pondok 2"),
                Jamaah(idSantri = "QR003", nama = "Fajar", kelas = "B", alamat = "Pondok 1"),
                Jamaah(idSantri = "QR004", nama = "Ilham", kelas = "C", alamat = "Pondok 3"),
                Jamaah(idSantri = "QR005", nama = "Bagas", kelas = "B", alamat = "Pondok 2")
            )
            dummies.forEach { dao.insertJamaah(it) }
        }
    }
}
