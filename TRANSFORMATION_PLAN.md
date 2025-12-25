# 🚛 TRANSFORMATION PLAN: Farm Directory → Live Haul Pro

## 🎯 Executive Summary

**CURRENT APP**: Farm Directory Pro (farmer/farm management)  
**NEW APP**: Live Haul Pro (poultry & egg hauling logistics)

**Business Model:**
- **Two Divisions**: Chicken Hauling + Egg Hauling
- **Clients**: Perdue, Mountaire, other integrators
- **Personnel**: Boss, Office, Haulers, Crew Leaders, Catchers
- **Focus**: Real-time haul tracking, payroll, fleet management

---

## 📊 What to Keep vs Transform

### KEEP & ADAPT ✅
| Current Feature | Transform To |
|----------------|--------------|
| Farmer Database | → Farm Database (pickup locations) |
| GPS Reconciliation | → Haul GPS Tracking |
| Route Optimization | → Multi-farm pickup routing |
| Attendance Tracking | → Time Clock (crew/haulers) |
| Real-Time WebSocket | → Live haul status updates |
| Import Data | → Bulk farm/personnel import |
| Settings | → Company settings + user roles |
| Voice Agent | → Voice haul updates |

### REMOVE ❌
| Feature | Reason |
|---------|--------|
| "Farmer Details" screen | Not relevant |
| Health status tracking | Not needed for hauling |
| Spouse information | Irrelevant |
| Farm types (Pullet/Breeder) | → Broiler/Layer |

### ADD NEW 🆕
| Feature | Purpose |
|---------|---------|
| Haul Management | Core business logic |
| Role-Based Dashboards | Boss, Office, Hauler, Crew |
| Load Tracking | Bird/egg counts |
| Payroll Calculator | Hours + piece rate |
| Client Contracts | Perdue, Mountaire management |
| Fleet Map | Live hauler locations |
| Photo Upload | Load/delivery documentation |
| Weight Tickets | Plant receipts |

---

## 🏗️ New Architecture

### Database Schema Changes:

#### RENAME:
```
farmers → farms (pickup locations)
FarmEntity → Farm
```

#### NEW TABLES:
```
hauls
personnel
clients
destinations
time_entries
payroll_records
gps_locations
haul_events
crews
```

#### MODIFY FARMS TABLE:
```kotlin
// OLD
data class FarmEntity(
    name: String,         // Farmer name
    farmName: String,     // Farm name
    healthStatus: String  // REMOVE
)

// NEW
data class Farm(
    farmName: String,           // Farm name
    farmNumber: String,         // Perdue #12345
    clientId: Int,             // Perdue, Mountaire
    farmType: FarmType,        // BROILER or LAYER
    farmerName: String,        // Grower name
    houseCount: Int,           // # of houses
    totalCapacity: Int,        // Max birds
    preferredPickupTime: String
)
```

---

## 📱 New Screen Structure

### CURRENT (9 screens):
1. Farmer List
2. Farmer Details
3. Add/Edit Farmer
4. Settings
5. Import Data
6. Reconcile
7. Attendance
8. Route Optimization
9. Logs
10. Smart Agents

### NEW (15 screens):

#### PUBLIC/AUTH:
1. **Login Screen** - Role selection + PIN

#### BOSS ROLE (Full Access):
2. **Boss Dashboard** - Fleet overview, today's hauls, revenue
3. **Fleet Map** - Live locations of all haulers
4. **Performance Analytics** - Hauler/crew stats

#### OFFICE ROLE:
5. **Dispatch Dashboard** - Create/assign hauls
6. **Schedule Calendar** - Weekly haul planning
7. **Payroll Screen** - Process weekly payroll

#### HAULER ROLE (Chicken):
8. **My Hauls** - Today's assignments
9. **Active Haul** - Current haul details
10. **Bird Count Entry** - Load tracking
11. **Upload Docs** - Photos, weight tickets

#### HAULER ROLE (Egg):
12. **My Hauls** - Today's assignments
13. **Active Haul** - Current haul details
14. **Case Count Entry** - Load tracking

#### CREW LEADER:
15. **Crew Management** - Roster, clock in/out
16. **Catch Log** - House-by-house tracking

#### CATCHER:
17. **Time Clock** - Clock in/out
18. **My Hours** - Weekly summary

#### SHARED:
19. **Farms** - Pickup locations (adapted from current)
20. **Clients** - Perdue, Mountaire, etc.
21. **Settings** - Adapted from current
22. **Reports** - New reporting system

---

## 🔄 Migration Steps

### PHASE 1: DATA MODEL (Day 1-2)
```
✅ Create new data models (DONE - see LIVE_HAUL_DATA_MODELS.kt)
□ Create Room database schema
□ Create migration from old schema
□ Test data persistence
```

### PHASE 2: CORE HAUL (Day 3-5)
```
□ Create Haul entity & DAO
□ Build Haul creation screen
□ Build Haul list screen
□ Build Active Haul screen
□ Implement status workflow
```

### PHASE 3: PERSONNEL (Day 6-7)
```
□ Create Personnel entity & DAO
□ Build login/role selection
□ Build personnel management
□ Implement time clock
```

### PHASE 4: GPS TRACKING (Day 8-9)
```
□ Adapt existing GPS code
□ Build real-time location service
□ Create fleet map screen
□ Add geofencing
```

