package com.example.farmdirectoryupgraded

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.farmdirectoryupgraded.data.FarmDatabase
import com.example.farmdirectoryupgraded.data.Farmer
import com.example.farmdirectoryupgraded.data.FarmerDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Instrumented database tests that execute on an Android device.
 */
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
        assertNull(retrieved)
    }

    @Test
    fun testToggleFavoriteStatus() = runTest {
        val farmer = Farmer(
            name = "John Doe",
            address = "123 Farm Lane",
            phone = "(828) 123-4567",
            type = "Pullet",
            isFavorite = false
        )
        farmerDao.insertFarmer(farmer)
        farmerDao.updateFavoriteStatus(1, true)
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

        val results = farmerDao.searchFarmers("John").first()
        assertEquals(1, results.size)
        assertEquals("John Doe", results[0].name)
    }
}
