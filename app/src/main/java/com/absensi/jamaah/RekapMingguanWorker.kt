package com.absensi.jamaah

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RekapMingguanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = FirebaseFirestore.getInstance()
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val (mulai, akhir) = rentangMinggu(format)

            val snapshot = db.collection("absensi")
                .whereGreaterThanOrEqualTo("tanggal", mulai)
                .whereLessThanOrEqualTo("tanggal", akhir)
                .orderBy("tanggal")
                .get()
                .await()

            val documents = snapshot.documents
            if (documents.isEmpty()) return Result.success()

            val hadir = documents.count { it.getString("status") == "Mengikuti" }
            val ijin = documents.count { it.getString("status") == "Ijin" }
            val telat = documents.count { it.getString("status") == "Telat" }
            val alpha = documents.count { it.getString("status") == "Tidak Mengikuti" }

            val jamaah = db.collection("jamaah").get().await()
            val totalJamaah = jamaah.documents.size

            val isi = "Hadir: $hadir • Ijin: $ijin • Telat: $telat • Alpha: $alpha\n" +
                "Total jamaah terdaftar: $totalJamaah\n" +
                "Periode: $mulai s.d. $akhir"

            NotifikasiHelper.tampilkanRekap(
                context = applicationContext,
                judul = "Rekap Mingguan",
                isi = isi
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun rentangMinggu(format: SimpleDateFormat): Pair<String, String> {
        val now = Calendar.getInstance()
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = (dayOfWeek + 5) % 7

        val start = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -daysFromMonday)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 0)
        }
        return format.format(start.time) to format.format(end.time)
    }
}
