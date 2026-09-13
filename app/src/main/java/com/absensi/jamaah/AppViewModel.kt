package com.absensi.jamaah

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()

    private val _jamaahList = MutableStateFlow<List<Jamaah>>(emptyList())
    val jamaahList: StateFlow<List<Jamaah>> = _jamaahList

    private val _absensiList = MutableStateFlow<List<Absensi>>(emptyList())
    val absensiList: StateFlow<List<Absensi>> = _absensiList

    init {
        muatDataRealtime()
    }

    private fun muatDataRealtime() {
        try {
            db.collection("jamaah").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _jamaahList.value = snapshot.documents.map { doc ->
                        Jamaah(
                            id = doc.id,
                            idSantri = doc.getString("idSantri") ?: "",
                            nama = doc.getString("nama") ?: "",
                            kelas = doc.getString("kelas") ?: "",
                            alamat = doc.getString("alamat") ?: ""
                        )
                    }.sortedBy { it.nama }
                }
            }

            db.collection("absensi").addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _absensiList.value = snapshot.documents.map { doc ->
                        Absensi(
                            id = doc.id,
                            jamaahId = doc.getString("jamaahId") ?: "",
                            tanggal = doc.getString("tanggal") ?: "",
                            waktuShalat = doc.getString("waktuShalat") ?: "",
                            status = doc.getString("status") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun tambahJamaah(idSantri: String, nama: String, kelas: String, alamat: String) {
        viewModelScope.launch {
            try {
                val data = mapOf("idSantri" to idSantri, "nama" to nama, "kelas" to kelas, "alamat" to alamat)
                db.collection("jamaah").add(data).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun perbaruiJamaah(jamaah: Jamaah) {
        viewModelScope.launch {
            try {
                val data = mapOf("idSantri" to jamaah.idSantri, "nama" to jamaah.nama, "kelas" to jamaah.kelas, "alamat" to jamaah.alamat)
                db.collection("jamaah").document(jamaah.id).set(data).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun hapusJamaah(jamaah: Jamaah) {
        viewModelScope.launch {
            try {
                db.collection("jamaah").document(jamaah.id).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun cekAbsensiAda(tanggal: String, waktu: String): Boolean {
        return try {
            val query = db.collection("absensi")
                .whereEqualTo("tanggal", tanggal)
                .whereEqualTo("waktuShalat", waktu)
                .get()
                .await()
            !query.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    fun simpanAbsensi(absensiListInput: List<Absensi>) {
        viewModelScope.launch {
            try {
                val batch = db.batch()
                absensiListInput.forEach { absen ->
                    val ref = db.collection("absensi").document()
                    val data = mapOf(
                        "jamaahId" to absen.jamaahId,
                        "tanggal" to absen.tanggal,
                        "waktuShalat" to absen.waktuShalat,
                        "status" to absen.status,
                        "timestamp" to absen.timestamp
                    )
                    batch.set(ref, data)
                }
                batch.commit().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun perbaruiAbsensi(absensi: Absensi) {
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "jamaahId" to absensi.jamaahId,
                    "tanggal" to absensi.tanggal,
                    "waktuShalat" to absensi.waktuShalat,
                    "status" to absensi.status,
                    "timestamp" to absensi.timestamp
                )
                db.collection("absensi").document(absensi.id).set(data).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun siapkanAbsensiScan(tanggal: String, waktu: String) {
        try {
            val query = db.collection("absensi")
                .whereEqualTo("tanggal", tanggal)
                .whereEqualTo("waktuShalat", waktu)
                .get()
                .await()

            if (query.isEmpty) {
                val listJamaah = jamaahList.value
                val batch = db.batch()
                listJamaah.forEach { jamaah ->
                    val ref = db.collection("absensi").document()
                    val data = mapOf(
                        "jamaahId" to jamaah.id,
                        "tanggal" to tanggal,
                        "waktuShalat" to waktu,
                        "status" to "Tidak Mengikuti",
                        "timestamp" to System.currentTimeMillis()
                    )
                    batch.set(ref, data)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun prosesScanQr(idHasilScan: String, tanggal: String, waktu: String): String {
        return try {
            val jamaah = jamaahList.value.find { it.idSantri == idHasilScan }
                ?: return "❌ QR '$idHasilScan' tidak terdaftar."

            val query = db.collection("absensi")
                .whereEqualTo("tanggal", tanggal)
                .whereEqualTo("waktuShalat", waktu)
                .whereEqualTo("jamaahId", jamaah.id)
                .get()
                .await()

            if (query.isEmpty) return "❌ Sesi belum disiapkan."

            val doc = query.documents[0]
            val currentStatus = doc.getString("status")

            if (currentStatus == "Mengikuti") {
                "⚠️ ${jamaah.nama} SUDAH absen."
            } else {
                doc.reference.update("status", "Mengikuti").await()
                "✅ ${jamaah.nama} HADIR!"
            }
        } catch (e: Exception) {
            "❌ Terjadi kesalahan koneksi."
        }
    }

    fun resetData() {
        viewModelScope.launch {
            try {
                val jamaahDocs = db.collection("jamaah").get().await()
                for (doc in jamaahDocs) { doc.reference.delete().await() }

                val absensiDocs = db.collection("absensi").get().await()
                for (doc in absensiDocs) { doc.reference.delete().await() }

                val dummies = listOf(
                    Jamaah(idSantri = "QR001", nama = "Ahmad", kelas = "A", alamat = "Pondok 1"),
                    Jamaah(idSantri = "QR002", nama = "Muhammad Rizky", kelas = "A", alamat = "Pondok 2"),
                    Jamaah(idSantri = "QR003", nama = "Fajar", kelas = "B", alamat = "Pondok 1"),
                    Jamaah(idSantri = "QR004", nama = "Ilham", kelas = "C", alamat = "Pondok 3"),
                    Jamaah(idSantri = "QR005", nama = "Bagas", kelas = "B", alamat = "Pondok 2")
                )
                dummies.forEach { 
                    db.collection("jamaah").add(mapOf("idSantri" to it.idSantri, "nama" to it.nama, "kelas" to it.kelas, "alamat" to it.alamat)).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
