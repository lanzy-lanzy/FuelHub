package dev.ml.fuelhub.data.util

import android.content.Context
import android.os.Environment
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import dev.ml.fuelhub.presentation.viewmodel.ReportFilteredData
import timber.log.Timber
import java.io.File
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility class for generating PDF reports.
 * Handles PDF creation for fuel transaction reports.
 */
class ReportPdfGenerator(private val context: Context) {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val numberFormatter = NumberFormat.getInstance(Locale.US)
    
    init {
        numberFormatter.minimumFractionDigits = 0
        numberFormatter.maximumFractionDigits = 0
    }
    
    private fun formatNumberWithComma(value: Double): String {
        return numberFormatter.format(value.toLong())
    }
    
    private fun formatCurrency(value: Double): String {
        return "PHP ${numberFormatter.format(value.toLong())}.${String.format("%02d", (value % 1 * 100).toInt())}"
    }
    
    /**
     * Generate a report PDF with filtered transaction data.
     */
    fun generateReportPdf(
        reportName: String,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        filteredData: ReportFilteredData
    ): String? {
        return try {
            // Create documents folder if it doesn't exist
            val docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            docDir?.mkdirs()
            
            // Generate filename
            val fileName = "report_${reportName}_${System.currentTimeMillis()}.pdf"
            val filePath = File(docDir, fileName).absolutePath
            
            // Create PDF with Landscape A4 format for better table display
            val writer = PdfWriter(filePath)
            val pdfDoc = PdfDocument(writer)
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate())  // Landscape orientation
            val document = Document(pdfDoc)
            
            // Add logo header
            val logoImage = loadImageFromAssets("mdrrmo.png")
            if (logoImage != null) {
                logoImage.setWidth(60f)
                logoImage.setHeight(60f)
                logoImage.setHorizontalAlignment(HorizontalAlignment.CENTER)
                document.add(logoImage)
            }
            
            // Add title
            document.add(
                Paragraph("Fuel Transaction Report")
                    .setFontSize(24f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            // Add subtitle
            document.add(
                Paragraph("$reportName Report")
                    .setFontSize(14f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            // Add date range
            document.add(
                Paragraph("Period: ${dateFrom.format(dateFormatter)} to ${dateTo.format(dateFormatter)}")
                    .setFontSize(11f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            // Add spacing
            document.add(Paragraph())
            
            // Add summary section
            document.add(
                Paragraph("Summary Statistics")
                    .setFontSize(14f)
                    .setBold()
            )
            
            // Create summary table
            val summaryTable = Table(2)
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Header cells
            summaryTable.addHeaderCell(createHeaderCell("Metric"))
            summaryTable.addHeaderCell(createHeaderCell("Value"))
            
            // Data rows
            summaryTable.addCell(createDataCell("Total Liters"))
            summaryTable.addCell(createDataCell(String.format("%.2f L", filteredData.totalLiters)))
            
            summaryTable.addCell(createDataCell("Total Transactions"))
            summaryTable.addCell(createDataCell(formatNumberWithComma(filteredData.transactionCount.toDouble())))
            
            summaryTable.addCell(createDataCell("Completed"))
            summaryTable.addCell(createDataCell(formatNumberWithComma(filteredData.completedCount.toDouble())))
            
            summaryTable.addCell(createDataCell("Pending"))
            summaryTable.addCell(createDataCell(formatNumberWithComma(filteredData.pendingCount.toDouble())))
            
            summaryTable.addCell(createDataCell("Failed"))
            summaryTable.addCell(createDataCell(formatNumberWithComma(filteredData.failedCount.toDouble())))
            
            summaryTable.addCell(createDataCell("Average Liters/Transaction"))
            summaryTable.addCell(createDataCell(String.format("%.2f L", filteredData.averageLitersPerTransaction)))
            
            summaryTable.addCell(createDataCell("Total Cost"))
            summaryTable.addCell(createDataCell(formatCurrency(filteredData.totalCost)))
            
            summaryTable.addCell(createDataCell("Average Cost per Liter"))
            val avgCostPerLiter = if (filteredData.totalLiters > 0) filteredData.totalCost / filteredData.totalLiters else 0.0
            summaryTable.addCell(createDataCell(formatCurrency(avgCostPerLiter)))
            
            document.add(summaryTable)
            
            // Add spacing
            document.add(Paragraph())
            
            // Add transaction details section
            if (filteredData.transactions.isNotEmpty()) {
                document.add(
                    Paragraph("Transaction Details (showing up to 50 records)")
                        .setFontSize(14f)
                        .setBold()
                )
                
                // Create transactions table with 8 columns (including Date)
                val transTable = Table(8)
                    .setWidth(UnitValue.createPercentValue(100f))
                
                // Headers
                transTable.addHeaderCell(createHeaderCell("Date"))
                transTable.addHeaderCell(createHeaderCell("Reference"))
                transTable.addHeaderCell(createHeaderCell("Driver"))
                transTable.addHeaderCell(createHeaderCell("Vehicle"))
                transTable.addHeaderCell(createHeaderCell("Liters"))
                transTable.addHeaderCell(createHeaderCell("Cost/L"))
                transTable.addHeaderCell(createHeaderCell("Total Cost"))
                transTable.addHeaderCell(createHeaderCell("Status"))
                
                // Data rows
                filteredData.transactions.take(50).forEach { transaction ->
                    // Format date as yyyy-MM-dd
                    val transactionDate = transaction.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    transTable.addCell(createDataCell(transactionDate))
                    transTable.addCell(createDataCell(transaction.referenceNumber))
                    // Use full name if available, otherwise use driver name
                    val driverDisplay = transaction.driverFullName ?: transaction.driverName
                    transTable.addCell(createDataCell(driverDisplay))
                    transTable.addCell(createDataCell(transaction.vehicleType))
                    transTable.addCell(createDataCell(String.format("%.2f", transaction.litersToPump)))
                    transTable.addCell(createDataCell(formatCurrency(transaction.costPerLiter)))
                    transTable.addCell(createDataCell(formatCurrency(transaction.getTotalCost())))
                    transTable.addCell(createDataCell(transaction.status.name))
                }
                
                document.add(transTable)
            }
            
            // Add footer
            document.add(Paragraph())
            document.add(
                Paragraph("Generated on: ${LocalDate.now().format(dateFormatter)}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
            )
            
            // Close document
            document.close()
            
            Timber.d("Report PDF generated successfully: $filePath")
            filePath
        } catch (e: Exception) {
            Timber.e(e, "Error generating report PDF: ${e.message}")
            null
        }
    }
    
    private fun createHeaderCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text).setBold())
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
    }
    
    private fun createDataCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text))
            .setTextAlignment(TextAlignment.LEFT)
    }
    
    private fun loadImageFromAssets(assetName: String): Image? {
        return try {
            val outputDir = context.cacheDir
            val imageFile = File(outputDir, assetName)
            
            // Always copy to ensure we have the latest version from assets
            val inputStream = context.assets.open(assetName)
            inputStream.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            if (imageFile.exists() && imageFile.length() > 0) {
                Image(ImageDataFactory.create(imageFile.absolutePath))
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load image from assets: $assetName")
            null
        }
    }
}
