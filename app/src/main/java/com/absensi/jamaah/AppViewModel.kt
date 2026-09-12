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

    fun tambahJamaah(nama: String, kelas: String, alamat: String) {
        viewModelScope.launch {
            dao.insertJamaah(Jamaah(nama = nama, kelas = kelas, alamat = alamat))
        }
    }

    fun hapusJamaah(jamaah: Jamaah) {
        viewModelScope.launch {
            dao.deleteJamaah(jamaah)
        }
    }

    suspend fun cekAbsensiAda(tanggal: String, waktu: String): Boolean {
        return dao.getAbsensiByTanggalWaktu(tanggal, waktu).isNotEmpty()
    }

    fun simpanAbsensi(absensi: List<Absensi>) {
        viewModelScope.launch {
            dao.insertAbsensiList(absensi)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            dao.clearAbsensi()
            dao.clearJamaah()
            // Re-insert dummy
            val dummies = listOf(
                Jamaah(nama = "Ahmad", kelas = "A", alamat = "Pondok 1"),
                Jamaah(nama = "Muhammad Rizky", kelas = "A", alamat = "Pondok 2"),
                Jamaah(nama = "Fajar", kelas = "B", alamat = "Pondok 1"),
                Jamaah(nama = "Ilham", kelas = "C", alamat = "Pondok 3"),
                Jamaah(nama = "Bagas", kelas = "B", alamat = "Pondok 2")
            )
            dummies.forEach { dao.insertJamaah(it) }
        }
    }
}
