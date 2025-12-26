# WebSocket Error Handling and Loading States - Implementation Guide

## Overview
This document describes the comprehensive error handling, retry logic, and loading state management implemented for the WebSocket connection between the Jetpack Compose frontend and the Skeleton Key backend.

## Architecture

### 1. FarmWebSocketService (Enhanced)
**Location:** `/app/src/main/java/com/example/farmdirectoryupgraded/data/FarmWebSocketService.kt`

#### New Features

##### Connection States
```kotlin
enum class ConnectionState {
    DISCONNECTED,    // Not connected
    CONNECTING,      // Initial connection attempt
    CONNECTED,       // Successfully connected
    RECONNECTING,    // Attempting to reconnect
    ERROR            // Connection error
}
```

##### Error Types
```kotlin
sealed class WebSocketError {
    data class ConnectionFailed(val message: String, val cause: Throwable? = null)
    data class JoinFarmFailed(val farmId: String, val reason: String)
    data class NetworkError(val message: String)
    data class TimeoutError(val operation: String)
    data class InvalidToken(val message: String)
    data class BackendOffline(val backendUrl: String)
    data class UnknownError(val message: String)
}
```

##### State Flows
- `connectionState: StateFlow<ConnectionState>` - Current connection state
- `isConnected: StateFlow<Boolean>` - Legacy boolean connection state
- `isLoading: StateFlow<Boolean>` - Loading indicator for operations
- `errors: SharedFlow<WebSocketError>` - Stream of error events
- `connectionErrorMessage: StateFlow<String?>` - Human-readable error message

##### Retry Logic
- **Exponential Backoff**: 1s, 2s, 4s, 8s, 16s, max 30s
- **Max Retries**: 5 attempts
- **Auto-reset**: Retry count resets on successful connection

##### Error Handling Features
1. **Connection Errors**
   - Timeout detection (10 second timeout)
   - Backend offline detection (ECONNREFUSED)
   - Invalid URL detection
   - Network error detection

2. **Join Farm Errors**
   - Timeout handling (10 second timeout)
   - Empty response detection
   - Server rejection handling

3. **Operation Errors**
   - Location update error handling
   - Health update error handling
   - Automatic error emission

##### Event Listeners
Enhanced with comprehensive error handling for all Socket.IO events:
- `EVENT_CONNECT` - Reset retry count, update state
- `EVENT_DISCONNECT` - Detect intentional vs error disconnect
- `EVENT_CONNECT_ERROR` - Parse and categorize errors
- `EVENT_RECONNECT` - Reset state on successful reconnection
- `EVENT_RECONNECT_ATTEMPT` - Update UI with attempt number
- `EVENT_RECONNECT_ERROR` - Log reconnection failures
- `EVENT_RECONNECT_FAILED` - Emit final failure error

### 2. FarmerViewModel (Enhanced)
**Location:** `/app/src/main/java/com/example/farmdirectoryupgraded/viewmodel/FarmerViewModel.kt`

#### New Features

##### State Management
```kotlin
// Connection states
val connectionState: StateFlow<ConnectionState>
val isLoading: StateFlow<Boolean>
val connectionErrorMessage: StateFlow<String?>

// User-facing messages
val errorMessage: StateFlow<String?>
val successMessage: StateFlow<String?>
```

##### Error Collection
```kotlin
private fun collectWebSocketErrors() {
    viewModelScope.launch {
        webSocketService.errors.collect { error ->
            val errorMsg = when (error) {
                is WebSocketError.ConnectionFailed ->
                    "Connection failed: ${error.message}"
                is WebSocketError.BackendOffline ->
                    "Backend server is offline (${error.backendUrl})"
                // ... other error types
            }
            _errorMessage.value = errorMsg
            addLog("WebSocket", "ERROR", "WebSocket error", errorMsg)
        }
    }
}
```

##### Enhanced Operations
1. **Connect with Error Handling**
   ```kotlin
   fun connectToBackend() {
       viewModelScope.launch {
           try {
               webSocketService.connect()
               addLog("WebSocket", "INFO", "Connecting to backend", "")
           } catch (e: Exception) {
               _errorMessage.value = "Failed to connect: ${e.message}"
               addLog("WebSocket", "ERROR", "Connection failed", e.message ?: "")
           }
       }
   }
   ```

