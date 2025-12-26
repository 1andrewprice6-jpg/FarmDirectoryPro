# WebSocket Error Handling - Test Scenarios

## Test Environment Setup

### Prerequisites
- Android device/emulator running the FarmDirectoryUpgraded app
- Skeleton Key backend server accessible
- Network control tools (airplane mode, WiFi toggle)
- PM2 for backend management (optional)

### Backend URLs
- **Emulator**: `http://10.0.2.2:4000`
- **Physical Device**: `http://<your-local-ip>:4000`
- **Production**: Update as needed

## Test Scenarios

### Scenario 1: Backend Offline on Initial Connection
**Objective:** Test behavior when backend is not running

**Steps:**
1. Ensure backend is stopped (`pm2 stop all` or kill process)
2. Open app Settings
3. Configure correct backend URL
4. Click "Connect Now"

**Expected Results:**
- Loading spinner appears in TopAppBar
- Connection state: CONNECTING → ERROR
- Error dialog appears with message: "Backend server is offline or unreachable."
- Error banner shows in Settings with retry button
- Activity log shows ERROR entry
- Retry button is enabled

**Pass Criteria:**
- [x] Error dialog displayed
- [x] Clear error message shown
- [x] Retry button available
- [x] Loading state cleared
- [x] Log entry created

---

### Scenario 2: Network Disconnection After Successful Connection
**Objective:** Test auto-reconnection and recovery

**Steps:**
1. Start backend server
2. Connect app successfully (verify green "Connected" status)
3. Disable network (airplane mode or WiFi off)
4. Wait 5 seconds
5. Re-enable network

**Expected Results:**
- Connection state: CONNECTED → DISCONNECTED → RECONNECTING
- Yellow "Reconnecting..." banner appears
- Automatic retry attempts with delays (1s, 2s, 4s, 8s, 16s)
- After network restore, reconnection succeeds
- State changes to CONNECTED
- Success message appears

**Pass Criteria:**
- [x] Auto-reconnection attempts
- [x] Exponential backoff delays observed
- [x] Successful reconnection after network restore
- [x] UI updates correctly throughout
- [x] No manual intervention needed

---

### Scenario 3: Invalid Backend URL
**Objective:** Test validation of backend configuration

**Steps:**
1. Open Settings
2. Enter invalid URL: "invalid-url-test"
3. Click "Connect Now"

**Expected Results:**
- Connection attempt starts
- Error occurs quickly
- Error message: "Invalid backend URL. Please check settings."
- Connection state: ERROR
- Settings validation error appears

**Pass Criteria:**
- [x] Invalid URL detected
- [x] Clear error message
- [x] No retry attempts (config error)
- [x] User directed to fix settings

---

### Scenario 4: Timeout During Connection
**Objective:** Test timeout handling

**Steps:**
1. Configure backend URL to unreachable server (e.g., `http://192.168.1.250:4000`)
2. Click "Connect Now"
3. Wait 10+ seconds

**Expected Results:**
- Loading spinner shows for ~10 seconds
- After 10 seconds, timeout error
- Error message: "Connection timed out. Please check your network."
- Connection state: ERROR
- Retry button available

**Pass Criteria:**
- [x] Timeout after ~10 seconds
- [x] Loading state cleared
- [x] Error message displayed
- [x] Retry option available

---

### Scenario 5: Invalid Farm ID on Join
**Objective:** Test farm join error handling

**Steps:**
1. Connect successfully to backend
2. In Settings, set Farm ID to "non-existent-farm-12345"
3. Ensure app is connected
4. Trigger join farm (reconnect or restart app with auto-connect)

**Expected Results:**
- Connection succeeds
- Join farm fails
- Error message: "Failed to join farm non-existent-farm-12345: [reason]"
- Connection remains active (green dot)
- Error dialog shows with dismiss option

**Pass Criteria:**
- [x] Join farm error separate from connection error
- [x] Connection maintained
- [x] Clear error about farm join failure
- [x] User can dismiss and try different farm

