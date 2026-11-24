package com.absensi.jamaah

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV

/**
 * Daftar teknologi NFC (nama class, format String[][]) untuk kartu RFID 13,56 MHz:
 * ISO14443A (Mifare, NfcA), ISO14443B (NfcB/IsoDep), Felica (NfcF), ISO15693 (NfcV).
 */
fun teknologiNfc(): Array<Array<String>> = arrayOf(
    arrayOf(
        Ndef::class.java.name,
        NfcA::class.java.name,
        NfcB::class.java.name,
        NfcF::class.java.name,
        NfcV::class.java.name,
        IsoDep::class.java.name,
        MifareClassic::class.java.name,
        MifareUltralight::class.java.name
    )
)

/**
 * Mengubah UID kartu menjadi string hex uppercase tanpa spasi, misal: 04A13B2C.
 * UID ini yang dipakai sebagai identitas kartu di Firestore.
 */
fun formatUidNfc(tag: Tag): String =
    tag.id?.joinToString("") { "%02X".format(it) } ?: ""
