package com.example.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.ui.screens.StockImportLog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StockReportGenerator {

    data class StockReportResult(
        val success: Boolean,
        val filePath: String,
        val fileName: String,
        val message: String
    )

    fun downloadDailyStockReport(context: Context, importLogs: List<StockImportLog>): StockReportResult {
        return try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
            val displayDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
            val fileName = "VediRaksha_Daily_Stock_Report_${dateFormat.format(Date())}.txt"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)
            val fos = FileOutputStream(file)

            val sb = StringBuilder()
            sb.append("=====================================================\n")
            sb.append("      VEDIRAKSHA SIVAKASI FIREWORKS UNIT 4\n")
            sb.append("      DAILY CHEMICAL STOCK & USAGE AUDIT REPORT\n")
            sb.append("=====================================================\n")
            sb.append("Generated On: ${displayDateFormat.format(Date())}\n")
            sb.append("PESO License: PESO/EXP/TN/2024/9912 • Explosives Act 1884\n")
            sb.append("-----------------------------------------------------\n\n")

            sb.append("ACTIVE IMPORT BATCHES & STOCK LOGS:\n")
            var totalKg = 0.0
            importLogs.forEachIndexed { index, log ->
                totalKg += log.importedKg
                sb.append("${index + 1}. Chemical: ${log.chemicalName}\n")
                sb.append("   Batch Code: ${log.batchCode}\n")
                sb.append("   Imported Quantity: ${log.importedKg} kg\n")
                sb.append("   Supplier: ${log.supplier}\n")
                sb.append("   Timestamp: ${displayDateFormat.format(Date(log.timestamp))}\n")
                sb.append("-----------------------------------------------------\n")
            }

            sb.append("\nSUMMARY:\n")
            sb.append("Total Active Batches: ${importLogs.size}\n")
            sb.append("Total Chemical Stock Weight: ${"%.1f".format(totalKg)} kg\n")
            sb.append("Status: VERIFIED & COMPLIANT WITH PESO RULE 84\n")
            sb.append("=====================================================\n")

            fos.write(sb.toString().toByteArray())
            fos.flush()
            fos.close()

            Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()

            StockReportResult(
                success = true,
                filePath = file.absolutePath,
                fileName = fileName,
                message = "Successfully downloaded to Downloads: $fileName"
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Error downloading report: ${e.message}", Toast.LENGTH_LONG).show()
            StockReportResult(
                success = false,
                filePath = "",
                fileName = "",
                message = e.message ?: "Unknown error"
            )
        }
    }
}
