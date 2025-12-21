package dev.ml.fuelhub.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import dev.ml.fuelhub.data.model.GasSlip
import timber.log.Timber
import java.io.File

/**
 * Utility class for managing PDF generation, printing, and sharing.
 * Handles file operations for gas slip PDFs.
 */
class PdfPrintManager(val context: Context) {
    
    private val pdfGenerator = GasSlipPdfGenerator(context)
    
    /**
     * Generate and print a gas slip PDF.
     */
    fun generateAndPrintGasSlip(gasSlip: GasSlip): Boolean {
        return try {
            val pdfPath = pdfGenerator.generateGasSlipPdf(gasSlip)
            printPdf(pdfPath, gasSlip.referenceNumber)
            Timber.d("Gas slip printed successfully: ${gasSlip.referenceNumber}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error generating or printing gas slip")
            false
        }
    }
    
    /**
     * Generate PDF without printing.
     */
    fun generatePdfOnly(gasSlip: GasSlip): String? {
        return try {
            val pdfPath = pdfGenerator.generateGasSlipPdf(gasSlip)
            Timber.d("PDF generated successfully: $pdfPath")
            pdfPath
        } catch (e: Exception) {
            Timber.e(e, "Error generating PDF")
            null
        }
    }
    
    /**
     * Print an existing PDF file.
     */
    private fun printPdf(filePath: String, jobName: String) {
        try {
            val file = File(filePath)
            
            // Open PDF with system's default PDF viewer which has print capability
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Open PDF viewer with chooser dialog
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    },
                    "Open PDF to Print"
                )
            )
            Timber.d("PDF viewer opened for printing: $jobName")
        } catch (e: Exception) {
            Timber.e(e, "Error opening PDF viewer")
        }
    }
    
    /**
     * Open PDF file in a viewer application.
     */
    fun openPdfInViewer(filePath: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            context.startActivity(Intent.createChooser(intent, "Open PDF"))
            Timber.d("Opening PDF file: $filePath")
        } catch (e: Exception) {
            Timber.e(e, "Error opening PDF file")
        }
    }
    
    /**
     * Share PDF file via email or other apps.
     */
    fun sharePdfFile(filePath: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Gas Slip - ${file.nameWithoutExtension}")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Gas Slip"))
            Timber.d("Sharing PDF file: $filePath")
        } catch (e: Exception) {
            Timber.e(e, "Error sharing PDF file")
        }
    }
    
    /**
     * Get all generated PDF files from documents folder.
     */
    fun getAllGeneratedPdfs(): List<File> {
        return try {
            val docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            docDir?.listFiles { file ->
                file.isFile && file.extension == "pdf" && file.name.startsWith("gas_slip_")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error getting generated PDFs")
            emptyList()
        }
    }
    
    /**
     * Delete a PDF file.
     */
    fun deletePdfFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Timber.d("PDF file deleted: $filePath")
                } else {
                    Timber.w("Failed to delete PDF file: $filePath")
                }
                deleted
            } else {
                Timber.w("PDF file not found: $filePath")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting PDF file")
            false
        }
    }
    
    /**
     * Get PDF file size in MB.
     */
    fun getPdfFileSizeMb(filePath: String): Double {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.length() / (1024.0 * 1024.0)
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
    
    /**
     * Check if PDF file exists.
     */
    fun pdfFileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }
}
