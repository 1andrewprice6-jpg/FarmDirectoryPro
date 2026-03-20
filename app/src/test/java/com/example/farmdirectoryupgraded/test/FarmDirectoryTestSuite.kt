package com.example.farmdirectoryupgraded.test

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.farmdirectoryupgraded.data.*
import com.example.farmdirectoryupgraded.utils.ValidationUtils
import com.example.farmdirectoryupgraded.viewmodel.FarmerListViewModel
import com.example.farmdirectoryupgraded.viewmodel.AttendanceViewModel
import com.example.farmdirectoryupgraded.viewmodel.LocationViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit Tests for FarmDirectoryPro Application
 *
 * Test categories:
 * 1. ViewModel Tests
 * 2. Utility & Validation Tests
 * 3. Security Tests
 *
 * Note: Database tests (requiring Android context) are in androidTest.
 */

// =====================
// 1. VIEWMODEL TESTS
// =====================

@OptIn(ExperimentalCoroutinesApi::class)
class FarmerListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context = mockk<Context>(relaxed = true)
    private val farmerDao = mockk<FarmerDao>()
    private lateinit var viewModel: FarmerListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
        viewModel = FarmerListViewModel(context, farmerDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddFarmer() = runTest {
        val farmer = Farmer(
            name = "Test Farmer",
            address = "Test Address",
            phone = "(828) 123-4567",
            type = "Pullet"
        )
        coEvery { farmerDao.insertFarmer(farmer) } returns Unit

        viewModel.addFarmer(farmer)
        // Verify success message is set
        assert(viewModel.successMessage.value?.contains("added") ?: false)
    }

    @Test
    fun testUpdateSearchQuery() = runTest {
        viewModel.updateSearchQuery("test query")
        assertEquals("test query", viewModel.searchQuery.value)
    }

    @Test
    fun testFilterByType() = runTest {
        viewModel.filterByType("Pullet")
        assertEquals("Pullet", viewModel.selectedType.value)
    }

    @Test
    fun testToggleFavorite() = runTest {
        val farmer = Farmer(
            id = 1,
            name = "Test Farmer",
            address = "Test Address",
            phone = "(828) 123-4567",
            type = "Pullet",
            isFavorite = false
        )
        coEvery { farmerDao.updateFavoriteStatus(1, true) } returns Unit

        viewModel.toggleFavorite(farmer)
        // Verify no error message
        assert(viewModel.errorMessage.value == null)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val attendanceDao = mockk<AttendanceDao>()
    private val employeeDao = mockk<EmployeeDao>()
    private lateinit var viewModel: AttendanceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
        coEvery { employeeDao.getAllEmployees() } returns flowOf(emptyList())
        viewModel = AttendanceViewModel(attendanceDao, employeeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCheckInWithGPS() = runTest {
        val employee = Employee(id = 1, name = "Test Employee", role = "CATCHER", phone = "", email = "")
        coEvery { employeeDao.getEmployeeById(1) } returns employee
        coEvery { attendanceDao.insertAttendanceRecord(any()) } returns 1L

        viewModel.checkInWithGPS(
            employeeId = 1,
            latitude = 35.7796,
            longitude = -81.3361,
            workLocation = "Farm A",
            taskDescription = "Checking chickens"
        )

        // Verify success message is set
        assert(viewModel.successMessage.value?.contains("successfully") ?: false)
    }

    @Test
    fun testCheckOut() = runTest {
        val record = AttendanceRecord(
            id = 1,
            employeeId = 1,
            employeeName = "Test Employee",
            method = "GPS",
            checkInTime = System.currentTimeMillis() - 3600000  // 1 hour ago
        )
        coEvery { attendanceDao.getAttendanceRecordById(1) } returns record
        coEvery { attendanceDao.updateAttendanceRecord(any()) } returns Unit

        viewModel.checkOut(1, 35.7796, -81.3361)

        // Verify success message contains hours worked
        assert(viewModel.successMessage.value?.contains("hours") ?: false)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val farmerDao = mockk<FarmerDao>()
    private val employeeDao = mockk<EmployeeDao>()
    private lateinit var viewModel: LocationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
        viewModel = LocationViewModel(farmerDao, employeeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCalculateHaversineDistance() {
        // Test distance between two known points
        val distance = viewModel.calculateHaversineDistance(
            35.7796, -81.3361,  // Hiddenite, NC
            35.7850, -81.3400   // ~1km away
        )
        assertTrue(distance in 0.9..1.2)  // Should be approximately 1km
    }

    @Test
    fun testFindCentroid() {
        val farmers = listOf(
            Farmer(name = "A", address = "", phone = "", latitude = 0.0, longitude = 0.0),
            Farmer(name = "B", address = "", phone = "", latitude = 2.0, longitude = 2.0),
            Farmer(name = "C", address = "", phone = "", latitude = 4.0, longitude = 4.0)
        )
        val centroid = viewModel.findCentroid(farmers)
        assertEquals(2.0, centroid.first)
        assertEquals(2.0, centroid.second)
    }

    @Test
    fun testUpdateFarmerLocation() = runTest {
        coEvery { farmerDao.updateFarmerLocation(any(), any(), any(), any()) } returns Unit

        viewModel.updateFarmerLocation(1, 35.7796, -81.3361)
        // Verify no error
        assert(viewModel.errorMessage.value == null)
    }
}

// =====================
// 2. VALIDATION TESTS
// =====================

class ValidationUtilsTest {

    @Test
    fun testValidEmail() {
        val result = ValidationUtils.validateEmail("test@example.com")
        assertTrue(result.isValid)
    }

    @Test
    fun testInvalidEmail() {
        val result = ValidationUtils.validateEmail("invalid-email")
        assertFalse(result.isValid)
    }

    @Test
    fun testEmptyEmailIsValid() {
        val result = ValidationUtils.validateEmail("")
        assertTrue(result.isValid)  // Optional field
    }

    @Test
    fun testValidPhone() {
        val result = ValidationUtils.validatePhone("(828) 123-4567")
        assertTrue(result.isValid)
    }

    @Test
    fun testInvalidPhone() {
        val result = ValidationUtils.validatePhone("123")
        assertFalse(result.isValid)
    }

    @Test
    fun testValidLatitude() {
        val result = ValidationUtils.validateLatitude("35.7796")
        assertTrue(result.isValid)
    }

    @Test
    fun testInvalidLatitude() {
        val result = ValidationUtils.validateLatitude("95.0")
        assertFalse(result.isValid)
    }

    @Test
    fun testValidLongitude() {
        val result = ValidationUtils.validateLongitude("-81.3361")
        assertTrue(result.isValid)
    }

    @Test
    fun testInvalidLongitude() {
        val result = ValidationUtils.validateLongitude("195.0")
        assertFalse(result.isValid)
    }

    @Test
    fun testValidFarmId() {
        val result = ValidationUtils.validateFarmId("farm-001")
        assertTrue(result.isValid)
    }

    @Test
    fun testInvalidFarmId() {
        val result = ValidationUtils.validateFarmId("f@m")
        assertFalse(result.isValid)
    }

    @Test
    fun testValidWorkerName() {
        val result = ValidationUtils.validateWorkerName("John Doe")
        assertTrue(result.isValid)
    }

    @Test
    fun testInvalidWorkerName() {
        val result = ValidationUtils.validateWorkerName("J")
        assertFalse(result.isValid)
    }
}

// =====================
// 3. SECURITY TESTS
// =====================

class SecurityTest {

    @Test
    fun testIsValidHostname() {
        assertTrue(com.example.farmdirectoryupgraded.security.CertificatePinning.isValidHostname("api.farmdirectory.com"))
    }

    @Test
    fun testIsInvalidHostname() {
        assertFalse(com.example.farmdirectoryupgraded.security.CertificatePinning.isValidHostname("invalid@hostname"))
    }

    @Test
    fun testExtractHostname() {
        val hostname = com.example.farmdirectoryupgraded.security.CertificatePinning.extractHostname("https://api.farmdirectory.com/path")
        assertEquals("api.farmdirectory.com", hostname)
    }

    @Test
    fun testValidationResults() {
        val success = ValidationUtils.ValidationResult.success()
        assertTrue(success.isValid)

        val error = ValidationUtils.ValidationResult.error("Test error")
        assertFalse(error.isValid)
        assertEquals("Test error", error.errorMessage)
    }
}
