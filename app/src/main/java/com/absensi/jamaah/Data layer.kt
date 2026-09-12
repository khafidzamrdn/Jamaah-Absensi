package com.absensi.jamaah

// Struktur Data untuk Firebase Firestore
data class Jamaah(
    var id: String = "",
    val idSantri: String = "",
    val nama: String = "",
    val kelas: String = "",
    val alamat: String = ""
)

data class Absensi(
    var id: String = "",
    val jamaahId: String = "",
    val tanggal: String = "", 
    val waktuShalat: String = "", 
    val status: String = "", 
    val timestamp: Long = System.currentTimeMillis()
)
