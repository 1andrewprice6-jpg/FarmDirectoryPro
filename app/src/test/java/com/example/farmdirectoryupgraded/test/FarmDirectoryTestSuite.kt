package com.example.farmdirectoryupgraded.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.farmdirectoryupgraded.data.*
import com.example.farmdirectoryupgraded.utils.ValidationUtils
import com.example.farmdirectoryupgraded.viewmodel.FarmerListViewModel
import com.example.farmdirectoryupgraded.viewmodel.AttendanceViewModel
import com.example.farmdirectoryupgraded.viewmodel.LocationViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit Tests for FarmDirectoryPro Application
 *
 * Test categories:
 * 1. Database & DAO Tests
 * 2. ViewModel Tests
 * 3. Utility & Validation Tests
 * 4. Security Tests
 */

// =====================
// 1. DATABASE TESTS
// =====================

@RunWith(AndroidJUnit4::class)
class FarmDatabaseTest {

    private lateinit var database: FarmDatabase
    private lateinit var farmerDao: FarmerDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FarmDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        farmerDao = database.farmerDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveFarmer() = runTest {
        val farmer = Farmer(
            name = "John Doe",
            address = "123 Farm Lane",
            phone = "(828) 123-4567",
            type = "Pullet",
            latitude = 35.7796,
            longitude = -81.3361
        )
        farmerDao.insertFarmer(farmer)
        val retrieved = farmerDao.getFarmerById(1)
        assertEquals("John Doe", retrieved?.name)
    }

    @Test
    fun testUpdateFarmer() = runTest {
        val farmer = Farmer(
            name = "John Doe",
            address = "123 Farm Lane",
            phone = "(828) 123-4567",
            type = "Pullet"
        )
        farmerDao.insertFarmer(farmer)
        val updated = farmer.copy(id = 1, name = "Jane Doe")
        farmerDao.updateFarmer(updated)
        val retrieved = farmerDao.getFarmerById(1)
        assertEquals("Jane Doe", retrieved?.name)
    }

    @Test
    fun testDeleteFarmer() = runTest {
        val farmer = Farmer(
            name = "John Doe",
            address = "123 Farm Lane",
            phone = "(828) 123-4567",
            type = "Pullet"
        )
        farmerDao.insertFarmer(farmer)
        farmerDao.deleteFarmer(farmer.copy(id = 1))
        val retrieved = farmerDao.getFarmerById(1)
        assertEquals(null, retrieved)
    }

    @Test
    fun testToggleFavoriteSatus() = runTest {
        val farmer = Farmer(
            name = "John Doe",
            address = "123 Farm Lane",
            phone = "(828) 123-4567",
            type = "Pullet",
            isFavorite = false
        )
        farmerDao.insertFarmer(farmer)
        farmerDao.updateFavoriteSatus(1, true)
        val retrieved = farmerDao.getFarmerById(1)
        assertTrue(retrieved?.isFavorite ?: false)
    }

    @Test
    fun testSearchFarmers() = runTest {
        val farmer1 = Farmer(
            name = "John Doe",
            address = "123 Farm Lane",
            phone = "(828) 123-4567",
            type = "Pullet"
        )
        val farmer2 = Farmer(
            name = "Jane Smith",
            address = "456 Poultry Road",
            phone = "(828) 987-6543",
            type = "Breeder"
        )
        farmerDao.insertFarmer(farmer1)
        farmerDao.insertFarmer(farmer2)

        val results = farmerDao.searchFarmers("John")
        assertEquals(1, results.size)
        assertEquals("John Doe", results[0].name)
    }
}

// =====================
// 2. VIEWMODEL TESTS
// =====================

@RunWith(AndroidJUnit4::class)
class FarmerListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val farmerDao = mockk<FarmerDao>()
    private lateinit var viewModel: FarmerListViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = FarmerListViewModel(farmerDao)
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
        coEvery { farmerDao.updateFavoriteSatus(1, true) } returns Unit

        viewModel.toggleFavorite(1, false)
        // Verify no error message
        assert(viewModel.errorMessage.value == null)
    }
}

@RunWith(AndroidJUnit4::class)
class AttendanceViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val attendanceDao = mockk<AttendanceDao>()
    private val employeeDao = mockk<EmployeeDao>()
    private lateinit var viewModel: AttendanceViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { employeeDao.getAllEmployees() } returns flowOf(emptyList())
        viewModel = AttendanceViewModel(attendanceDao, employeeDao)
    }

    @Test
    fun testCheckInWithGPS() = runTest {
        coEvery { attendanceDao.insertAttendanceRecord(any()) } returns Unit

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

@RunWith(AndroidJUnit4::class)
class LocationViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val farmerDao = mockk<FarmerDao>()
    private val employeeDao = mockk<EmployeeDao>()
    private lateinit var viewModel: LocationViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = LocationViewModel(farmerDao, employeeDao)
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
// 3. VALIDATION TESTS
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
// 4. SECURITY TESTS
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
