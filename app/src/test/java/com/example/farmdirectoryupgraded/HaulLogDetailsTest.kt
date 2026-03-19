package com.example.farmdirectoryupgraded

import com.example.farmdirectoryupgraded.data.HaulLogDetails
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for [HaulLogDetails] encode/parse round-trips and edge cases.
 */
class HaulLogDetailsTest {

    @Test
    fun encode_producesCorrectFormat() {
        val details = HaulLogDetails(
            truckId = "T1",
            truckName = "Ford F-750",
            trailerId = "TR3",
            trailerName = "48ft Livestock",
            destination = "Mountaire",
            farmName = "Adams Farm"
        )
        val encoded = details.encode()
        assertEquals(
            "Truck:T1|Ford F-750;Trailer:TR3|48ft Livestock;Destination:Mountaire;Farm:Adams Farm",
            encoded
        )
    }

    @Test
    fun parse_returnsNullForNonHaulDetails() {
        assertNull(HaulLogDetails.parse("Some generic log detail"))
        assertNull(HaulLogDetails.parse(""))
        assertNull(HaulLogDetails.parse("Category:Fuel;Amount:50"))
    }

    @Test
    fun parse_roundTrip_fullDetails() {
        val original = HaulLogDetails(
            truckId = "T1",
            truckName = "Ford F-750",
            trailerId = "TR3",
            trailerName = "48ft Livestock",
            destination = "Mountaire",
            farmName = "Adams Farm"
        )
        val parsed = HaulLogDetails.parse(original.encode())
        assertNotNull(parsed)
        assertEquals(original.truckId, parsed!!.truckId)
        assertEquals(original.truckName, parsed.truckName)
        assertEquals(original.trailerId, parsed.trailerId)
        assertEquals(original.trailerName, parsed.trailerName)
        assertEquals(original.destination, parsed.destination)
        assertEquals(original.farmName, parsed.farmName)
    }

    @Test
    fun parse_roundTrip_purdueDestination() {
        val original = HaulLogDetails(
            truckId = "T2",
            truckName = "Chevy 5500",
            trailerId = "TR1",
            trailerName = "36ft Poultry",
            destination = "Purdue",
            farmName = "Rogers Farm"
        )
        val parsed = HaulLogDetails.parse(original.encode())
        assertNotNull(parsed)
        assertEquals("Purdue", parsed!!.destination)
    }

    @Test
    fun haulMarker_isContainedInEncodedString() {
        val encoded = HaulLogDetails(
            truckId = "T1", truckName = "Truck", trailerId = "", trailerName = "",
            destination = "Mountaire", farmName = "Farm"
        ).encode()
        assertTrue(encoded.contains(HaulLogDetails.HAUL_MARKER))
    }

    @Test
    fun parse_withEmptyOptionalFields_returnsDetails() {
        val original = HaulLogDetails(
            truckId = "T5",
            truckName = "",
            trailerId = "",
            trailerName = "",
            destination = "Mountaire",
            farmName = ""
        )
        val parsed = HaulLogDetails.parse(original.encode())
        assertNotNull(parsed)
        assertEquals("T5", parsed!!.truckId)
        assertEquals("", parsed.truckName)
        assertEquals("Mountaire", parsed.destination)
    }
}
