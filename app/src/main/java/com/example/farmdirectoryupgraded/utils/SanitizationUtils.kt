package com.example.farmdirectoryupgraded.utils

/**
 * Input Sanitization Utilities
 * Cleans and sanitizes user inputs to prevent security issues
 */
object SanitizationUtils {

    /**
     * Sanitize text input by removing potentially harmful characters
     */
    fun sanitizeText(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"'`]"), "") // Remove HTML/script injection chars
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .take(500) // Limit length
    }

    /**
     * Sanitize email input
     */
    fun sanitizeEmail(email: String): String {
        return email
            .trim()
            .lowercase()
            .take(254) // RFC 5321 maximum email length
    }

    /**
     * Sanitize phone number by removing non-numeric characters except + and ()
     */
    fun sanitizePhone(phone: String): String {
        return phone
            .trim()
            .replace(Regex("[^0-9+()\\s.-]"), "")
            .take(20)
    }

    /**
     * Sanitize URL input
     */
    fun sanitizeUrl(url: String): String {
        return url
            .trim()
            .replace(Regex("\\s+"), "") // Remove all whitespace
            .take(2048) // Maximum URL length
    }

    /**
     * Sanitize numeric input
     */
    fun sanitizeNumeric(input: String): String {
        return input
            .trim()
            .replace(Regex("[^0-9.-]"), "")
            .take(20)
    }

    /**
     * Sanitize alphanumeric input (for IDs)
     */
    fun sanitizeAlphanumeric(input: String): String {
        return input
            .trim()
            .replace(Regex("[^a-zA-Z0-9_-]"), "")
            .take(50)
    }

    /**
     * Prevent SQL injection by escaping single quotes
     * Note: Room uses parameterized queries, but this is an extra safety layer
     */
    fun escapeSqlString(input: String): String {
        return input.replace("'", "''")
    }

    /**
     * Remove control characters that might cause issues
     */
    fun removeControlCharacters(input: String): String {
        return input.replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "")
    }

    /**
     * Sanitize address field (allows more characters than regular text)
     */
    fun sanitizeAddress(address: String): String {
        return address
            .trim()
            .replace(Regex("[<>\"'`]"), "")
            .replace(Regex("\\s+"), " ")
            .take(1000)
    }
}