2. **Retry Connection**
   ```kotlin
   fun retryConnection() {
       viewModelScope.launch {
           try {
               webSocketService.retryConnection()
               addLog("WebSocket", "INFO", "Retrying connection", "")
           } catch (e: Exception) {
               _errorMessage.value = "Retry failed: ${e.message}"
           }
       }
   }
   ```

3. **Join Farm with Callback**
   ```kotlin
   fun joinFarm(farmId: String, workerId: String, workerName: String,
                onResult: ((Boolean) -> Unit)? = null) {
       // Handles success/failure and invokes callback
   }
   ```

##### Event Processing
- Location updates automatically sync to local database
- Health alerts logged and emitted
- Critical alerts logged with ERROR level
- Worker presence changes logged

### 3. MainActivity UI (Enhanced)
**Location:** `/app/src/main/java/com/example/farmdirectoryupgraded/MainActivity.kt`

#### New Features

##### Error Dialog
```kotlin
if (showErrorDialog && errorMessage != null) {
    AlertDialog(
        onDismissRequest = { /* Clear error */ },
        icon = { Icon(Icons.Default.Error, ...) },
        title = { Text("Connection Error") },
        text = {
            Column {
                Text(errorMessage!!)
                Text("Connection state: ${connectionState.name}")
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.retryConnection() }) {
                Icon(Icons.Default.Refresh, ...)
                Text("Retry")
            }
        },
        dismissButton = {
            TextButton(onClick = { /* Dismiss */ }) {
                Text("Dismiss")
            }
        }
    )
}
```

##### Success Snackbar
- Auto-dismisses after 3 seconds
- Shows connection success, join farm success, etc.
- Green primary container color

##### Connection Status Indicator (TopAppBar)
Shows different states with visual feedback:
- **Connected**: Green dot + "Live (X workers)"
- **Connecting**: Loading spinner + "Connecting..."
- **Reconnecting**: Blue dot + "Reconnecting..."
- **Error**: Red dot + "Error"
- **Disconnected**: Gray dot + "Offline"

##### Error Banners in List Screen
1. **Connection Error Banner**
   - Red error container
   - Shows error message
   - Retry button

2. **Reconnecting Banner**
   - Yellow/secondary container
   - Shows loading spinner
   - "Reconnecting to backend..." message

### 4. Settings Screen (Enhanced)
**Location:** `/app/src/main/java/com/example/farmdirectoryupgraded/ui/Screens.kt`

#### New Features

##### Connection Status Card
Shows detailed connection information:
- Current state with color coding
- Loading spinner during operations
- Error messages if present

##### Connection Controls
- **Connect/Disconnect Button**: Disabled during loading
- **Retry Button**: Only shown when disconnected
- Both buttons respect loading state

##### Error Display
- Last error shown in red error card
- Only visible when error present
- Clears when connection successful

## Testing Scenarios

### 1. Backend Offline
**Test:** Stop the backend server before connecting

**Expected Behavior:**
- Connection state changes: DISCONNECTED → CONNECTING → ERROR
- Error dialog shows: "Backend server is offline"
- Retry button appears
- Error logged to activity logs

**Verification:**
```bash
# Stop backend
pm2 stop all

# In app: Try to connect
# Should see error dialog and banner
```

### 2. Network Disconnected
**Test:** Disable network after connection

**Expected Behavior:**
- Connection state changes: CONNECTED → DISCONNECTED
- Auto-reconnection attempts with exponential backoff
- Reconnecting banner shows
- After 5 failed attempts, shows error

**Verification:**
```bash
# Connect app first
# Then disable WiFi/mobile data
# Watch reconnection attempts in logs
```

### 3. Invalid Backend URL
**Test:** Enter invalid URL in settings (e.g., "invalid-url")

**Expected Behavior:**
- Error dialog: "Invalid backend URL"
- Connection state: ERROR
- No retry attempts (invalid config)

**Verification:**
- Settings → Backend URL → "invalid-url" → Connect
- Should immediately show error

### 4. Invalid Farm ID
**Test:** Try to join farm with non-existent ID

**Expected Behavior:**
- Join farm fails with timeout or server rejection
- Error: "Failed to join farm: [reason]"
- Connection remains active but farm not joined

**Verification:**
- Connect successfully
- Join farm "non-existent-farm"
- Should see join error but stay connected

### 5. Timeout Scenario
**Test:** Simulate slow network response

