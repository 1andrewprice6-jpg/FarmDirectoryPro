# FarmDirectoryPro — Visual Ingestion Full Integration

This bundle finishes the work started in `fdp-vision.zip`. It adds:

1. **Gauge calibration UI** — 4-tap calibration walks the user through a known-min/known-max needle setup once per gauge.
2. **Sync worker** — periodic + immediate WorkManager job uploads unsynced captures.
3. **Captures history tab** — browse, filter, drill into the forensic archive.
4. **Three downstream log domains** — `FuelLog`, `MileageEntry`, `ChickenHouseLog` Room entities + DAOs.
5. **`DefaultLedgerTargets`** — concrete `LedgerPropagator.Targets` against the three DAOs.
6. **Backend mirrors** — REST endpoints for all four domain types + gauge calibrations.

---

## File tree

```
android/src/main/java/com/farmdirectory/pro/
├── vision/
│   ├── calibration/
│   │   ├── GaugeCalibrationEntity.kt   # @Entity + @Dao
│   │   ├── GaugeCalibrationViewModel.kt
│   │   └── GaugeCalibrationScreen.kt   # 4-tap UI
│   ├── sync/
│   │   ├── CaptureSyncClient.kt        # OkHttp multipart
│   │   └── CaptureSyncWorker.kt        # CoroutineWorker
│   └── history/
│       ├── CaptureHistoryViewModel.kt
│       └── CaptureHistoryScreen.kt
└── logs/
    ├── DefaultLedgerTargets.kt
    ├── fuel/FuelLogEntity.kt           # @Entity + @Dao
    ├── mileage/MileageEntity.kt        # @Entity + @Dao
    └── chickenhouse/ChickenHouseLogEntity.kt   # @Entity + @Dao + MetricsConverter

backend/
├── src/logs.ts          # 4 routers: fuel, mileage, chicken-house, gauges
└── prisma/schema.prisma # 4 models

android/migration.sql    # all 5 new SQLite tables
android/build-snippet.gradle.kts  # +work-runtime-ktx
```

---

## AppDatabase wiring

```kotlin
@Database(
    entities = [
        /* ...existing... */,
        // From fdp-vision (already wired):
        com.farmdirectory.pro.vision.ledger.CaptureEntity::class,
        // New in this bundle:
        com.farmdirectory.pro.vision.calibration.GaugeCalibrationEntity::class,
        com.farmdirectory.pro.logs.fuel.FuelLogEntity::class,
        com.farmdirectory.pro.logs.mileage.MileageEntryEntity::class,
        com.farmdirectory.pro.logs.chickenhouse.ChickenHouseLogEntity::class,
    ],
    version = N + 2,                       // bump
    exportSchema = true,
)
@TypeConverters(
    com.farmdirectory.pro.vision.ledger.ParsedFieldsConverter::class,
    com.farmdirectory.pro.logs.chickenhouse.MetricsConverter::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun captureDao(): com.farmdirectory.pro.vision.ledger.CaptureDao
    abstract fun gaugeCalibrationDao(): com.farmdirectory.pro.vision.calibration.GaugeCalibrationDao
    abstract fun fuelLogDao(): com.farmdirectory.pro.logs.fuel.FuelLogDao
    abstract fun mileageDao(): com.farmdirectory.pro.logs.mileage.MileageDao
    abstract fun chickenHouseDao(): com.farmdirectory.pro.logs.chickenhouse.ChickenHouseLogDao
    /* ...existing DAOs... */
}
```

Apply `migration.sql` as a `Migration(N, N+2)` object — every `CREATE TABLE`
is `IF NOT EXISTS` and every `CREATE INDEX` is too, so the migration is
idempotent and safe to re-run during development.

---

## Application.onCreate wiring

```kotlin
class FarmDirectoryProApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)

        // Build the ledger propagator with the concrete targets
        val targets = DefaultLedgerTargets(
            fuelDao = db.fuelLogDao(),
            mileageDao = db.mileageDao(),
            chickenHouseDao = db.chickenHouseDao(),
        )

        val farmGridMapper = FarmGridMapper(
            lookup = object : FarmLookup {
                override suspend fun all() = db.farmDao().getAll().map {
                    FarmLookup.Farm(it.id.toString(), it.name, it.lat, it.lon)
                }
                override suspend fun byId(id: String) = id.toIntOrNull()?.let { db.farmDao().byId(it) }
                    ?.let { FarmLookup.Farm(it.id.toString(), it.name, it.lat, it.lon) }
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
        SyncConfig.baseUrl = BuildConfig.BACKEND_URL  // or null to disable sync
        SyncConfig.authToken = userPrefs.authToken    // or null
        CaptureSyncWorker.schedulePeriodic(this)
    }
}
```

