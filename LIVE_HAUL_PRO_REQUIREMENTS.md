# 🚛 LIVE HAUL PRO - Poultry & Egg Hauling Management System

## Business Requirements - COMPLETE REDESIGN

### THE BUSINESS:
**Two-Sided Live Haul Operation:**

#### Side 1: CHICKEN HAULING (Live Birds) 🐔
- Live bird transport from farms to processing plants
- Contracts with: Perdue, Mountaire, other integrators
- **Crews**: Leaders + Catchers
- Real-time tracking during hauls
- Bird count & weight tracking

#### Side 2: EGG HAULING 🥚
- Egg transport from layer farms to processing
- Temperature-sensitive loads
- Case/flat counting
- Quality control tracking

### PERSONNEL STRUCTURE:
```
OFFICE
├── Boss/Owner
├── Office Manager
├── Payroll Administrator
└── Dispatcher

CHICKEN HAULING DIVISION
├── Chicken Haulers (Drivers)
└── Catching Crews
    ├── Crew Leaders
    └── Catchers

EGG HAULING DIVISION
└── Egg Haulers (Drivers)
```

### CLIENT CONTRACTS:
- **Perdue** - Chicken processing
- **Mountaire** - Chicken processing
- Other integrators
- Layer farms (egg pickup)

---

## 🎯 Core Features Needed

### 1. **Haul Management** (PRIMARY)
- Create new haul jobs
- Assign haulers/crews
- Track haul status (Assigned → En Route → Loading → Hauling → Delivered)
- Bird counts & weights
- Egg case counts
- Live GPS tracking during hauls
- Time tracking (pickup time, delivery time)

### 2. **Personnel Management**
- Boss dashboard (overview of everything)
- Office staff (dispatch, scheduling, payroll prep)
- Hauler profiles (chicken vs egg haulers)
- Crew management (leaders + catchers)
- Availability tracking
- Contact information

### 3. **Payroll Tracking**
- Hours worked per haul
- Piece rate (per bird, per case)
- Crew splits (leader vs catcher rates)
- Weekly/bi-weekly totals
- Export to payroll system

### 4. **Farm/Client Management**
- Farm locations (GPS coordinates)
- Client contracts (Perdue, Mountaire)
- Pickup schedules
- Farm contact information
- Historical haul data

### 5. **Real-Time Tracking**
- Live location of haulers
- Haul progress updates
- Delay notifications
- ETA calculations
- Fleet overview map

### 6. **Reporting & Analytics**
- Daily haul summaries
- Weekly performance reports
- Hauler performance metrics
- Crew productivity
- Client delivery stats
- Payroll reports

---

## 📱 NEW App Structure

### User Roles:
1. **BOSS** - Full access, overview dashboard
2. **OFFICE** - Dispatch, scheduling, reports
3. **CHICKEN HAULER** - View/update chicken hauls
4. **EGG HAULER** - View/update egg hauls
5. **CREW LEADER** - Manage crew, update catches
6. **CATCHER** - Clock in/out, view assignments

---

## 🔄 Haul Workflow

### Chicken Haul:
```
1. DISPATCH → Create haul job
   - Farm location
   - Destination (Perdue/Mountaire plant)
   - Estimated bird count
   - Assign hauler + crew

2. CREW LEADER → Accept assignment
   - View farm details
   - Assemble crew
   - En route to farm

3. LOADING → Catching process
   - Clock in crew
   - Count birds as loaded
   - Track time per house
   - Photos (optional)

4. HAULING → In transit
   - Live GPS tracking
   - ETA updates
   - Temperature monitoring

5. DELIVERY → Plant arrival
   - Final bird count
   - Weight ticket
   - Plant inspector sign-off
   - Upload docs

6. PAYROLL → Auto-calculate
   - Hauler pay
   - Crew leader pay
   - Catcher pay (split)
```

### Egg Haul:
```
1. DISPATCH → Create haul job
   - Layer farm location
   - Destination (processing plant)
   - Expected case count

2. EGG HAULER → Accept assignment
   - View farm details
   - En route

3. LOADING → Egg pickup
   - Count flats/cases
   - Check temperature
   - Quality inspection
   - Load photos

4. HAULING → In transit
   - Live GPS tracking
   - Temperature monitoring
   - Careful driving alerts

5. DELIVERY → Plant arrival
   - Final count
   - Quality check
   - Receiving ticket
   - Upload docs

6. PAYROLL → Auto-calculate
   - Hauler pay (per case)
```

---

## 🎨 NEW UI Screens

### 1. **Login/Role Selection**
- Select role: Boss, Office, Hauler, Crew Leader, Catcher
- PIN/Password authentication

### 2. **BOSS DASHBOARD**
- Fleet map (all active hauls)
- Today's haul summary
- Hauler status grid
- Revenue tracking
- Quick access to all features

### 3. **OFFICE DISPATCH**
- Create new haul
- Assign personnel
- View schedule (calendar view)
- Active hauls list
- Communication center

### 4. **HAULER APP (Chicken)**
- My assignments
- Active haul details
- Bird count entry
- GPS tracking toggle
- Upload photos/docs
- Time clock

### 5. **HAULER APP (Egg)**
- My assignments
- Active haul details
- Case count entry
- Temperature log
- GPS tracking
- Upload docs

### 6. **CREW LEADER APP**
- View assignment
- Manage crew roster
- Clock in/out crew
- Bird count tracking
- House-by-house logging

### 7. **CATCHER APP**
- Clock in/out
- View today's assignments
- Hours worked
- Week total preview

### 8. **PAYROLL SCREEN**
- Week selection
- Personnel list
- Hours/pieces breakdown
- Rate calculations
- Export CSV for payroll

