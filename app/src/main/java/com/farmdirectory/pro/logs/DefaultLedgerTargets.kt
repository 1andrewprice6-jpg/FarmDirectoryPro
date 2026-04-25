package com.farmdirectory.pro.logs

import com.farmdirectory.pro.logs.chickenhouse.ChickenHouseLogDao
import com.farmdirectory.pro.logs.chickenhouse.ChickenHouseLogEntity
import com.farmdirectory.pro.logs.fuel.FuelLogDao
import com.farmdirectory.pro.logs.fuel.FuelLogEntity
import com.farmdirectory.pro.logs.mileage.MileageDao
import com.farmdirectory.pro.logs.mileage.MileageEntryEntity
import com.farmdirectory.pro.vision.ledger.LedgerPropagator

/**
 * Default wiring of the LedgerPropagator.Targets interface against the three
 * downstream tables. Construct in your DI graph / Application.onCreate.
 */
class DefaultLedgerTargets(
    private val fuelDao: FuelLogDao,
    private val mileageDao: MileageDao,
    private val chickenHouseDao: ChickenHouseLogDao,
) : LedgerPropagator.Targets {

    override suspend fun insertFuelLog(entry: LedgerPropagator.FuelLogInsert): Long? =
        fuelDao.insert(
            FuelLogEntity(
                capturedAtEpochMs = entry.capturedAtEpochMs,
                farmId = entry.farmId,
                totalCost = entry.totalCost,
                gallons = entry.gallons,
                pricePerGallon = entry.pricePerGallon,
                odometerMiles = entry.odometerMiles,
                sourceCaptureId = entry.sourceCaptureId,
            ),
        )

    override suspend fun insertMileageEntry(entry: LedgerPropagator.MileageInsert): Long? =
        mileageDao.insert(
            MileageEntryEntity(
                capturedAtEpochMs = entry.capturedAtEpochMs,
                odometerMiles = entry.odometerMiles,
                farmId = entry.farmId,
                sourceCaptureId = entry.sourceCaptureId,
            ),
        )

    override suspend fun insertChickenHouseLog(entry: LedgerPropagator.ChickenHouseInsert): Long? =
        chickenHouseDao.insert(
            ChickenHouseLogEntity(
                dateIso = entry.dateIso,
                farmId = entry.farmId,
                metrics = entry.metrics,
                sourceCaptureId = entry.sourceCaptureId,
            ),
        )
}
