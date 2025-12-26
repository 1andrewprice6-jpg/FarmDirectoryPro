package com.example.farmdirectoryupgraded

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.farmdirectoryupgraded", appContext.packageName)
    }

    @Test
    fun verifyAppName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val appName = appContext.applicationInfo.loadLabel(appContext.packageManager).toString()
        assertNotNull(appName)
    }
}
