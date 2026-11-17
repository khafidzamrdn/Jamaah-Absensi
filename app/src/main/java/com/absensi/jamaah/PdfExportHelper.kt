package com.absensi.jamaah

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Menghasilkan file PDF laporan absensi mingguan dan membuka dialog bagikan/simpan Android.
 */
fun exportWeeklyReportPdf(
    context: Context,
    weekLabel: String,
    kategori: String,
    ranked: List<Triple<Jamaah, List<Absensi>, Int>>
) {
    if (ranked.isEmpty()) {
        Toast.makeText(context, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
        return
    }

    val pdfDocument = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 40f

    val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subtitlePaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 11f
    }
    val headerPaint = Paint().apply {
        color = Color.BLACK
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val bodyPaint = Paint().apply {
        color = Color.BLACK
        textSize = 10.5f
    }
    val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
    }

    val printDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

    var pageNumber = 1
    var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas: Canvas = page.canvas
    var y = 50f

    fun drawTableHeader() {
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 16f
        canvas.drawText("No", margin, y, headerPaint)
        canvas.drawText("Nama Jamaah", margin + 30f, y, headerPaint)
        canvas.drawText("Hadir", margin + 250f, y, headerPaint)
        canvas.drawText("Ijin", margin + 305f, y, headerPaint)
        canvas.drawText("Telat", margin + 350f, y, headerPaint)
        canvas.drawText("Alpha", margin + 400f, y, headerPaint)
        canvas.drawText("Poin", margin + 455f, y, headerPaint)
        y += 8f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 16f
    }

    canvas.drawText("Laporan Absensi Mingguan Jamaah", margin, y, titlePaint)
    y += 20f
    canvas.drawText("$weekLabel  •  Kategori: $kategori", margin, y, subtitlePaint)
    y += 16f
    canvas.drawText("Dicetak: $printDate", margin, y, subtitlePaint)
    y += 16f
    drawTableHeader()

    ranked.forEachIndexed { index, (jamaah, data, point) ->
        if (y > pageHeight - 60f) {
            pdfDocument.finishPage(page)
            pageNumber++
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = 50f
            drawTableHeader()
        }

        val hadir = data.count { it.status == "Mengikuti" }
        val ijin = data.count { it.status == "Ijin" }
        val telat = data.count { it.status == "Telat" }
        val alpha = data.count { it.status == "Tidak Mengikuti" }
        val nama = if (jamaah.nama.length > 28) jamaah.nama.take(27) + "…" else jamaah.nama

        canvas.drawText("${index + 1}", margin, y, bodyPaint)
        canvas.drawText(nama, margin + 30f, y, bodyPaint)
        canvas.drawText("$hadir", margin + 255f, y, bodyPaint)
        canvas.drawText("$ijin", margin + 308f, y, bodyPaint)
        canvas.drawText("$telat", margin + 355f, y, bodyPaint)
        canvas.drawText("$alpha", margin + 405f, y, bodyPaint)
        canvas.drawText("$point", margin + 458f, y, bodyPaint)
        y += 18f
    }

    pdfDocument.finishPage(page)

    try {
        val safeWeek = weekLabel.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val file = File(context.cacheDir, "Laporan_${kategori}_$safeWeek.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Absensi $weekLabel ($kategori)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Ekspor Laporan PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}