---

### Scenario 6: Connection Recovery After Max Retries
**Objective:** Test behavior after retry limit reached

**Steps:**
1. Ensure backend is offline
2. Connect app
3. Let automatic retry attempts exhaust (5 attempts)
4. Start backend server
5. Click "Retry" button

**Expected Results:**
- After 5 failed attempts: "Failed to reconnect after 5 attempts. Please try manually."
- Automatic retries stop
- Manual retry button available
- After clicking retry, connection succeeds
- State changes to CONNECTED

**Pass Criteria:**
- [x] Max retry limit enforced
- [x] Clear message about manual retry needed
- [x] Manual retry works
- [x] Retry count resets after success

---

### Scenario 7: Network Quality Degradation
**Objective:** Test intermittent connectivity

**Steps:**
1. Connect successfully
2. Simulate poor network (use network throttling if available)
3. Perform location update
4. Observe behavior

**Expected Results:**
- Location update may fail with error
- Error message: "Cannot update location: Not connected to server"
- Connection may drop and auto-reconnect
- Data operation queues or fails gracefully

**Pass Criteria:**
- [x] Operation failures handled gracefully
- [x] Error messages shown
- [x] No app crashes
- [x] Recovery possible when network improves

---

### Scenario 8: Backend Restart During Active Session
**Objective:** Test behavior when backend restarts

**Steps:**
1. Connect successfully and join farm
2. Restart backend server (`pm2 restart skeleton-key`)
3. Wait for reconnection
4. Verify functionality restored

**Expected Results:**
- Connection drops (DISCONNECTED)
- Auto-reconnection starts
- After backend is ready, connection restores
- May need to rejoin farm
- Full functionality restored

**Pass Criteria:**
- [x] Connection drop detected
- [x] Auto-reconnection successful
- [x] Farm rejoin successful
- [x] Real-time updates resume

---

### Scenario 9: Multiple Error Types in Sequence
**Objective:** Test error message updates

**Steps:**
1. Try invalid URL (error 1)
2. Dismiss error
3. Fix URL but keep backend offline (error 2)
4. Dismiss error
5. Start backend and retry (success)

**Expected Results:**
- Each error shows appropriate message
- Previous errors are cleared
- Error messages don't stack up
- Final success clears all errors

**Pass Criteria:**
- [x] Error messages update correctly
- [x] Old errors cleared
- [x] UI state resets properly
- [x] Success message shows

---

### Scenario 10: Loading State Interruption
**Objective:** Test loading state management

**Steps:**
1. Start connection attempt
2. While loading spinner is showing, click disconnect
3. Observe state

**Expected Results:**
- Loading spinner stops
- Connection attempt cancelled
- State: DISCONNECTED
- No errors shown
- UI returns to ready state

**Pass Criteria:**
- [x] Loading state cleared
- [x] Operation cancelled gracefully
- [x] No stuck loading indicators
- [x] UI responsive

---

### Scenario 11: Worker Presence Updates
**Objective:** Test real-time worker tracking

**Steps:**
1. Connect and join farm successfully
2. Connect another instance (or simulate worker join)
3. Observe worker count
4. Disconnect other worker
5. Observe worker count update

**Expected Results:**
- Worker count shows in TopAppBar
- "Live (X workers)" updates in real-time
- Success message: "[Worker name] joined"
- Worker left events logged

**Pass Criteria:**
- [x] Worker count accurate
- [x] Real-time updates
- [x] Join/leave messages shown
- [x] Logs record events

---

### Scenario 12: Critical Health Alert
**Objective:** Test critical alert handling

**Steps:**
1. Connect and join farm
2. From another client/backend, trigger critical health alert
3. Observe app response

**Expected Results:**
- Critical alert notification appears
- Red snackbar: "CRITICAL: [message]"
- Alert logged with ERROR level
- Alert visible in health alerts flow

