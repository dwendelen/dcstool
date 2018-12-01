package se.daan.dcstool.model

import org.junit.Assert.assertEquals
import org.junit.Test
import se.daan.dcstool.model.Hemisphere.*

class LaLoSecondTest {
    @Test
    fun test() {
        test(NORTH, 0,0,0.0, EAST, 0,0,0.0, "N 00° 00' 00\" E 000° 00' 00\"")
        test(SOUTH, 10,50,15.0, WEST, 10, 30,16.0, "S 10° 50' 15\" W 010° 30' 16\"")
        test(SOUTH, 10,0,0.0, WEST, 100,0,0.0, "S 10° 00' 00\" W 100° 00' 00\"")
    }

    fun test(latH: Hemisphere, latD: Int, latM: Int, latS: Double, lonH: Hemisphere, lonD: Int, lonM: Int, lonS:Double, expected: String) {
        val actual: Any = LaLoSecond(latH, latD, latM, latS, lonH, lonD, lonM, lonS).print()
        println(actual)
        assertEquals(expected, actual);
    }
}