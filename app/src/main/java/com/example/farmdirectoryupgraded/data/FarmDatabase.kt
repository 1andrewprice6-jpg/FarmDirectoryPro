package com.example.farmdirectoryupgraded.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Farmer::class, AttendanceRecord::class, LogEntry::class],
    version = 5, // Updated: added company field to Farmer entity
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmerDao(): FarmerDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: FarmDatabase? = null

        fun getDatabase(context: Context): FarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FarmDatabase::class.java,
                    "farm_directory_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database.farmerDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(farmerDao: FarmerDao) {
            // Pre-populate with farmer data with sample GPS coordinates (Hiddenite, NC area)
            val farmers = listOf(
                Farmer(name = "Adams, Eugene", address = "153 White Pine Lane, Hiddenite, NC 28636", phone = "(828) 632-1702", cellPhone = "(828) 381-3617", email = "jerryadams240@yahoo.com", type = "Pullet", latitude = 35.7796, longitude = -81.3361),
                Farmer(name = "Adams, ReNaye", spouse = "Jerry", address = "5745 Cheatham Ford Rd, Hiddenite, NC 28636", phone = "(828) 381-6541, (828) 381-3900", email = "renaye.adams@att.net", type = "Pullet", latitude = 35.7845, longitude = -81.3420),
                Farmer(name = "Ball, Brandon", spouse = "Sierra", address = "3741 Old Vashti Rd., Hiddenite, NC 28636", phone = "(828) 632-7818", cellPhone = "(828) 315-0032", email = "tballfamily4@yahoo.com", type = "Breeder", latitude = 35.7920, longitude = -81.3515),
                Farmer(name = "Bowles, Jonathan & Emily", farmName = "E&J Bowles Farm LLC", address = "1179 Old Vashti Road, Taylorsville, NC 28681", phone = "(828) 446-0087, (828) 474-8587", email = "eydesign.eb@gmail.com", type = "Breeder", latitude = 35.8234, longitude = -81.2456),
                Farmer(name = "Brown, Guy", spouse = "Shana", address = "2004 Joe Johnson Road, Catawba, NC 28609", phone = "", cellPhone = "(828) 228-8933", email = "shanabrownpoultry@gmail.com", type = "Breeder", latitude = 35.7101, longitude = -81.0543),
                Farmer(name = "Cartner, Tony", farmName = "Cartner Cattle Farms", address = "503 Stroud Mill Road, Harmony, NC 28634", phone = "(336) 486-1759", email = "tony.cartner58@gmail.com", type = "Pullet", latitude = 35.9567, longitude = -80.7689),
                Farmer(name = "Cook, Kim", farmName = "Smokey Quartz Farm, LLC", address = "3211 Sharpe Mill Rd., Hiddenite, NC 28636", phone = "(828) 352-6991, (828) 352-4202", email = "kimcook4790@yahoo.com", latitude = 35.7654, longitude = -81.3298),
                Farmer(name = "Daniels, Blake & Joy", farmName = "Double Deuce", address = "261 Jim Barnes Lane, Taylorsville, NC 28681", phone = "(828) 320-7343, (828) 234-3777", email = "poochp@bellsouth.net", latitude = 35.8345, longitude = -81.2334),
                Farmer(name = "Dennis, Joe & Amy", farmName = "Dennis Family Farm", address = "548 Jim Millsaps Rd, Hiddenite, NC 28636", phone = "", cellPhone = "(828) 514-1895, (828) 635-6420", email = "amydennis@bellsouth.net", type = "Breeder", latitude = 35.7734, longitude = -81.3412),
                Farmer(name = "Dobson, Jordan & Elizabeth", farmName = "Dobson Creek", address = "195 Redeemed Lane, Harmony, NC 28634", phone = "(704) 902-3394, (704) 881-2694", email = "dth.jordan@yahoo.com", type = "Breeder", latitude = 35.9678, longitude = -80.7456),
                Farmer(name = "Elder, Curtis", spouse = "Ruth", address = "319 Center Church Road, Hiddenite, NC 28636", phone = "(828) 632-3126", cellPhone = "(828) 320-4303", email = "curtis3126@gmail.com", latitude = 35.7823, longitude = -81.3489),
                Farmer(name = "Fox, Alex", farmName = "AW Fox Enterprises Inc", address = "131 Johnson Farm Rd, Hiddenite, NC 28636", phone = "(704) 881-3429", email = "afox@synagro.com", type = "Pullet", latitude = 35.7889, longitude = -81.3567),
                Farmer(name = "Harris, Justin", farmName = "Harris Creek", address = "334 Allen Rd, Harmony, NC 28634", phone = "", cellPhone = "(704) 902-8626", email = "harriscreekfarm15@yahoo.com", latitude = 35.9534, longitude = -80.7723),
                Farmer(name = "Hatton, Jeremy", farmName = "Hatton Farms", address = "38 Lee Matheson Road, Taylorsville, NC 28681", phone = "(828) 310-4290", email = "baltimorekid76@gmail.com", latitude = 35.8456, longitude = -81.2189),
                Farmer(name = "Millsaps, Gary N", farmName = "G & G Poultry", address = "6572 Sulphur Springs Rd, Hiddenite, NC 28636", phone = "(828) 352-3448", cellPhone = "(828) 352-5031", email = "millsapssteelfab@gmail.com", latitude = 35.7567, longitude = -81.3201),
                Farmer(name = "Moore, Todd", farmName = "Moore Farms, Inc.", address = "215 Yadkin River Dr., Salisbury, NC 28147", phone = "(704) 433-5287", email = "mooretodd10@gmail.com", latitude = 35.6701, longitude = -80.4743),
                Farmer(name = "Myers, Craig", farmName = "D&M Farm", address = "1039 Calahaln Rd, Mocksville, NC 27208", phone = "(336) 345-9813", email = "myerscraig84@gmail.com", type = "Breeder", latitude = 35.8934, longitude = -80.5612),
                Farmer(name = "Robertson, Josh & Jason", farmName = "Robertson Farms", address = "765 County Line Road, Stony Point, NC 28678", phone = "(828) 234-9721, (828) 234-7901", email = "jasonrobertson1984@gmail.com", latitude = 35.8667, longitude = -81.0445),
                Farmer(name = "Rogers, Bryan", spouse = "Shanda", address = "800 Rogers Farm Lane, Hiddenite, NC 28636", phone = "(828) 632-5818", cellPhone = "(828) 312-5309", email = "bryan1trucking@yahoo.com", latitude = 35.7712, longitude = -81.3378),
                Farmer(name = "Sprinkle, Alan & Pam", farmName = "Brookhaven Farms", address = "712 Brookhaven Rd, Statesville, NC 28677", phone = "(704) 592-4864", cellPhone = "(704) 880-1300", email = "haynes_pamela@yahoo.com", type = "Breeder", latitude = 35.7823, longitude = -80.8712)
            )
            farmerDao.insertFarmers(farmers)
        }
    }
}
