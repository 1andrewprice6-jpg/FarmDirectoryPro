# Farm Directory Pro - Quick Start Guide

## What's New in Version 2.0 🎉

Your Farm Directory app now includes:
- ✅ **Live WebSocket Connection** with status indicator
- ✅ **Comprehensive Settings** with ALL fields editable
- ✅ **Multi-Method Import** (Camera, Voice, Files, OCR, Email)
- ⏳ **Route Optimization** (coming soon)
- ⏳ **Multiple Attendance Methods** (coming soon)
- ⏳ **Farm Reconciliation** (coming soon)

## 🚀 Quick Setup (5 Minutes)

### Step 1: Open Settings
1. Launch Farm Directory app
2. Tap the **gear icon** (⚙️) in top right
3. You'll see the Settings screen

### Step 2: Configure Connection
```
Backend URL: http://10.0.2.2:4000
(Use 10.0.2.2 for Android Emulator, or your server IP for real device)

Farm ID: farm-001
(Your unique farm identifier)

Worker Name: Your Name
(How you'll appear to other workers)

✓ Auto-connect on startup
(Enable this for automatic connection)
```

Tap **Save** (💾 icon in top right)

### Step 3: Test Connection
1. Tap "Connect" button on the connection status card
2. Watch the status indicator:
   - 🔴 Red dot = Offline
   - 🟢 Green dot = Connected
3. Should show "Live (X workers)" when connected

### Step 4: Import Your Data
1. Go back to main screen
2. Tap "Import" button
3. Choose method:

**Option A: Import from File (Easiest)**
- Tap "Text File (CSV/JSON)"
- Select your farms.json or farmers.csv
- Data imports automatically

**Option B: Camera/QR Code**
- Tap "Camera / QR Code"
- Grant camera permission
- Scan QR code with farmer data

**Option C: Voice Input (Coolest)**
- Tap "Voice Input"
- Grant microphone permission
- Say: "Add farmer John Doe, farm name Green Acres, phone 555-1234"
- Data is parsed and added!

### Step 5: View Real-Time Updates
Once connected, you'll see:
- 📍 Location updates as cards
- 🚨 Health alerts as notifications
- 👥 Active worker count in top bar
- 🟢 Live sync indicator

## 📊 File Format Examples

### JSON Format (farmers.json)
```json
[
  {
    "name": "John Doe",
    "farmName": "Green Acres Farm",
    "address": "123 Farm Road, Rural County",
    "phone": "555-0100",
    "cellPhone": "555-0101",
    "email": "john@greenacres.com",
    "type": "Pullet",
    "spouse": "Jane Doe",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "healthStatus": "HEALTHY",
    "healthNotes": ""
  }
]
```

### CSV Format (farmers.csv)
```csv
name,farm_name,address,phone,cell_phone,email,type,latitude,longitude
John Doe,Green Acres,123 Farm Road,555-0100,555-0101,john@farm.com,Pullet,40.7128,-74.0060
Jane Smith,Happy Hens,456 Country Lane,555-0200,555-0201,jane@hens.com,Breeder,40.7580,-73.9855
```

### Text Format (farmers.txt)
```
name: John Doe
farm: Green Acres
address: 123 Farm Road
phone: 555-0100
---
name: Jane Smith
farm: Happy Hens
address: 456 Country Lane
phone: 555-0200
```

### QR Code Format
```
name=John Doe;farm=Green Acres;phone=555-0100;lat=40.7128;lon=-74.0060
```

## 🎤 Voice Commands

The voice input understands natural language:

**Adding a Farmer:**
- "Add farmer John Doe"
- "Add farmer John Doe, farm name Green Acres"
- "Add farmer John Doe, farm Green Acres, phone 555-1234"
- "Add farmer John Doe, farm Green Acres, phone 555-1234, address 123 Farm Road"

**Keywords Recognized:**
- farmer, name → Farmer name
- farm, farm name → Farm name  
- phone, telephone → Phone number
- address, location → Address
- lat, latitude → GPS latitude
- lon, longitude → GPS longitude

## 🔧 Settings Explained

### Connection Settings
| Setting | Description | Example |
|---------|-------------|---------|
| Backend URL | Your WebSocket server | http://10.0.2.2:4000 |
| Farm ID | Unique farm identifier | farm-001 |
| Worker Name | Your display name | John Doe |
| Auto-connect | Connect automatically on app start | ✓ Enabled |

