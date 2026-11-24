package com.absensi.jamaah

import android.nfc.Tag

/**
 * Mengubah UID kartu menjadi string hex uppercase tanpa spasi, misal: 04A13B2C.
 * UID ini yang dipakai sebagai identitas kartu di Firestore.
 *
 * Berlaku untuk kartu RFID 13,56 MHz (ISO14443A/B, Mifare, Felica, ISO15693).
 * Kartu 125 kHz tidak dapat dibaca oleh NFC HP Android.
 */
fun formatUidNfc(tag: Tag): String =
    tag.id?.joinToString("") { "%02X".format(it) } ?: ""
