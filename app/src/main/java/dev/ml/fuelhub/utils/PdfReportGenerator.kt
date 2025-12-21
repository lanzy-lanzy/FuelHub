package dev.ml.fuelhub.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.presentation.viewmodel.ReportFilteredData
import dev.ml.fuelhub.presentation.viewmodel.ReportFilterState
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfReportGenerator {

    private const val REPORTS_DIR = "FuelHubReports"
    
    fun generateReport(
        context: Context,
        filteredData: ReportFilteredData,
        filterState: ReportFilterState,
        reportTitle: String = "Fuel Transaction Report"
    ): String? {
        return try {
            val file = createReportFile(context)
            
            val writer = PdfWriter(file)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)
            val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            
            // Header
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
            headerTable.setWidth(UnitValue.createPercentValue(100f))
            headerTable.addCell(
                Cell().add(
                    Paragraph(reportTitle)
                        .setFont(boldFont)
                        .setFontSize(20f)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10f)
                )
            )
            headerTable.addCell(
                Cell().add(
                    Paragraph("Generated: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
                        .setFont(font)
                        .setFontSize(10f)
                        .setTextAlignment(TextAlignment.CENTER)
                )
            )
            document.add(headerTable)
            document.add(Paragraph())
            
            // Filter Summary
            document.add(
                Paragraph("Filter Summary")
                    .setFont(boldFont)
                    .setFontSize(12f)
                    .setMarginBottom(10f)
            )
            val filterTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            filterTable.setWidth(UnitValue.createPercentValue(100f))
            
            addFilterRow(filterTable, "Date Range:", "${filterState.dateFrom} to ${filterState.dateTo}", font, boldFont)
            addFilterRow(filterTable, "Fuel Types:", filterState.selectedFuelTypes.joinToString(", "), font, boldFont)
            addFilterRow(filterTable, "Status Filter:", filterState.selectedStatuses.joinToString(", "), font, boldFont)
            addFilterRow(filterTable, "Vehicle Count:", filterState.selectedVehicles.size.toString(), font, boldFont)
            addFilterRow(filterTable, "Driver Count:", filterState.selectedDrivers.size.toString(), font, boldFont)
            addFilterRow(filterTable, "Liter Range:", "${filterState.minLiters} - ${filterState.maxLiters}", font, boldFont)
            
            document.add(filterTable)
            document.add(Paragraph())
            
            // Summary Statistics
            document.add(
                Paragraph("Summary Statistics")
                    .setFont(boldFont)
                    .setFontSize(12f)
                    .setMarginBottom(10f)
            )
            val statsTable = Table(UnitValue.createPercentArray(floatArrayOf(25f, 25f, 25f, 25f)))
            statsTable.setWidth(UnitValue.createPercentValue(100f))
            
            statsTable.addCell(createHeaderCell("Total Liters", boldFont))
            statsTable.addCell(createHeaderCell("Total Transactions", boldFont))
            statsTable.addCell(createHeaderCell("Completed", boldFont))
            statsTable.addCell(createHeaderCell("Pending", boldFont))
            
            statsTable.addCell(Cell().add(Paragraph("${String.format("%.2f", filteredData.totalLiters)} L").setFont(font)))
            statsTable.addCell(Cell().add(Paragraph(filteredData.transactionCount.toString()).setFont(font)))
            statsTable.addCell(Cell().add(Paragraph(filteredData.completedCount.toString()).setFont(font)))
            statsTable.addCell(Cell().add(Paragraph(filteredData.pendingCount.toString()).setFont(font)))
            
            document.add(statsTable)
            document.add(Paragraph())
            
            // Detailed Transactions
            if (filteredData.transactions.isNotEmpty()) {
                document.add(
                    Paragraph("Detailed Transactions (${filteredData.transactions.size} records)")
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setMarginBottom(10f)
                )
                
                val transactionTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 12f, 12f, 10f, 12f, 10f, 14f, 20f)))
                transactionTable.setWidth(UnitValue.createPercentValue(100f))
                
                // Headers
                transactionTable.addCell(createHeaderCell("Ref #", boldFont))
                transactionTable.addCell(createHeaderCell("Vehicle", boldFont))
                transactionTable.addCell(createHeaderCell("Driver", boldFont))
                transactionTable.addCell(createHeaderCell("Fuel Type", boldFont))
                transactionTable.addCell(createHeaderCell("Liters", boldFont))
                transactionTable.addCell(createHeaderCell("Status", boldFont))
                transactionTable.addCell(createHeaderCell("Date", boldFont))
                transactionTable.addCell(createHeaderCell("Destination", boldFont))
                
                // Data rows (limited to first 1000 for performance)
                filteredData.transactions.take(1000).forEach { transaction ->
                    transactionTable.addCell(Cell().add(Paragraph(transaction.referenceNumber).setFont(font).setFontSize(8f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.vehicleId).setFont(font).setFontSize(8f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.driverName).setFont(font).setFontSize(8f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.fuelType.name).setFont(font).setFontSize(8f)))
                    transactionTable.addCell(Cell().add(Paragraph(String.format("%.2f", transaction.litersToPump)).setFont(font).setFontSize(8f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.status.name).setFont(font).setFontSize(8f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).setFont(font).setFontSize(8f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.destination).setFont(font).setFontSize(8f)))
                }
                
                document.add(transactionTable)
            }
            
            document.close()
            Timber.d("PDF Report generated: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error generating PDF report")
            null
        }
    }

    fun generateDailyPdfReport(
        context: Context,
        transactions: List<FuelTransaction>,
        date: String,
        totalLiters: Double,
        totalTransactions: Int,
        completed: Int,
        pending: Int,
        failed: Int
    ): String? {
        return try {
            val file = createReportFile(context, "Daily_Report_$date.pdf")
            
            val writer = PdfWriter(file)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)
            val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            
            // Header
            document.add(
                Paragraph("Daily Fuel Transaction Report")
                    .setFont(boldFont)
                    .setFontSize(20f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5f)
            )
            document.add(
                Paragraph("Date: $date")
                    .setFont(font)
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
            )
            
            // Summary Stats
            val statsTable = Table(UnitValue.createPercentArray(floatArrayOf(20f, 20f, 20f, 20f, 20f)))
            statsTable.setWidth(UnitValue.createPercentValue(100f))
            
            statsTable.addCell(createHeaderCell("Total Liters", boldFont))
            statsTable.addCell(createHeaderCell("Transactions", boldFont))
            statsTable.addCell(createHeaderCell("Completed", boldFont))
            statsTable.addCell(createHeaderCell("Pending", boldFont))
            statsTable.addCell(createHeaderCell("Failed", boldFont))
            
            statsTable.addCell(Cell().add(Paragraph(String.format("%.2f L", totalLiters)).setFont(font)))
            statsTable.addCell(Cell().add(Paragraph(totalTransactions.toString()).setFont(font)))
            statsTable.addCell(Cell().add(Paragraph(completed.toString()).setFont(font)))
            statsTable.addCell(Cell().add(Paragraph(pending.toString()).setFont(font)))
            statsTable.addCell(Cell().add(Paragraph(failed.toString()).setFont(font)))
            
            document.add(statsTable)
            document.add(Paragraph())
            
            // Transactions detail
            if (transactions.isNotEmpty()) {
                document.add(
                    Paragraph("Transaction Details")
                        .setFont(boldFont)
                        .setFontSize(12f)
                        .setMarginBottom(10f)
                )
                
                val transactionTable = Table(UnitValue.createPercentArray(floatArrayOf(15f, 15f, 15f, 15f, 15f, 25f)))
                transactionTable.setWidth(UnitValue.createPercentValue(100f))
                
                transactionTable.addCell(createHeaderCell("Reference", boldFont))
                transactionTable.addCell(createHeaderCell("Vehicle", boldFont))
                transactionTable.addCell(createHeaderCell("Driver", boldFont))
                transactionTable.addCell(createHeaderCell("Fuel Type", boldFont))
                transactionTable.addCell(createHeaderCell("Liters", boldFont))
                transactionTable.addCell(createHeaderCell("Status", boldFont))
                
                transactions.forEach { transaction ->
                    transactionTable.addCell(Cell().add(Paragraph(transaction.referenceNumber).setFont(font).setFontSize(9f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.vehicleId).setFont(font).setFontSize(9f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.driverName).setFont(font).setFontSize(9f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.fuelType.name).setFont(font).setFontSize(9f)))
                    transactionTable.addCell(Cell().add(Paragraph(String.format("%.2f", transaction.litersToPump)).setFont(font).setFontSize(9f)))
                    transactionTable.addCell(Cell().add(Paragraph(transaction.status.name).setFont(font).setFontSize(9f)))
                }
                
                document.add(transactionTable)
            }
            
            document.close()
            Timber.d("Daily PDF generated: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error generating daily PDF")
            null
        }
    }

    private fun createReportFile(context: Context, filename: String = "Report_${System.currentTimeMillis()}.pdf"): File {
        val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), REPORTS_DIR)
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        return File(reportsDir, filename)
    }

    private fun createHeaderCell(text: String, font: com.itextpdf.kernel.font.PdfFont): Cell {
        return Cell().add(
            Paragraph(text)
                .setFont(font)
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
        ).setBackgroundColor(com.itextpdf.kernel.colors.DeviceRgb(200, 200, 200))
    }

    private fun addFilterRow(
        table: Table,
        label: String,
        value: String,
        font: com.itextpdf.kernel.font.PdfFont,
        boldFont: com.itextpdf.kernel.font.PdfFont
    ) {
        table.addCell(Cell().add(Paragraph(label).setFont(boldFont).setFontSize(10f)))
        table.addCell(Cell().add(Paragraph(value).setFont(font).setFontSize(10f)))
    }
}