### Synchronization
| Setting | Description | Default |
|---------|-------------|---------|
| Sync Interval | Time between syncs (ms) | 30000 (30 sec) |
| Enable Notifications | Show real-time alerts | ✓ Enabled |

### GPS & Location
| Setting | Description | Default |
|---------|-------------|---------|
| Enable GPS | Track location during visits | ✓ Enabled |
| GPS Accuracy | Accuracy in meters | 50 meters |

### Data Management
| Action | Description |
|--------|-------------|
| Export Data | Download all data as JSON |
| Clear Cache | Remove temporary files |

### Advanced Options
| Action | Description |
|--------|-------------|
| Test Connection | Ping server to verify connectivity |
| View Logs | See connection and error logs |
| Reset to Defaults | Restore original settings |

## 🐛 Troubleshooting

### Connection Won't Connect
1. Check Backend URL is correct
2. Ensure server is running
3. For emulator, use `http://10.0.2.2:PORT`
4. For real device, use actual IP (e.g., `http://192.168.1.100:PORT`)
5. Tap "Test Connection" in Advanced settings

### Import Fails
1. Check file format (JSON array or CSV with headers)
2. Ensure file has correct fields (name, farm_name, etc.)
3. Grant storage permissions
4. Try different import method

### GPS Not Working
1. Grant location permissions
2. Enable GPS in Settings
3. Ensure device location is enabled
4. Try increasing GPS accuracy in settings

### Voice Import Not Working
1. Grant microphone permission
2. Speak clearly and slowly
3. Use keywords: "farmer", "farm name", "phone"
4. Check Recent Imports to see if it was parsed

## 📱 App Permissions

The app needs these permissions:

| Permission | Used For |
|------------|----------|
| 📷 Camera | QR code scanning, photo verification |
| 🎤 Microphone | Voice input |
| 📁 Storage | File import/export |
| 📍 Location | GPS tracking, farm matching |
| 🌐 Internet | WebSocket connection, real-time sync |

Grant permissions when prompted or in:
Settings → Apps → Farm Directory → Permissions

## 🎯 Common Tasks

### Add a Single Farmer (Manually)
1. Tap "+" button
2. Fill in details
3. Tap "Save"

### Import Multiple Farmers
1. Prepare CSV or JSON file
2. Tap "Import"
3. Choose "Text File"
4. Select file
5. Confirm import

### Check Connection Status
Look at top bar:
- 🟢 + "Live (X workers)" = Connected
- 🔴 + "Offline" = Not connected

### Export All Data
1. Open Settings
2. Tap "Export Data"
3. File saved to Downloads folder

### View Import History
1. Open Import screen
2. Scroll to "Recent Imports" section
3. See all past imports with timestamps

## 🔥 Pro Tips

1. **Use Voice for Quick Adds**: When visiting farms, use voice input to add farmers hands-free

2. **Auto-Connect**: Enable auto-connect so you're always synced

3. **GPS Accuracy**: Lower number = more accurate, but uses more battery (50m is good balance)

4. **Import Templates**: Keep a template CSV/JSON file for easy bulk imports

5. **Backup Regularly**: Export data weekly to Downloads folder

6. **Test Connection First**: Before important work, test connection in Settings

## 📞 Need Help?

1. **View Logs**: Settings → Advanced → View Logs
2. **Test Connection**: Settings → Advanced → Test Connection
3. **Reset Settings**: Settings → Advanced → Reset to Defaults
4. **Export Before Changes**: Always export data before major operations

## 🎓 Next Features Coming Soon

- ✅ Multi-stop route optimization
- ✅ Multiple attendance methods (GPS, QR, NFC, etc.)
- ✅ Farm reconciliation with confidence scoring
- ✅ Comprehensive logs viewer
- ✅ Offline mode with sync queue
- ✅ Push notifications for all alerts

---

## 🏁 You're Ready!

Your Farm Directory Pro is now configured with:
- ✓ Live real-time sync
- ✓ Multiple import methods
- ✓ Comprehensive settings
- ✓ GPS tracking
- ✓ Health monitoring

**Start adding farmers and see real-time updates!** 🎉

---

**Version:** 2.0 Pro
**Date:** December 24, 2024
