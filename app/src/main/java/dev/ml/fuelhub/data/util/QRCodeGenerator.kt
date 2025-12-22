package dev.ml.fuelhub.data.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber

/**
 * Utility class for generating QR codes.
 * Used for transaction verification and security.
 */
object QRCodeGenerator {
    
    /**
     * Generate a QR code bitmap from transaction data.
     * 
     * @param data The data to encode (transaction details)
     * @param size The size of the QR code in pixels (default 300x300)
     * @return Bitmap of the generated QR code, or null if generation fails
     */
    fun generateQRCode(data: String, size: Int = 300): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1
            )
            
            // Create bitmatrix
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
            
            // Convert bitmatrix to bitmap
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate QR code")
            null
        }
    }
    
    /**
     * Create transaction data string for QR encoding.
     * Encodes key transaction details for verification.
     * Format: REF:xxx|PLATE:xxx|DRIVER:xxx|FUEL:xxx|LITERS:xxx|DATE:xxx
     */
    fun createTransactionData(
        referenceNumber: String,
        vehiclePlate: String,
        driverName: String,
        fuelType: String,
        liters: Double,
        date: String
    ): String {
        // Build the string directly without extra spaces or line breaks
        return "REF:$referenceNumber|PLATE:$vehiclePlate|DRIVER:$driverName|FUEL:$fuelType|LITERS:$liters|DATE:$date"
            .also { data ->
                Timber.d("📱 QR Code Data Generated: $data")
            }
    }
}
