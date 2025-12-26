package com.example.farmdirectoryupgraded.utils

import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRCodeScanner {
    private val TAG = "QRCodeScanner"
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    /**
     * Scan QR code from an InputImage
     * @param image The image to scan
     * @param onSuccess Callback with scanned QR code data
     * @param onFailure Callback with error message
     */
    fun scanQRCode(
        image: InputImage,
        onSuccess: (qrCodeData: String) -> Unit,
        onFailure: (error: String) -> Unit
    ) {
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val qrCode = barcodes.first()
                    val rawValue = qrCode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        Log.d(TAG, "QR Code scanned: $rawValue")
                        onSuccess(rawValue)
                    } else {
                        onFailure("QR code data is empty")
                    }
                } else {
                    onFailure("No QR code found in image")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "QR code scanning failed", exception)
                onFailure(exception.message ?: "QR code scanning failed")
            }
    }

    /**
     * Parse employee ID or data from QR code
     * Assumes QR code format: "EMP:123" or just "123"
     * @param qrData Raw QR code data
     * @return Parsed employee ID or null if invalid
     */
    fun parseEmployeeFromQR(qrData: String): Int? {
        return try {
            // Try parsing EMP:123 format
            if (qrData.startsWith("EMP:", ignoreCase = true)) {
                qrData.substring(4).toIntOrNull()
            } else {
                // Try direct integer parsing
                qrData.toIntOrNull()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse employee ID from QR", e)
            null
        }
    }

    /**
     * Parse attendance data from QR code
     * Format: "EMP:123|FARM:John's Farm|TASK:Harvesting"
     * @param qrData Raw QR code data
     * @return AttendanceQRData or null if invalid
     */
    fun parseAttendanceFromQR(qrData: String): AttendanceQRData? {
        return try {
            val parts = qrData.split("|").associate { part ->
                val (key, value) = part.split(":").let { it[0] to (if (it.size > 1) it[1] else "") }
                key to value
            }

            AttendanceQRData(
                employeeId = parts["EMP"]?.toIntOrNull() ?: return null,
                workLocation = parts["FARM"],
                taskDescription = parts["TASK"]
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse attendance data from QR", e)
            null
        }
    }
}

data class AttendanceQRData(
    val employeeId: Int,
    val workLocation: String? = null,
    val taskDescription: String? = null
)
