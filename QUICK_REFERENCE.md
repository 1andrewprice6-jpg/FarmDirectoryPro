# WebSocket Error Handling - Quick Reference

## Connection States

| State | Color | Icon | Meaning |
|-------|-------|------|---------|
| DISCONNECTED | Gray | ⚪ | Not connected to backend |
| CONNECTING | Blue | 🔵 (spinner) | Attempting initial connection |
| CONNECTED | Green | 🟢 | Successfully connected |
| RECONNECTING | Yellow | 🟡 (spinner) | Attempting to reconnect |
| ERROR | Red | 🔴 | Connection error occurred |

## Error Types

| Error Type | Common Cause | User Action |
|------------|--------------|-------------|
| ConnectionFailed | Network issues, wrong URL | Check settings, retry |
| BackendOffline | Server not running | Wait or contact admin |
| NetworkError | No internet | Check network, retry |
| TimeoutError | Slow network | Retry, check connection |
| JoinFarmFailed | Invalid farm ID | Verify farm ID |
| InvalidToken | Auth issue | Re-authenticate |
| UnknownError | Various | Retry, check logs |

## UI Components

### Error Dialog
- **When**: Major errors occur
- **Actions**: Retry or Dismiss
- **Location**: Modal overlay

### Error Banner
- **When**: Connection errors in main screen
- **Actions**: Retry button included
- **Location**: Below search bar

### Success Snackbar
- **When**: Successful operations
- **Actions**: Auto-dismisses (3s)
- **Location**: Bottom of screen

### Status Indicator
- **When**: Always visible
- **Actions**: Click to view details
- **Location**: TopAppBar (under title)

## Common Issues & Solutions

### "Backend server is offline"
```
Cause: Server not running
Solution:
1. Check backend status
2. Start backend: pm2 start skeleton-key
3. Click "Retry" in app
```

### "Connection timed out"
```
Cause: Network slow or URL wrong
Solution:
1. Check internet connection
2. Verify backend URL in Settings
3. Reduce network load
4. Click "Retry"
```

### "Failed to join farm"
```
Cause: Invalid farm ID
Solution:
1. Open Settings
2. Verify Farm ID exists in backend
3. Update Farm ID
4. Reconnect
```

### Stuck in "Reconnecting..."
```
Cause: Auto-retry in progress
Solution:
1. Wait for retries to complete (5 attempts)
2. OR manually disconnect and retry
3. Check backend is accessible
```

### Loading spinner won't clear
```
Cause: Operation timeout or error
Solution:
1. Wait 10 seconds for timeout
2. Force close and restart app
3. Check logs for errors
```

## Retry Logic

### Automatic Retry
- **Trigger**: Connection loss
- **Attempts**: 5 maximum
- **Delays**: 1s, 2s, 4s, 8s, 16s
- **After 5 attempts**: Manual retry required

### Manual Retry
- **Location**: Error dialog, error banner, Settings
- **When to use**: After auto-retry fails
- **Effect**: Resets retry count, attempts connection

## Settings Configuration

### Backend URL
- **Default**: `http://10.0.2.2:4000` (emulator)
- **Format**: `http://IP:PORT` or `https://domain:PORT`
- **Validation**: Checked on connect

### Farm ID
- **Required**: Yes
- **Format**: Alphanumeric string
- **Example**: `farm-123`, `main-farm`

### Worker Name
- **Required**: Yes
- **Format**: Any text
- **Example**: `John Doe`, `Worker 1`

### Auto-connect
- **Default**: Off
- **Effect**: Connects automatically on app start

## Keyboard Shortcuts

### In Settings
- **Connect**: Alt + C
- **Disconnect**: Alt + D
- **Retry**: Alt + R

### In Error Dialog
- **Retry**: Enter
- **Dismiss**: Escape

## Activity Logs

### View Logs
1. Main screen
2. Bottom navigation
3. Click "Logs" tab

### Log Categories
- **WebSocket**: Connection events
- **Farmer**: Farmer operations
- **Import**: Data imports
- **Reconcile**: GPS reconciliation
- **Route**: Route optimization
- **Attendance**: Check-in/out

### Log Levels
- **INFO** (🔵): Normal operations
- **SUCCESS** (🟢): Successful operations
- **WARNING** (🟡): Non-critical issues
- **ERROR** (🔴): Critical errors

### Export Logs
1. Open Logs screen
2. Click share icon
3. Select export method