### PHASE 5: PAYROLL (Day 10-11)
```
□ Build payroll calculator
□ Create weekly payroll screen
□ Add export functionality
□ Test calculations
```

### PHASE 6: DASHBOARDS (Day 12-13)
```
□ Boss dashboard
□ Office dispatch dashboard
□ Hauler dashboards
□ Crew dashboards
```

### PHASE 7: POLISH (Day 14)
```
□ UI refinements
□ Testing
□ Bug fixes
□ Documentation
```

---

## 🎨 UI Theme Updates

### Brand Identity:
```
OLD: Farm Directory (🌾 green/nature theme)
NEW: Live Haul Pro (🚛 blue/orange logistics theme)

Primary Color: #1976D2 (Blue - trust, logistics)
Secondary Color: #FF6F00 (Orange - energy, chicken)
Accent: #4CAF50 (Green - success)
Error: #D32F2F (Red - alerts)
```

### Icons:
```
OLD                    NEW
🏠 Home               → 🚛 My Hauls
📍 Reconcile          → 📦 Load Tracking
✅ Attendance         → ⏰ Time Clock
🗺️ Routes             → 🗺️ Fleet Map
📋 Logs               → 📊 Reports
```

---

## 🔐 Role-Based Access Control

### Permissions Matrix:

| Feature | Boss | Office | Dispatcher | Chicken Hauler | Egg Hauler | Crew Leader | Catcher |
|---------|------|--------|-----------|----------------|------------|-------------|---------|
| View All Hauls | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Create Haul | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Assign Personnel | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| View My Hauls | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Update Haul Status | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Enter Counts | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| Clock In/Out | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ |
| Process Payroll | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| View Reports | ✅ | ✅ | ✅ | ⚠️ Limited | ⚠️ Limited | ⚠️ Limited | ❌ |
| Manage Personnel | ✅ | ✅ | ❌ | ❌ | ❌ | ⚠️ Crew Only | ❌ |
| Fleet Map | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |

---

## 📋 Critical Configuration

### Company Settings:
```kotlin
data class CompanySettings(
    // Company Info
    val companyName: String,
    val dotNumber: String,
    val mcNumber: String,
    
    // Pay Rates (defaults)
    val chickenHaulerRate: Double,
    val eggHaulerRate: Double,
    val crewLeaderRate: Double,
    val catcherRate: Double,
    
    // Payroll
    val payrollCycle: PayrollCycle, // WEEKLY, BI_WEEKLY
    val overtimeThreshold: Double, // 40 hours
    
    // Haul Defaults
    val avgBirdWeight: Double, // lbs
    val eggsPerCase: Int, // 15 dozen
    
    // GPS
    val trackingInterval: Int, // seconds
    val geofenceRadius: Int, // meters
    
    // Notifications
    val delayAlertMinutes: Int,
    val adminPhoneNumber: String
)
```

### Client Setup (Pre-loaded):
```kotlin
val PERDUE = Client(
    name = "Perdue Farms",
    type = ClientType.CHICKEN_PROCESSOR,
    contactName = "Perdue Receiving",
    contactPhone = "1-800-PERDUE",
    payRatePerBird = 0.12 // $0.12 per bird
)

val MOUNTAIRE = Client(
    name = "Mountaire Farms",
    type = ClientType.CHICKEN_PROCESSOR,
    contactName = "Mountaire Receiving",
    contactPhone = "1-800-MOUNT",
    payRatePerBird = 0.11
)
```

---

## 🚀 Quick Start Implementation

### Step 1: Update App Name & Package
```kotlin
// build.gradle.kts
android {
    namespace = "com.example.livehaul"
    applicationId = "com.example.livehaul"
}
```

### Step 2: Replace Data Models
```
Replace: data/FarmEntity.kt
With: data/Farm.kt, data/Haul.kt, data/Personnel.kt
```

### Step 3: Update Database
```kotlin
@Database(
    entities = [
        Haul::class,
        Personnel::class,
        Farm::class,
        Client::class,
        Destination::class,
        TimeEntry::class,
        PayrollRecord::class,
        GPSLocation::class,
        HaulEvent::class
    ],
    version = 2
)
```

### Step 4: Create New Screens
```
Priority Order:
1. LoginScreen.kt
2. HaulListScreen.kt
3. CreateHaulScreen.kt
4. ActiveHaulScreen.kt
5. TimeClockScreen.kt
```

---

## 📊 Success Metrics

### Before (Farm Directory):
- Manage farmer contacts
- GPS reconciliation
- Basic attendance

### After (Live Haul Pro):
- ✅ Real-time haul tracking
- ✅ Automated payroll calculations
- ✅ Fleet GPS monitoring
- ✅ Bird/egg count accuracy
- ✅ Client delivery tracking
- ✅ Crew time management
- ✅ Performance analytics

---

## 🎯 NEXT IMMEDIATE ACTIONS:

1. **Confirm Requirements** - Review with stakeholders
2. **Start Data Migration** - Implement new database
3. **Build Core Screens** - Login, Haul List, Active Haul
4. **GPS Integration** - Adapt existing tracking
5. **Payroll Logic** - Build calculator
6. **Test with Real Data** - Pilot with one hauler

---

**Ready to transform Farm Directory → Live Haul Pro!** 🚛🐔🥚

What should we build first?

A) Haul Management (core feature)
B) Role-Based Login
C) GPS Fleet Tracking
D) Payroll System

Pick one and I'll start building! 🚀
