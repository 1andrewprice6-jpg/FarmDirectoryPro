package com.example.farmdirectoryupgraded.utils

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Input Validation Utilities
 * Provides comprehensive validation for user inputs
 */
object ValidationUtils {

    // Regex patterns for validation
    private val EMAIL_PATTERN = Patterns.EMAIL_ADDRESS
    private val PHONE_PATTERN = Pattern.compile("^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$")
    private val URL_PATTERN = Patterns.WEB_URL
    private val COORDINATE_PATTERN = Pattern.compile("^-?([0-9]{1,2}|1[0-7][0-9]|180)(\\.[0-9]{1,10})?$")

    /**
     * Validation result with error message
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true, null)
            fun error(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * Validate email address
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult.success() // Email is optional
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.error("Invalid email format")
        }

        if (email.length > 254) {
            return ValidationResult.error("Email is too long (max 254 characters)")
        }

        return ValidationResult.success()
    }

    /**
     * Validate phone number
     */
    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) {
            return ValidationResult.success() // Phone is optional
        }

        // Remove common separators for validation
        val cleanPhone = phone.replace(Regex("[\\s().-]"), "")

        if (cleanPhone.length < 10) {
            return ValidationResult.error("Phone number must have at least 10 digits")
        }

        if (cleanPhone.length > 15) {
            return ValidationResult.error("Phone number is too long (max 15 digits)")
        }

        if (!cleanPhone.all { it.isDigit() || it == '+' }) {
            return ValidationResult.error("Phone number contains invalid characters")
        }

        return ValidationResult.success()
    }

    /**
     * Validate URL
     */
    fun validateUrl(url: String): ValidationResult {
        if (url.isBlank()) {
            return ValidationResult.error("URL cannot be empty")
        }

        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ws://") && !url.startsWith("wss://")) {
            return ValidationResult.error("URL must start with http://, https://, ws://, or wss://")
        }

        if (!URL_PATTERN.matcher(url).matches()) {
            return ValidationResult.error("Invalid URL format")
        }

        if (url.length > 2048) {
            return ValidationResult.error("URL is too long (max 2048 characters)")
        }

        return ValidationResult.success()
    }

    /**
     * Validate required text field
     */
    fun validateRequired(value: String, fieldName: String): ValidationResult {
        if (value.isBlank()) {
            return ValidationResult.error("$fieldName is required")
        }

        if (value.length > 500) {
            return ValidationResult.error("$fieldName is too long (max 500 characters)")
        }

        return ValidationResult.success()
    }

    /**
     * Validate GPS coordinates
     */
    fun validateLatitude(latitude: String): ValidationResult {
        if (latitude.isBlank()) {
            return ValidationResult.success() // Coordinates are optional
        }

        val lat = latitude.toDoubleOrNull()
        if (lat == null) {
            return ValidationResult.error("Latitude must be a valid number")
        }

        if (lat < -90.0 || lat > 90.0) {
            return ValidationResult.error("Latitude must be between -90 and 90")
        }

        return ValidationResult.success()
    }

    fun validateLongitude(longitude: String): ValidationResult {
        if (longitude.isBlank()) {
            return ValidationResult.success() // Coordinates are optional
        }

        val lon = longitude.toDoubleOrNull()
        if (lon == null) {
            return ValidationResult.error("Longitude must be a valid number")
        }

        if (lon < -180.0 || lon > 180.0) {
            return ValidationResult.error("Longitude must be between -180 and 180")
        }

        return ValidationResult.success()
    }

    /**
     * Validate farm ID
     */
    fun validateFarmId(farmId: String): ValidationResult {
        if (farmId.isBlank()) {
            return ValidationResult.error("Farm ID is required")
        }

        if (farmId.length < 3) {
            return ValidationResult.error("Farm ID must be at least 3 characters")
        }

        if (farmId.length > 50) {
            return ValidationResult.error("Farm ID is too long (max 50 characters)")
        }

        // Only allow alphanumeric, hyphens, and underscores
        if (!farmId.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            return ValidationResult.error("Farm ID can only contain letters, numbers, hyphens, and underscores")
        }

        return ValidationResult.success()
    }

    /**
     * Validate worker name
     */
    fun validateWorkerName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.error("Worker name is required")
        }

        if (name.length < 2) {
            return ValidationResult.error("Worker name must be at least 2 characters")
        }

        if (name.length > 100) {
            return ValidationResult.error("Worker name is too long (max 100 characters)")
        }

        return ValidationResult.success()
    }
}