### 9. **FARMS/CLIENTS**
- Farm list (GPS mapped)
- Client contracts
- Contact info
- Haul history
- Notes

### 10. **REPORTS**
- Daily summary
- Weekly performance
- Client reports
- Hauler stats
- Crew productivity

---

## 📊 NEW Data Models

### Haul
```kotlin
data class Haul(
    val id: Int,
    val type: HaulType, // CHICKEN or EGG
    val status: HaulStatus,
    val farmId: Int,
    val destinationId: Int,
    val haulerId: Int,
    val crewLeaderId: Int?,
    val catcherIds: List<Int>,
    val scheduledTime: Long,
    val startTime: Long?,
    val loadStartTime: Long?,
    val loadEndTime: Long?,
    val deliveryTime: Long?,
    val birdCount: Int?,
    val totalWeight: Double?,
    val caseCount: Int?,
    val temperature: Double?,
    val notes: String,
    val clientId: Int, // Perdue, Mountaire, etc.
    val payrollProcessed: Boolean
)

enum class HaulType {
    CHICKEN, EGG
}

enum class HaulStatus {
    SCHEDULED, ASSIGNED, EN_ROUTE, LOADING, 
    HAULING, DELIVERED, COMPLETED, CANCELLED
}
```

### Personnel
```kotlin
data class Personnel(
    val id: Int,
    val name: String,
    val role: PersonnelRole,
    val phone: String,
    val email: String,
    val hireDate: Long,
    val payRate: Double,
    val payType: PayType,
    val active: Boolean,
    val licenseNumber: String?, // For haulers
    val crewId: Int? // For crew members
)

enum class PersonnelRole {
    BOSS, OFFICE, DISPATCHER,
    CHICKEN_HAULER, EGG_HAULER,
    CREW_LEADER, CATCHER
}

enum class PayType {
    HOURLY, PER_BIRD, PER_CASE, SALARY
}
```

### Farm/Client
```kotlin
data class Farm(
    val id: Int,
    val name: String,
    val farmNumber: String,
    val clientId: Int, // Perdue, Mountaire
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contactName: String,
    val contactPhone: String,
    val houseCount: Int,
    val farmType: FarmType,
    val notes: String
)

enum class FarmType {
    BROILER, LAYER
}

data class Client(
    val id: Int,
    val name: String, // "Perdue", "Mountaire"
    val type: ClientType,
    val contactName: String,
    val contactPhone: String,
    val plantLocations: List<PlantLocation>
)

enum class ClientType {
    CHICKEN_PROCESSOR, EGG_PROCESSOR
}
```

### TimeEntry
```kotlin
data class TimeEntry(
    val id: Int,
    val personnelId: Int,
    val haulId: Int,
    val clockInTime: Long,
    val clockOutTime: Long?,
    val totalHours: Double,
    val entryType: TimeEntryType
)

enum class TimeEntryType {
    HAULING, CATCHING, OFFICE
}
```

### PayrollRecord
```kotlin
data class PayrollRecord(
    val id: Int,
    val personnelId: Int,
    val weekEnding: Long,
    val totalHours: Double,
    val totalPieces: Int, // Birds or cases
    val grossPay: Double,
    val hauls: List<Int>, // Haul IDs
    val processed: Boolean,
    val paidDate: Long?
)
```

---

## 🚀 Implementation Priority

### PHASE 1: CORE HAUL MANAGEMENT (Week 1)
- [ ] Haul creation & assignment
- [ ] Hauler app (basic)
- [ ] GPS tracking
- [ ] Bird/case counting
- [ ] Status updates

### PHASE 2: CREW & PAYROLL (Week 2)
- [ ] Crew management
- [ ] Time clock
- [ ] Payroll calculations
- [ ] Export functionality

### PHASE 3: OFFICE & DISPATCH (Week 3)
- [ ] Dispatch dashboard
- [ ] Scheduling calendar
- [ ] Farm/client management
- [ ] Communication system

### PHASE 4: BOSS FEATURES (Week 4)
- [ ] Boss dashboard
- [ ] Fleet map
- [ ] Reports & analytics
- [ ] Revenue tracking

### PHASE 5: ADVANCED FEATURES
- [ ] Route optimization
- [ ] Predictive scheduling
- [ ] Performance analytics
- [ ] Client portal

---

## 🔧 Key Integrations Needed

### 1. **GPS Tracking**
- Real-time location updates
- Geofencing (farm arrival/departure)
- Route history

### 2. **Photo Upload**
- Load documentation
- Weight tickets
- Delivery receipts

### 3. **Communication**
- In-app messaging
- Push notifications
- Emergency alerts

### 4. **Payroll Export**
- CSV export for QuickBooks
- PDF reports
- Email distribution

### 5. **Client Integration** (Future)
- Perdue API integration
- Mountaire systems
- Automated scheduling

---

## 📋 Critical Questions to Answer

1. **Payroll Rates:**
   - Chicken hauler: $ per bird? Hourly?
   - Egg hauler: $ per case? Hourly?
   - Crew leader: $ per bird + bonus?
   - Catcher: $ per bird split among crew?

2. **Scheduling:**
   - How far in advance are hauls scheduled?
   - Do farms have preferred time windows?
   - How many hauls per day per hauler?

3. **Equipment:**
   - How many trucks?
   - Coops per truck?
   - Temperature monitoring devices?

4. **Compliance:**
   - DOT regulations?
   - Food safety requirements?
   - Animal welfare standards?

---

This is a **MAJOR PIVOT** from farm directory to live haul logistics!

Should I proceed with creating the new screens and data models?

**Next steps:**
1. Confirm business requirements
2. Create haul management screens
3. Build role-based dashboards
4. Implement GPS tracking
5. Add payroll calculations

What should I prioritize first? 🚛🐔🥚
