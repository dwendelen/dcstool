package se.daan.dcstool.model

import org.junit.Assert.assertEquals
import org.junit.Test
import se.daan.dcstool.model.Hemisphere.*

class LaLoMinuteTest {
    @Test
    fun test() {
        test(NORTH, 0,0.0, EAST, 0, 0.0, "N 00° 00.00' E 000° 00.00'")
        test(SOUTH, 10,50.0, WEST, 10, 30.156846846324, "S 10° 50.00' W 010° 30.16'")
        test(SOUTH, 10,0.0, WEST, 100,0.0, "S 10° 00.00' W 100° 00.00'")
    }

    fun test(latH: Hemisphere, latD: Int, latM: Double, lonH: Hemisphere, lonD: Int, lonM: Double, expected: String) {
        val actual: Any = LaLo(MinutePart(latH, latD, latM, PartType.Latitude), MinutePart(lonH, lonD, lonM, PartType.Longitude)).print()
        println(actual)
        assertEquals(expected, actual);
    }

    @Test
    fun testFromLaLoDegree() {
        val laLoD = LaLo(DegreePart(SOUTH, 0.5, PartType.Latitude), DegreePart(EAST, 11.9915, PartType.Longitude))
        val actual = LaLoMinuteFactory.fromLaLoDegree(laLoD)
        println(actual.print())
        assertEquals(LaLo(MinutePart(SOUTH, 0, 30.0, PartType.Latitude), MinutePart(EAST, 11, 59.49, PartType.Longitude)).print(), actual.print())
    }

    @Test
    fun testToLaLoDegree() {
        val laLoM = LaLo(MinutePart(SOUTH, 10, 30.0, PartType.Latitude), MinutePart(EAST, 0, 59.49, PartType.Longitude))
        val actual = laLoM.toLaLoDegree()
        println(actual.print())
        assertEquals(LaLo(DegreePart(SOUTH, 10.5, PartType.Latitude), DegreePart(EAST, 0.9915, PartType.Longitude)).print(), actual.print())
    }
}