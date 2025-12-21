package dev.ml.fuelhub.data.util

import android.content.Context
import android.os.Environment
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import dev.ml.fuelhub.data.model.GasSlip
import timber.log.Timber
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Utility class for generating PDF gas slips.
 * Creates printable documents with all transaction details.
 */
class GasSlipPdfGenerator(private val context: Context) {
    
    /**
     * Generate a PDF gas slip and return the file path.
     */
    fun generateGasSlipPdf(gasSlip: GasSlip): String {
        val fileName = "gas_slip_${gasSlip.referenceNumber}.pdf"
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )
        
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        // 3.5x5 inch index card size in points (72 DPI)
        // Width: 3.5 * 72 = 252
        // Height: 5.0 * 72 = 360
        val receiptPageSize = PageSize(252f, 360f)
        val document = Document(pdfDocument, receiptPageSize)
        
        try {
            // Set margins for receipt (minimal margins to maximize space)
            document.setMargins(10f, 10f, 10f, 10f)
            
            val titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            
            // ═══ HEADER ═══
            // MDRRMO Logo
            val logoImage = loadImageFromAssets("mdrrmo.png")
            if (logoImage != null) {
                logoImage.setWidth(40f)
                logoImage.setHeight(40f)
                logoImage.setHorizontalAlignment(HorizontalAlignment.CENTER)
                document.add(logoImage)
            }
            
            // Title
            val title = Paragraph("FUEL DISPENSING SLIP")
                .setFont(titleFont)
                .setFontSize(11f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0f)
                .setMarginTop(2f)
            document.add(title)
            
            // Reference Number (Subtle)
            val refPara = Paragraph("REF: ${gasSlip.referenceNumber.uppercase()}")
                .setFont(regularFont)
                .setFontSize(7f)
                .setFontColor(DeviceGray.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8f)
            document.add(refPara)

            // ═══ FUEL ALLOCATION (Key Info - Boxed/Highlighted) ═══
            val fuelTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setMarginBottom(8f)

            val fuelCell = Cell()
                .setBorder(SolidBorder(DeviceGray.BLACK, 1f))
                .setPadding(6f)
                .setBackgroundColor(DeviceGray(0.95f)) // Light gray background
                
            fuelCell.add(Paragraph("FUEL ALLOCATION")
                .setFont(titleFont)
                .setFontSize(8f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4f))
            
            val fuelDetailsTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // Large Fuel Type
            fuelDetailsTable.addCell(Cell().add(Paragraph(gasSlip.fuelType.name.uppercase())
                .setFont(titleFont)
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER))
                .setBorder(null))
                
            // Large Liters
            fuelDetailsTable.addCell(Cell().add(Paragraph("${gasSlip.litersToPump}L")
                .setFont(titleFont)
                .setFontSize(14f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER))
                .setBorder(null))
                
            fuelCell.add(fuelDetailsTable)
            fuelTable.addCell(fuelCell)
            document.add(fuelTable)

            // ═══ DETAILS GRID ═══
            val detailsTable = Table(UnitValue.createPercentArray(floatArrayOf(0.8f, 2f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setMarginBottom(8f)
            
            // Rows
            addDetailRow(detailsTable, "Plate:", gasSlip.vehiclePlateNumber.uppercase(), true)
            addDetailRow(detailsTable, "Vehicle:", gasSlip.vehicleType.uppercase(), false)
            
            // Display full name if available, otherwise fall back to driver name
            val displayDriverName = gasSlip.driverFullName?.uppercase() ?: gasSlip.driverName.uppercase()
            addDetailRow(detailsTable, "Driver:", displayDriverName, true)
            
            addDetailRow(detailsTable, "Purpose:", gasSlip.tripPurpose.uppercase(), false)
            addDetailRow(detailsTable, "Destination:", gasSlip.destination.uppercase(), true)
            
            document.add(detailsTable)
            
            // ═══ SIGNATURE SECTION ═══
            // Push to bottom using fixed positioning or spacer? 
            // Since it's a small card, likely just flow is fine, but let's add some space.
            
            val signatureTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setMarginTop(10f)
            
            // Left: Authorized By
            val authCell = Cell().setBorder(null).setPadding(4f)
            authCell.add(Paragraph("Authorized By:")
                .setFont(regularFont).setFontSize(6f).setFontColor(DeviceGray.GRAY))
            
            // Signature Image
            val sigImage = loadImageFromAssets("signature.png")
            if (sigImage != null) {
                sigImage.setWidth(75f)
                sigImage.setHeight(40f)
                sigImage.setHorizontalAlignment(HorizontalAlignment.LEFT)
                authCell.add(sigImage)
            } else {
                authCell.add(Paragraph("\n__________________").setFontSize(6f))
            }
            
            // Right: Recipient
            val recipCell = Cell().setBorder(null).setPadding(4f)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
                .setTextAlignment(TextAlignment.RIGHT)
            
            recipCell.add(Paragraph("Recipient Signature:")
                .setFont(regularFont).setFontSize(6f).setFontColor(DeviceGray.GRAY))
            recipCell.add(Paragraph("\n__________________")
                .setFont(regularFont).setFontSize(6f).setTextAlignment(TextAlignment.RIGHT))
                
            val dateStr = gasSlip.generatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")).uppercase()
            recipCell.add(Paragraph(dateStr)
                .setFont(regularFont).setFontSize(6f).setTextAlignment(TextAlignment.RIGHT).setMarginTop(2f))

            signatureTable.addCell(authCell)
            signatureTable.addCell(recipCell)
            document.add(signatureTable)

            // ═══ FOOTER ═══
            val footer = Paragraph("FuelHub System • Official Receipt")
                .setFont(regularFont)
                .setFontSize(5f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(DeviceGray.GRAY)
                .setFixedPosition(10f, 15f, receiptPageSize.width - 20f)
            document.add(footer)
            
            // Draw border LAST to ensure page exists
            if (pdfDocument.numberOfPages > 0) {
                val canvas = PdfCanvas(pdfDocument.getPage(1))
                canvas.rectangle(10.0, 10.0, receiptPageSize.width - 20.0, receiptPageSize.height - 20.0)
                canvas.setStrokeColor(DeviceGray.BLACK)
                canvas.setLineWidth(1f)
                canvas.stroke()
            }
            
        } finally {
            document.close()
        }
        
        return file.absolutePath
    }
    
    private fun addDetailRow(table: Table, label: String, value: String, isGrayBg: Boolean) {
        val bgArgs = if (isGrayBg) DeviceGray(0.98f) else null // Very subtle alternate row
        
        val labelCell = Cell().add(Paragraph(label).setFontSize(8f).setBold())
            .setBorder(null)
            .setPadding(2f)
        
        val valueCell = Cell().add(Paragraph(value).setFontSize(8f))
            .setBorder(null)
            .setPadding(2f)

        // IText7 basics for background if needed, but simple clean white is often better for print.
        // Let's stick to clean white.
        table.addCell(labelCell)
        table.addCell(valueCell)
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
