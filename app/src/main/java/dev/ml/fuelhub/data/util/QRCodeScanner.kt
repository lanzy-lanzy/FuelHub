package dev.ml.fuelhub.data.util

import timber.log.Timber

/**
 * Data class for parsed QR code transaction data
 */
data class ScannedTransaction(
    val referenceNumber: String,
    val vehiclePlate: String,
    val driverName: String,
    val fuelType: String,
    val liters: Double,
    val date: String
)

/**
 * Utility class for parsing and validating scanned QR code data.
 */
object QRCodeScanner {
    
    /**
     * Parse the QR code data string into a ScannedTransaction object.
     * 
     * @param data The raw QR code data string
     * @return ScannedTransaction if parsing is successful, null otherwise
     */
    fun parseQRCode(data: String): ScannedTransaction? {
        return try {
            Timber.d("📱 Parsing QR Code Raw Data: '$data'")
            
            val parts = data.split("|")
                .map { it.trim() }
                .also { Timber.d("📱 QR Parts after split: $it") }
                .associate { 
                    val (key, value) = it.split(":", limit = 2)
                    key.trim() to value.trim()
                }
            
            Timber.d("📱 QR Parts mapped: $parts")
            
            val referenceNumber = parts["REF"] ?: run {
                Timber.e("❌ Missing REF in QR code")
                return null
            }
            val vehiclePlate = parts["PLATE"] ?: run {
                Timber.e("❌ Missing PLATE in QR code")
                return null
            }
            val driverName = parts["DRIVER"] ?: run {
                Timber.e("❌ Missing DRIVER in QR code")
                return null
            }
            val fuelType = parts["FUEL"] ?: run {
                Timber.e("❌ Missing FUEL in QR code")
                return null
            }
            val liters = parts["LITERS"]?.toDoubleOrNull() ?: run {
                Timber.e("❌ Missing or invalid LITERS in QR code: ${parts["LITERS"]}")
                return null
            }
            val date = parts["DATE"] ?: run {
                Timber.e("❌ Missing DATE in QR code")
                return null
            }
            
            val scanned = ScannedTransaction(
                referenceNumber = referenceNumber,
                vehiclePlate = vehiclePlate,
                driverName = driverName,
                fuelType = fuelType,
                liters = liters,
                date = date
            )
            
            Timber.d("✓ QR Code parsed successfully: REF=$referenceNumber")
            scanned
        } catch (e: Exception) {
            Timber.e(e, "❌ Error parsing QR code: ${e.message}")
            null
        }
    }
    
    /**
     * Validate the scanned transaction data.
     * Ensures all required fields are non-empty.
     */
    fun isValidTransaction(transaction: ScannedTransaction): Boolean {
        return with(transaction) {
            val isValid = referenceNumber.isNotEmpty() &&
                vehiclePlate.isNotEmpty() &&
                driverName.isNotEmpty() &&
                fuelType.isNotEmpty() &&
                liters > 0.0 &&
                date.isNotEmpty()
            
            if (!isValid) {
                Timber.w("⚠️ QR Transaction validation failed:")
                Timber.w("  - REF empty: ${referenceNumber.isEmpty()}")
                Timber.w("  - PLATE empty: ${vehiclePlate.isEmpty()}")
                Timber.w("  - DRIVER empty: ${driverName.isEmpty()}")
                Timber.w("  - FUEL empty: ${fuelType.isEmpty()}")
                Timber.w("  - LITERS invalid: ${liters <= 0.0}")
                Timber.w("  - DATE empty: ${date.isEmpty()}")
            } else {
                Timber.d("✓ QR Transaction validation passed")
            }
            
            isValid
        }
    }
}