## Testing Quick Commands

### Test Backend Offline
```bash
# Stop backend
pm2 stop all

# Start app, try to connect
# Expected: "Backend server is offline" error
```

### Test Network Disconnect
```bash
# Connect app successfully
# Enable airplane mode
# Wait for reconnection attempts
# Disable airplane mode
# Expected: Auto-reconnection succeeds
```

### Test Timeout
```bash
# In Settings, set URL to: http://192.168.1.250:4000
# Click Connect
# Wait 10+ seconds
# Expected: "Connection timed out" error
```

### Test Invalid Farm ID
```bash
# Connect successfully
# Set Farm ID to: non-existent-farm
# Reconnect
# Expected: "Failed to join farm" error
```

## Performance Metrics

### Connection Times
- **Normal**: < 2 seconds
- **Slow network**: < 10 seconds
- **Timeout**: 10 seconds

### Retry Times
- **Attempt 1**: 1 second delay
- **Attempt 2**: 2 seconds delay
- **Attempt 3**: 4 seconds delay
- **Attempt 4**: 8 seconds delay
- **Attempt 5**: 16 seconds delay
- **Total**: ~31 seconds for all attempts

### UI Response
- **State update**: Immediate
- **Error display**: < 100ms
- **Loading spinner**: Immediate

## Debugging Tips

### Enable Verbose Logging
```kotlin
// In FarmWebSocketService.kt
private val TAG = "FarmWebSocket"

// Android Studio Logcat filter:
tag:FarmWebSocket
```

### Common Log Messages
```
✅ Connected to farm monitoring
❌ Disconnected from farm monitoring
🔄 Reconnection attempt N
📍 Location update: entity-123
🏥 Health alert: entity-456
🚨 CRITICAL ALERT: entity-789
```

### Check Connection State
```kotlin
// In ViewModel or Activity
Log.d("Debug", "State: ${viewModel.connectionState.value}")
Log.d("Debug", "Loading: ${viewModel.isLoading.value}")
Log.d("Debug", "Error: ${viewModel.errorMessage.value}")
```

## API Reference

### ViewModel Functions
```kotlin
// Connection
viewModel.connectToBackend()
viewModel.disconnectFromBackend()
viewModel.retryConnection()

// Join farm
viewModel.joinFarm(farmId, workerId, workerName) { success ->
    if (success) { /* joined */ }
}

// Clear messages
viewModel.clearErrorMessage()
viewModel.clearSuccessMessage()

// Update operations
viewModel.updateFarmerLocation(farmerId, lat, lng, workerId)
viewModel.updateFarmerHealth(farmerId, status, notes, workerId)
```

### State Flows
```kotlin
// Connection
viewModel.connectionState.collectAsState()
viewModel.isConnected.collectAsState()
viewModel.isLoading.collectAsState()

// Messages
viewModel.errorMessage.collectAsState()
viewModel.successMessage.collectAsState()
viewModel.connectionErrorMessage.collectAsState()

// Events
viewModel.healthAlerts.collect { alert -> }
viewModel.criticalAlerts.collect { alert -> }
```

## Troubleshooting Flowchart

```
Connection Issue?
    ↓
Check Backend Running?
    ↓ Yes
Check Network Connection?
    ↓ Yes
Check Backend URL?
    ↓ Correct
Check Logs for Error Type
    ↓
Follow Solution for Error Type
    ↓
Still Failing?
    ↓
1. Clear app data
2. Restart app
3. Check backend logs
4. Contact support
```

## Support Checklist

When asking for help, provide:
- [ ] Device/emulator details
- [ ] Backend URL (without credentials)
- [ ] Connection state when error occurred
- [ ] Error message shown
- [ ] Activity logs (export from app)
- [ ] Screenshots of error
- [ ] Steps to reproduce
- [ ] Network conditions
- [ ] Backend status (running/stopped)

## Version Info

- **Implementation Version**: 1.0
- **Last Updated**: December 25, 2025
- **Compatibility**: Android 8.0+ (API 26+)
- **Backend**: Skeleton Key (Socket.IO)

## Additional Resources

- **Full Documentation**: WEBSOCKET_ERROR_HANDLING.md
- **Test Scenarios**: ERROR_HANDLING_TEST_SCENARIOS.md
- **Implementation Summary**: IMPLEMENTATION_SUMMARY.md
- **Backend Docs**: /skeleton-key/README.md
