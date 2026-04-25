package com.example.farmdirectoryupgraded.vision.ledger

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.farmdirectoryupgraded.vision.capture.CaptureMode
import com.example.farmdirectoryupgraded.vision.capture.ParsedFields
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Fans a validated capture out to every downstream log that should own a piece
 * of it. Everything runs inside a single Room transaction — if the fuel log
 * insert fails, the mileage insert is rolled back too. No orphans.
 *
 * The downstream DAOs (FuelLogDao, MileageEntryDao, ChickenHouseLogDao) are
 * passed via [Targets]. This keeps the propagator decoupled from the rest of
 * the app's data module — you implement the interfaces where your tables
 * already live.
 */
class LedgerPropagator(
    private val db: RoomDatabase,
    private val captureDao: CaptureDao,
    private val targets: Targets,
    private val farmGridMapper: FarmGridMapper,
) {

    /** Implement these three surfaces against your existing DAOs. */
    interface Targets {
        suspend fun insertFuelLog(entry: FuelLogInsert): Long?
        suspend fun insertMileageEntry(entry: MileageInsert): Long?
        suspend fun insertChickenHouseLog(entry: ChickenHouseInsert): Long?
    }

    data class FuelLogInsert(
        val capturedAtEpochMs: Long,
        val farmId: String?,
        val totalCost: Double?,
        val gallons: Double?,
        val pricePerGallon: Double?,
        val odometerMiles: Int?,
        val sourceCaptureId: Long,
    )

    data class MileageInsert(
        val capturedAtEpochMs: Long,
        val odometerMiles: Int,
        val farmId: String?,
        val sourceCaptureId: Long,
    )

    data class ChickenHouseInsert(
        val dateIso: String?,
        val farmId: String?,
        val metrics: Map<String, Double>,
        val sourceCaptureId: Long,
    )

    /**
     * @return the map of downstream IDs that were created, suitable for
     *         writing back into [CaptureEntity.linkedEntitiesJson].
     */
    suspend fun propagate(
        capture: CaptureEntity,
        mode: CaptureMode,
        fields: ParsedFields,
    ): Map<String, Long?> = db.withTransaction {
        val matched = farmGridMapper.resolve(fields.farmId, capture.latitude, capture.longitude)
        val resolvedFarmId = matched?.farm?.id ?: fields.farmId

        val links = mutableMapOf<String, Long?>()

        when (mode) {
            CaptureMode.FUEL_PUMP -> {
                val fuelId = targets.insertFuelLog(
                    FuelLogInsert(
                        capturedAtEpochMs = capture.capturedAtEpochMs,
                        farmId = resolvedFarmId,
                        totalCost = fields.totalCost,
                        gallons = fields.gallons,
                        pricePerGallon = fields.pricePerGallon,
                        odometerMiles = fields.odometerMiles,
                        sourceCaptureId = capture.id,
                    ),
                )
                links["fuelLogId"] = fuelId
                // A fuel-pump capture that also surfaces odometer → mileage entry too
                fields.odometerMiles?.let { odo ->
                    val mileageId = targets.insertMileageEntry(
                        MileageInsert(
                            capturedAtEpochMs = capture.capturedAtEpochMs,
                            odometerMiles = odo,
                            farmId = resolvedFarmId,
                            sourceCaptureId = capture.id,
                        ),
                    )
                    links["mileageEntryId"] = mileageId
                }
            }
            CaptureMode.DASHBOARD_ODOMETER -> {
                fields.odometerMiles?.let { odo ->
                    val id = targets.insertMileageEntry(
                        MileageInsert(
                            capturedAtEpochMs = capture.capturedAtEpochMs,
                            odometerMiles = odo,
                            farmId = resolvedFarmId,
                            sourceCaptureId = capture.id,
                        ),
                    )
                    links["mileageEntryId"] = id
                }
            }
            CaptureMode.CHICKEN_HOUSE_LOG -> {
                val id = targets.insertChickenHouseLog(
                    ChickenHouseInsert(
                        dateIso = fields.dateIso,
                        farmId = resolvedFarmId,
                        metrics = fields.metrics,
                        sourceCaptureId = capture.id,
                    ),
                )
                links["chickenHouseLogId"] = id
            }
            CaptureMode.ANALOG_GAUGE, CaptureMode.GENERIC_LOG -> {
                // Capture stands alone; nothing to fan out.
            }
        }

        // Persist the linkage back onto the capture row
        val linksJson = buildJsonObject {
            links.forEach { (k, v) -> put(k, if (v != null) JsonPrimitive(v) else JsonPrimitive(null as Long?)) }
        }.toString()
        captureDao.update(
            capture.copy(
                linkedEntitiesJson = linksJson,
                farmId = resolvedFarmId,
            ),
        )
        links
    }
}