**Pass Criteria:**
- [x] Critical alert received
- [x] High-priority UI notification
- [x] Logged correctly
- [x] User aware of critical issue

---

## Performance Tests

### Load Test 1: Rapid Reconnections
**Steps:**
1. Toggle network on/off rapidly 5 times
2. Observe app behavior

**Expected Results:**
- No crashes
- State updates correctly
- No memory leaks
- Eventually reaches stable state

### Load Test 2: Multiple Simultaneous Operations
**Steps:**
1. While connecting, try to update location
2. While location updating, try to update health
3. Observe error handling

**Expected Results:**
- Operations queue or fail gracefully
- Appropriate error messages
- No crashes
- State remains consistent

## Edge Cases

### Edge Case 1: Empty/Null Responses
**Test:** Backend sends empty response to join farm
**Expected:** Error message about empty response

### Edge Case 2: Malformed Data
**Test:** Backend sends invalid JSON
**Expected:** Parse error caught, error message shown

### Edge Case 3: Unexpected Disconnection
**Test:** Network cable unplugged (physical device)
**Expected:** Disconnect detected, auto-reconnection starts

## Regression Tests

After any code changes, run these quick tests:

1. **Basic Connection**: Connect → verify success
2. **Basic Disconnection**: Disconnect → verify disconnected
3. **Error Display**: Trigger any error → verify dialog shows
4. **Retry**: Trigger error → retry → verify works
5. **Loading State**: Connect → verify spinner → verify clears

## Test Checklist

Use this checklist for complete testing:

- [ ] Backend offline on initial connect
- [ ] Network disconnection during session
- [ ] Invalid backend URL
- [ ] Connection timeout
- [ ] Invalid farm ID
- [ ] Max retry attempts reached
- [ ] Manual retry after max attempts
- [ ] Network quality degradation
- [ ] Backend restart during session
- [ ] Multiple sequential errors
- [ ] Loading state interruption
- [ ] Worker presence updates
- [ ] Critical health alerts
- [ ] Rapid reconnections (load test)
- [ ] Multiple simultaneous operations
- [ ] Empty/null responses
- [ ] Malformed data
- [ ] Unexpected disconnection
- [ ] All regression tests pass

## Automated Testing Considerations

For future automated testing:

```kotlin
@Test
fun testConnectionFailure() {
    // Mock backend offline
    every { socket.connect() } throws Exception("ECONNREFUSED")

    // Trigger connection
    viewModel.connectToBackend()

    // Verify error state
    assertEquals(ConnectionState.ERROR, viewModel.connectionState.value)
    assertNotNull(viewModel.errorMessage.value)
    assertTrue(viewModel.errorMessage.value!!.contains("offline"))
}

@Test
fun testRetryWithExponentialBackoff() {
    // Verify retry delays
    val delays = (1..5).map { calculateRetryDelay(it) }
    assertEquals(listOf(1000L, 2000L, 4000L, 8000L, 16000L), delays)
}

@Test
fun testMaxRetriesEnforcement() {
    repeat(6) { viewModel.retryConnection() }

    verify(exactly = 5) { webSocketService.connect() }
    assertTrue(viewModel.errorMessage.value!!.contains("after 5 attempts"))
}
```

## Success Metrics

- **Error Detection Rate**: 100% of errors caught and displayed
- **Recovery Rate**: >95% of recoverable errors auto-recover
- **User Intervention**: <5% of errors require user action beyond retry
- **Loading State Accuracy**: 100% of operations show correct loading state
- **Log Coverage**: 100% of errors logged

## Known Issues / Limitations

1. Network throttling may cause unpredictable behavior
2. Emulator network simulation not always accurate
3. Some error messages may need refinement based on user feedback
4. Retry count persists during app session (reset on app restart)

## Reporting Issues

When reporting issues, include:
- Device/emulator details
- Backend URL and status
- Network conditions
- Steps to reproduce
- Activity logs (export from app)
- Screenshots of error dialogs
- Connection state at time of error