Trigger one-shot upload right after a successful capture (in `CaptureViewModel.onShutter`'s success branch):

```kotlin
CaptureSyncWorker.enqueueImmediate(applicationContext)
```

---

## Nav graph

```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen(...) }

    composable("capture") {
        CaptureScreen(viewModel = captureViewModel) { id ->
            navController.navigate("capture_review/$id")
        }
    }
    composable("capture_review/{id}") { back ->
        val id = back.arguments?.getString("id")?.toLongOrNull() ?: return@composable
        val r = captureViewModel.ui.value.lastResult
        if (r?.captureId == id) {
            CaptureReviewScreen(
                result = r,
                onCommit = { fields ->
                    captureViewModel.commitReview(id, fields)
                    CaptureSyncWorker.enqueueImmediate(applicationContext)
                    navController.popBackStack("home", inclusive = false)
                },
                onDiscard = { navController.popBackStack() },
            )
        }
    }

    composable("history") {
        CaptureHistoryScreen { table, linkedId ->
            // Tap on a linked-log chip routes to the corresponding screen
            when (table) {
                "fuelLog"          -> navController.navigate("fuel/$linkedId")
                "mileageEntry"     -> navController.navigate("mileage/$linkedId")
                "chickenHouseLog"  -> navController.navigate("chicken_house/$linkedId")
            }
        }
    }

    composable("calibrate_gauge") {
        // Pass in a Bitmap from a freshly-captured photo, or load from CaptureEntity
        GaugeCalibrationScreen(
            viewModel = gaugeCalViewModel,
            initialPhoto = pendingCalibrationBitmap,
            onDone   = { navController.popBackStack() },
            onCancel = { navController.popBackStack() },
        )
    }
}
```

---

## Backend wiring

```bash
# 1. Append models from backend/prisma/schema.prisma
npx prisma migrate dev --name add_logs_and_gauges
npx prisma generate

# 2. Mount the router
```
```typescript
import logsRouter from './logs';
app.use('/api', logsRouter);
```

Full endpoint list (15 endpoints):

| Method | Path | Body |
|--------|------|------|
| GET | `/api/fuel-logs?farmId=&since=` | – |
| POST | `/api/fuel-logs` | `{capturedAtEpochMs, farmId?, totalCost?, gallons?, pricePerGallon?, odometerMiles?, sourceCaptureId?, notes?}` |
| DELETE | `/api/fuel-logs/:id` | – |
| GET | `/api/mileage?farmId=` | – |
| POST | `/api/mileage` | `{capturedAtEpochMs, odometerMiles, farmId?, sourceCaptureId?, notes?}` |
| DELETE | `/api/mileage/:id` | – |
| GET | `/api/chicken-house-logs?farmId=&since=` | – |
| POST | `/api/chicken-house-logs` | `{dateIso?, farmId?, metrics, sourceCaptureId?, notes?}` |
| DELETE | `/api/chicken-house-logs/:id` | – |
| GET | `/api/gauge-calibrations` | – |
| PUT | `/api/gauge-calibrations/:gaugeId` | full calibration |
| DELETE | `/api/gauge-calibrations/:gaugeId` | – |
| POST | `/api/captures` | multipart (already in fdp-vision) |
| GET | `/api/captures` | (already in fdp-vision) |
| GET | `/api/captures/:id/raw` | (already in fdp-vision) |

---

## End-to-end automated flow

You snap a photo at the shop fuel pump. Within 2 seconds:

1. **L0** — JPEG captured, GPS attached, archived to `/files/captures/<uuid>.jpg`.
2. **L1** — ML Kit OCR + classifier extract `{cost: 45.23, gallons: 12.345, ppg: 3.667}`. Triplet reconciliation passes (`12.345 × 3.667 = 45.27 ≈ 45.23`). ValidationGate → COMPLETE.
3. **L2 fan-out** — single transaction:
   - `INSERT INTO fuel_logs` → returns id 442
   - `INSERT INTO mileage_entries` (because the dashboard odo was visible) → 318
   - `UPDATE captures SET linkedEntities = '{"fuelLogId":442,"mileageEntryId":318}'`
4. **Sync** — `CaptureSyncWorker.enqueueImmediate` uploads the row + JPEG to the backend; `syncedAt` timestamp recorded on success.
5. **Dashboards refresh** — fuel-log Flow re-emits, mileage Flow re-emits, history list shows a new green-check capture.
6. **Audit trail** — open History tab → tap the new row → see the original photo, parsed JSON, and tappable links to the fuel and mileage entries it created. Either link opens the detail screen of the corresponding downstream log.

No manual data entry. No orphans. No data point uncoupled from its source photo.