**Expected Behavior:**
- After 10 seconds, timeout error
- Error: "Operation timed out: [operation]"
- Retry available

**Verification:**
- Can use network throttling tools
- Or modify timeout in code for testing

## Error Recovery Flow

```
User Action → Connection Attempt
    ↓
CONNECTION STATE: CONNECTING
Loading: true
    ↓
ERROR OCCURS
    ↓
CONNECTION STATE: ERROR
Error Message: Set
Loading: false
    ↓
User Sees Error Dialog
    ↓
User Clicks "Retry"
    ↓
CONNECTION STATE: RECONNECTING
    ↓
Exponential Backoff Delay
    ↓
Retry Attempt
    ↓
SUCCESS?
    ↓ Yes
CONNECTION STATE: CONNECTED
Clear Errors
    ↓ No (retry count < max)
Increase delay
Back to Retry Attempt
    ↓ No (retry count >= max)
Show "Max retries reached"
Stop retrying
```

## Key Implementation Details

### Retry Algorithm
```kotlin
private fun calculateRetryDelay(attempt: Int): Long {
    val delay = baseRetryDelay * (1 shl (attempt - 1)) // 2^(n-1)
    return minOf(delay, maxRetryDelay)
}

// Results: 1s, 2s, 4s, 8s, 16s, capped at 30s
```

### Error Categorization
```kotlin
val detailedMessage = when {
    errorMessage.contains("timeout") ->
        "Connection timed out. Please check your network."
    errorMessage.contains("ECONNREFUSED") ->
        "Backend server is offline or unreachable."
    errorMessage.contains("ERR_NAME_NOT_RESOLVED") ->
        "Invalid backend URL. Please check settings."
    errorMessage.contains("Network") ->
        "Network error. Please check your internet connection."
    else -> "Connection error: $errorMessage"
}
```

### Loading State Management
- Set `isLoading = true` at operation start
- Set `isLoading = false` in finally block
- UI elements disabled during loading
- Loading spinner shown in appropriate places

## Configuration

### Timeouts
- Connection timeout: 10 seconds
- Join farm timeout: 10 seconds
- Base retry delay: 1 second
- Max retry delay: 30 seconds
- Max retry attempts: 5

### Backend URL
Default: `http://10.0.2.2:4000` (Android emulator localhost)
Production: Update in Settings screen

## Logging

All WebSocket events are logged to the Activity Logs:
- Connection attempts: INFO
- Successful operations: SUCCESS
- Errors: ERROR
- Worker events: INFO
- Health alerts: WARNING/ERROR

**View Logs:** Main screen → Logs tab (bottom navigation)

## Best Practices

1. **Always check connection state** before operations
2. **Use loading indicators** for async operations
3. **Provide retry options** for failed operations
4. **Show clear error messages** to users
5. **Log all important events** for debugging
6. **Reset state** on successful operations
7. **Handle edge cases** (empty responses, null values)
8. **Use coroutines properly** (viewModelScope, error handling)

## Troubleshooting

### Error: "Connection failed: null"
- Check backend URL in Settings
- Verify backend is running (`pm2 status`)
- Check network connectivity

### Error: "Failed to join farm"
- Verify farm ID exists in backend
- Check worker name is valid (non-empty)
- Review backend logs for rejection reason

### Auto-reconnect not working
- Check retry count hasn't exceeded max (5)
- Verify error state allows retry
- Check logs for reconnection attempts

### Loading spinner stuck
- Check for uncaught exceptions in coroutines
- Verify finally blocks are executed
- Review logs for operation completion

## File Structure

```
app/src/main/java/com/example/farmdirectoryupgraded/
├── data/
│   ├── FarmWebSocketService.kt (Enhanced with error handling)
│   └── WebSocketModels.kt (Error types and states)
├── viewmodel/
│   └── FarmerViewModel.kt (Error collection and handling)
├── ui/
│   └── Screens.kt (Settings screen with status)
└── MainActivity.kt (Error dialogs and banners)
```

## Summary

The implementation provides:
- Comprehensive error handling for all WebSocket operations
- Automatic retry with exponential backoff
- Clear loading states throughout the UI
- User-friendly error messages and recovery options
- Detailed logging for debugging
- Robust connection state management
- Graceful handling of all error scenarios

All error cases are properly handled with user-facing messages, retry options, and proper state management.
