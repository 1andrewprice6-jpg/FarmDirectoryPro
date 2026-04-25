package com.example.farmdirectoryupgraded.vision.ledger

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.capture.ParsedFields
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * The forensic record. Every capture — good, flagged, or rejected — is
 * recorded here. Raw JPEG lives on disk at [rawImagePath]; never auto-deleted.
 */
@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val capturedAtEpochMs: Long,
    val mode: String,
    val rawImagePath: String,
    val latitude: Double?,
    val longitude: Double?,
    /** Serialized ParsedFields JSON — keeps schema migrations cheap. */
    val parsedFieldsJson: String,
    /** "COMPLETE" | "INCOMPLETE" | "INCONSISTENT" | "REJECTED" | "EDITED" */
    val status: String,
    /** Set when the user manually corrects fields in the review screen. */
    val userEditedAtEpochMs: Long? = null,
    /** Linked downstream entity IDs (JSON) for audit trail:
     *  {"fuelLogId": 42, "mileageEntryId": 19, "chickenHouseLogId": null} */
    val linkedEntitiesJson: String = "{}",
    /** If this capture references a farm, the matched farm's ID. */
    val farmId: String? = null,
    /** Backend sync status. */
    val syncedAt: Long? = null,
)

class ParsedFieldsConverter {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @TypeConverter
    fun toJson(fields: ParsedFields): String = json.encodeToString(fields)

    @TypeConverter
    fun fromJson(s: String): ParsedFields =
        if (s.isBlank()) ParsedFields() else json.decodeFromString(ParsedFields.serializer(), s)

    @TypeConverter
    fun modeToString(m: CaptureMode): String = m.name

    @TypeConverter
    fun modeFromString(s: String): CaptureMode = CaptureMode.valueOf(s)
}
