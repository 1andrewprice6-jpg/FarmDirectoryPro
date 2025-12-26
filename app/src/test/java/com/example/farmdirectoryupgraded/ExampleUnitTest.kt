package com.example.farmdirectoryupgraded

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Before
    fun setup() {
        // Setup code before each test
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun string_concatenation_works() {
        val result = "Hello" + " " + "World"
        assertEquals("Hello World", result)
    }

    @Test
    fun list_operations_work() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.size)
        assertTrue(list.contains(3))
        assertFalse(list.contains(6))
    }
}
