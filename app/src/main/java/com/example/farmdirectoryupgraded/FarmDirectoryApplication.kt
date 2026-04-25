package com.example.farmdirectoryupgraded

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.farmdirectoryupgraded.data.FarmDatabase
import com.example.farmdirectoryupgraded.logs.DefaultLedgerTargets
import com.example.farmdirectoryupgraded.vision.ledger.CaptureRepository
import com.example.farmdirectoryupgraded.vision.ledger.FarmGridMapper
import com.example.farmdirectoryupgraded.vision.ledger.LedgerPropagator
import com.example.farmdirectoryupgraded.vision.sync.CaptureSyncWorker
import com.example.farmdirectoryupgraded.vision.sync.SyncConfig
import com.example.farmdirectoryupgraded.vision.vision.AnalogGaugeDetector
import com.example.farmdirectoryupgraded.vision.vision.TextRecognitionEngine
import com.example.farmdirectoryupgraded.vision.ui.CaptureViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object ServiceLocator {
    lateinit var captureRepository: CaptureRepository
}

class CaptureViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaptureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaptureViewModel(
                application,
                ServiceLocator.captureRepository,
                TextRecognitionEngine()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class FarmDirectoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val db = FarmDatabase.getDatabase(this)

        val targets = DefaultLedgerTargets(
            fuelDao = db.newFuelLogDao(),
            mileageDao = db.mileageDao(),
            chickenHouseDao = db.chickenHouseDao(),
        )

        val farmGridMapper = FarmGridMapper(
            lookup = object : FarmGridMapper.FarmLookup {
                override suspend fun all() = db.farmerDao().getAllFarmersSync().map {
                    FarmGridMapper.FarmLookup.Farm(it.id.toString(), it.name, it.latitude, it.longitude)
                }
                override suspend fun byId(id: String) = id.toIntOrNull()?.let { 
                    db.farmerDao().getFarmerById(it) 
                }?.let { 
                    FarmGridMapper.FarmLookup.Farm(it.id.toString(), it.name, it.latitude, it.longitude) 
                }
            },
        )

        val propagator = LedgerPropagator(
            db = db,
            captureDao = db.captureDao(),
            targets = targets,
            farmGridMapper = farmGridMapper,
        )

        ServiceLocator.captureRepository = CaptureRepository(
            context = this,
            captureDao = db.captureDao(),
            textEngine = TextRecognitionEngine(),
            gaugeDetector = AnalogGaugeDetector(),
            propagator = propagator,
            calibrationProvider = { gaugeId ->
                db.gaugeCalibrationDao().byId(gaugeId)?.toDomain()
            },
        )

        // Backend sync setup
        SyncConfig.baseUrl = com.example.farmdirectoryupgraded.BuildConfig.API_BASE_URL
        SyncConfig.captureDao = db.captureDao()
        CaptureSyncWorker.schedulePeriodic(this)
    }
}