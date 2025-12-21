package dev.ml.fuelhub.data.util

import android.content.Context
import android.graphics.Bitmap
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
import java.io.FileOutputStream
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
        // 3.5x5 inch index card size (252 x 360 points)
        val receiptPageSize = PageSize(252f, 360f)
        val document = Document(pdfDocument, receiptPageSize)
        
        try {
            // Margins: Compact for single page - Top 10, Bottom 8, Left/Right 10
            document.setMargins(10f, 10f, 8f, 10f)
            
            val titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            
            // ═══ HEADER ═══
            val logoImage = loadImageFromAssets("mdrrmo.png")
            if (logoImage != null) {
                logoImage.setWidth(32f)
                logoImage.setHeight(32f)
                logoImage.setHorizontalAlignment(HorizontalAlignment.CENTER)
                document.add(logoImage)
            }
            
            document.add(Paragraph("FUEL DISPENSING SLIP")
                .setFont(titleFont)
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(0f)
                .setMarginBottom(0f))
            
            document.add(Paragraph("REF: ${gasSlip.referenceNumber.uppercase()}")
                .setFont(regularFont)
                .setFontSize(6f)
                .setFontColor(DeviceGray.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(6f))

            // ═══ HERO SECTION (Fuel Allocation) ═══
            // Light gray box for key info
            val fuelTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1.2f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setMarginBottom(8f)
                
            val fuelCellCommon = Cell()
                .setBorder(null)
                .setBackgroundColor(DeviceGray(0.96f)) // Very light gray modern look
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8f)

            // Fuel Type (Left)
            val typeCell = fuelCellCommon.clone(true)
            typeCell.add(Paragraph("FUEL TYPE")
                .setFont(regularFont).setFontSize(5f).setFontColor(DeviceGray.GRAY))
            typeCell.add(Paragraph(gasSlip.fuelType.name.uppercase())
                .setFont(titleFont).setFontSize(11f).setFontColor(DeviceGray.BLACK))
                
            // Liters (Right - Highlighted)
            val amountCell = fuelCellCommon.clone(true)
                .setTextAlignment(TextAlignment.RIGHT)
            amountCell.add(Paragraph("ALLOCATION")
                .setFont(regularFont).setFontSize(5f).setFontColor(DeviceGray.GRAY))
            amountCell.add(Paragraph("${gasSlip.litersToPump} L")
                .setFont(titleFont).setFontSize(16f).setFontColor(DeviceGray.BLACK)) // Large & Bold
                
            fuelTable.addCell(typeCell)
            fuelTable.addCell(amountCell)
            document.add(fuelTable)

            // ═══ DETAILS GRID ═══
            val detailsTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2.5f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setMarginBottom(8f)
            
            // Combined Vehicle Info
            addDetailRow(detailsTable, "VEHICLE", "${gasSlip.vehicleType.uppercase()} • ${gasSlip.vehiclePlateNumber.uppercase()}")
            
            // Driver
            val displayDriverName = gasSlip.driverFullName?.uppercase() ?: gasSlip.driverName.uppercase()
            addDetailRow(detailsTable, "DRIVER", displayDriverName)
            
            addDetailRow(detailsTable, "PURPOSE", gasSlip.tripPurpose.uppercase())
            addDetailRow(detailsTable, "DESTINATION", gasSlip.destination.uppercase())
            
            document.add(detailsTable)
            
            // ═══ FOOTER & SIGNATURES ═══
            // Layout: QR Code (Left) | Signatures (Right Checkbox style)
            val footerTable = Table(UnitValue.createPercentArray(floatArrayOf(0.8f, 1.2f)))
                .setWidth(UnitValue.createPercentValue(100f))
            
            // 1. QR Code Column
            val qrCell = Cell().setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(0f)
            
            val transactionData = QRCodeGenerator.createTransactionData(
                referenceNumber = gasSlip.referenceNumber,
                vehiclePlate = gasSlip.vehiclePlateNumber,
                driverName = displayDriverName,
                fuelType = gasSlip.fuelType.name,
                liters = gasSlip.litersToPump,
                date = gasSlip.generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            )
            
            val qrCodeBitmap = QRCodeGenerator.generateQRCode(transactionData, 200)
            if (qrCodeBitmap != null) {
                val qrFile = saveBitmapToTemp(qrCodeBitmap)
                if (qrFile != null) {
                    val qrImage = Image(ImageDataFactory.create(qrFile.absolutePath))
                    qrImage.setWidth(85f)
                    qrImage.setHeight(85f)
                    qrCell.add(qrImage)
                }
            }
            
            // 2. Signatures Column
            val sigCell = Cell().setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingLeft(12f)
                
            // Auth Signature
            sigCell.add(Paragraph("AUTHORIZED BY:")
                .setFont(titleFont).setFontSize(5f).setFontColor(DeviceGray.GRAY)
                .setMarginBottom(1f))
            
            val sigImage = loadImageFromAssets("signature.png")
            if (sigImage != null) {
                sigImage.setWidth(70f)
                sigImage.setHeight(38f)
                sigCell.add(sigImage)
            } else {
                sigCell.add(Paragraph("").setFontSize(24f))
            }
            sigCell.add(Paragraph("_______________________")
                .setFont(regularFont).setFontSize(5f).setFontColor(ColorConstants.LIGHT_GRAY)
                .setMarginTop(-3f)) // Pull line up slightly to overlap/sit under signature nicely
                
            // Recipient Signature
            sigCell.add(Paragraph("RECEIVED BY:")
                .setFont(titleFont).setFontSize(5f).setFontColor(DeviceGray.GRAY)
                .setMarginTop(7f)) // More separation
                
            sigCell.add(Paragraph("").setFontSize(18f)) // Space for manual signing
            sigCell.add(Paragraph("_______________________")
                .setFont(regularFont).setFontSize(5f).setFontColor(ColorConstants.LIGHT_GRAY)
                .setMarginTop(12f))

            footerTable.addCell(qrCell)
            footerTable.addCell(sigCell)
            document.add(footerTable)

            // Bottom Brand & Statement - Compact
            // Brand line with date
            document.add(Paragraph("MDRRMO • ${gasSlip.generatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
                .setFont(titleFont)
                .setFontSize(4.5f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginTop(12f)
                .setMarginBottom(2f))
            
            // Legal statement with better spacing
            document.add(Paragraph("This Fuel Dispensing Slip is system-generated and valid only upon QR code verification. Unauthorized reproduction, alteration, or misuse is strictly prohibited.")
                .setFont(regularFont)
                .setFontSize(3.5f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMultipliedLeading(1.1f)
                .setMarginBottom(0f)
                .setMarginTop(1f))
            // Optional: Thin border around content
            if (pdfDocument.numberOfPages > 0) {
                val canvas = PdfCanvas(pdfDocument.getPage(1))
                canvas.setStrokeColor(DeviceGray.BLACK)
                canvas.setLineWidth(0.5f)
                canvas.rectangle(10.0, 10.0, receiptPageSize.width - 20.0, receiptPageSize.height - 20.0)
                canvas.stroke()
            }
            
        } finally {
            document.close()
        }
        
        return file.absolutePath
    }
    
    private fun addDetailRow(table: Table, label: String, value: String) {
        val labelCell = Cell().add(Paragraph(label).setFontSize(6f).setBold().setFontColor(DeviceGray.GRAY))
            .setBorder(null)
            .setBorderBottom(SolidBorder(DeviceGray(0.85f), 0.5f))
            .setPaddingTop(2f)
            .setPaddingBottom(2f)
        
        val valueCell = Cell().add(Paragraph(value).setFontSize(8f).setTextAlignment(TextAlignment.RIGHT))
            .setBorder(null)
            .setBorderBottom(SolidBorder(DeviceGray(0.85f), 0.5f))
            .setPaddingTop(2f)
            .setPaddingBottom(2f)

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
    
    private fun saveBitmapToTemp(bitmap: Bitmap): File? {
        return try {
            val tempDir = context.cacheDir
            val tempFile = File(tempDir, "qr_code_temp_${System.currentTimeMillis()}.png")
            
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            tempFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to save QR code bitmap to temp file")
            null
        }
    }
}
