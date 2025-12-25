package com.example.farmdirectoryupgraded.agents

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Voice Recognition Agent
 * Natural Language Understanding for voice commands
 */
class VoiceAgent(private val context: Context) {
    
    private val TAG = "VoiceAgent"
    
    /**
     * Process voice input and extract farmer/farm information
     */
    suspend fun processVoiceCommand(transcribedText: String): VoiceResult = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "Processing voice: $transcribedText")
            
            // Detect intent
            val intent = detectIntent(transcribedText)
            
            when (intent) {
                VoiceIntent.ADD_FARMER -> extractFarmerInfo(transcribedText)
                VoiceIntent.CHECK_IN -> extractCheckInInfo(transcribedText)
                VoiceIntent.SEARCH -> extractSearchQuery(transcribedText)
                VoiceIntent.NAVIGATE -> extractNavigationRequest(transcribedText)
                VoiceIntent.UPDATE -> extractUpdateInfo(transcribedText)
                else -> VoiceResult(
                    success = false,
                    intent = VoiceIntent.UNKNOWN,
                    message = "I didn't understand that. Try saying 'Add farmer John Doe' or 'Check in at Green Acres'"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing voice", e)
            VoiceResult(
                success = false,
                intent = VoiceIntent.UNKNOWN,
                message = "Error: ${e.message}"
            )
        }
    }
    
    /**
     * Detect user intent from voice command
     */
    private fun detectIntent(text: String): VoiceIntent {
        val lower = text.toLowerCase()
        return when {
            lower.contains("add") && (lower.contains("farmer") || lower.contains("farm")) -> VoiceIntent.ADD_FARMER
            lower.contains("check in") || lower.contains("check-in") || lower.contains("checkin") -> VoiceIntent.CHECK_IN
            lower.contains("search") || lower.contains("find") || lower.contains("show me") -> VoiceIntent.SEARCH
            lower.contains("navigate") || lower.contains("directions") || lower.contains("take me") -> VoiceIntent.NAVIGATE
            lower.contains("update") || lower.contains("change") || lower.contains("modify") -> VoiceIntent.UPDATE
            else -> VoiceIntent.UNKNOWN
        }
    }
    
    /**
     * Extract farmer information from voice command
     */
    private fun extractFarmerInfo(text: String): VoiceResult {
        val data = mutableMapOf<String, String>()
        
        // Extract name
        val namePattern = Regex("(?:farmer|person|add)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)", RegexOption.IGNORE_CASE)
        namePattern.find(text)?.let {
            data["name"] = it.groupValues[1].trim()
        }
        
        // Extract farm name
        val farmPattern = Regex("farm\\s+(?:name\\s+)?(?:is\\s+)?([A-Za-z\\s]+?)(?:,|phone|address|location|$)", RegexOption.IGNORE_CASE)
        farmPattern.find(text)?.let {
            data["farmName"] = it.groupValues[1].trim()
        }
        
        // Extract phone
        val phonePattern = Regex("(?:phone|telephone|number|call)\\s+(?:is\\s+)?(\\d[\\d\\s\\-\\.\\(\\)]+)", RegexOption.IGNORE_CASE)
        phonePattern.find(text)?.let {
            data["phone"] = it.groupValues[1].trim().replace(Regex("[^0-9]"), "")
        }
        
        // Extract address
        val addressPattern = Regex("(?:address|location|at|located at)\\s+(?:is\\s+)?([^,]+?)(?:,|phone|$)", RegexOption.IGNORE_CASE)
        addressPattern.find(text)?.let {
            data["address"] = it.groupValues[1].trim()
        }
        
        // Extract type
        val typePattern = Regex("(?:type|kind)\\s+(?:is\\s+)?(pullet|breeder)", RegexOption.IGNORE_CASE)
        typePattern.find(text)?.let {
            data["type"] = it.groupValues[1].trim().capitalize()
        }
        
        return if (data.containsKey("name") || data.containsKey("farmName")) {
            VoiceResult(
                success = true,
                intent = VoiceIntent.ADD_FARMER,
                data = data,
                message = "Found: ${data["name"] ?: "Farmer"} - ${data["farmName"] ?: "Farm"}"
            )
        } else {
            VoiceResult(
                success = false,
                intent = VoiceIntent.ADD_FARMER,
                message = "Please specify at least a farmer name or farm name"
            )
        }
    }
    
    /**
     * Extract check-in information
     */
    private fun extractCheckInInfo(text: String): VoiceResult {
        val data = mutableMapOf<String, String>()
        
        // Extract farm name
        val farmPattern = Regex("(?:at|to)\\s+([A-Za-z\\s]+?)(?:,|$)", RegexOption.IGNORE_CASE)
        farmPattern.find(text)?.let {
            data["farmName"] = it.groupValues[1].trim()
        }
        
        // Extract notes
        val notesPattern = Regex("(?:note|notes|reason)\\s+(.+)$", RegexOption.IGNORE_CASE)
        notesPattern.find(text)?.let {
            data["notes"] = it.groupValues[1].trim()
        }
        
        return VoiceResult(
            success = true,
            intent = VoiceIntent.CHECK_IN,
            data = data,
            message = "Check in at: ${data["farmName"] ?: "current location"}"
        )
    }
    
    /**
     * Extract search query
     */
    private fun extractSearchQuery(text: String): VoiceResult {
        val searchPattern = Regex("(?:search|find|show me)\\s+(.+)", RegexOption.IGNORE_CASE)
        val query = searchPattern.find(text)?.groupValues?.get(1)?.trim() ?: ""
        
        return VoiceResult(
            success = query.isNotBlank(),
            intent = VoiceIntent.SEARCH,
            data = mapOf("query" to query),
            message = "Searching for: $query"
        )
    }
    
    /**
     * Extract navigation request
     */
    private fun extractNavigationRequest(text: String): VoiceResult {
        val navPattern = Regex("(?:to|at)\\s+([A-Za-z\\s]+?)(?:,|$)", RegexOption.IGNORE_CASE)
        val destination = navPattern.find(text)?.groupValues?.get(1)?.trim() ?: ""
        
        return VoiceResult(
            success = destination.isNotBlank(),
            intent = VoiceIntent.NAVIGATE,
            data = mapOf("destination" to destination),
            message = "Navigate to: $destination"
        )
    }
    
    /**
     * Extract update information
     */
    private fun extractUpdateInfo(text: String): VoiceResult {
        val data = mutableMapOf<String, String>()
        
        // Detect what to update
        when {
            text.contains("health", ignoreCase = true) -> {
                data["updateType"] = "health"
                val statusPattern = Regex("(?:to|as)\\s+(healthy|sick|critical)", RegexOption.IGNORE_CASE)
                statusPattern.find(text)?.let {
                    data["healthStatus"] = it.groupValues[1].trim().toUpperCase()
                }
            }
            text.contains("location", ignoreCase = true) -> {
                data["updateType"] = "location"
            }
            text.contains("phone", ignoreCase = true) -> {
                data["updateType"] = "phone"
                val phonePattern = Regex("(\\d[\\d\\s\\-\\.]+)", RegexOption.IGNORE_CASE)
                phonePattern.find(text)?.let {
                    data["phone"] = it.groupValues[1].trim()
                }
            }
        }
        
        return VoiceResult(
            success = data.isNotEmpty(),
            intent = VoiceIntent.UPDATE,
            data = data,
            message = "Update ${data["updateType"] ?: "information"}"
        )
    }
}

/**
 * Voice command intents
 */
enum class VoiceIntent {
    ADD_FARMER,
    CHECK_IN,
    SEARCH,
    NAVIGATE,
    UPDATE,
    UNKNOWN
}

/**
 * Voice processing result
 */
data class VoiceResult(
    val success: Boolean,
    val intent: VoiceIntent,
    val data: Map<String, String> = emptyMap(),
    val message: String,
    val confidence: Double = 0.0
)
