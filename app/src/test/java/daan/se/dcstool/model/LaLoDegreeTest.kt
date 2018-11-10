package daan.se.dcstool.model

import daan.se.dcstool.model.Hemisphere.*
import org.junit.Assert.*
import org.junit.Test

class LaLoDegreeTest {
    @Test
    fun test() {
        test(NORTH, 0.0, EAST, 0.0, "N 00.0000° E 000.0000°")
        test(SOUTH, 10.0, WEST, 10.0, "S 10.0000° W 010.0000°")
        test(SOUTH, 10.0, WEST, 100.0, "S 10.0000° W 100.0000°")
    }

    fun test(latH: Hemisphere, latD: Double, lonH: Hemisphere, lonD: Double, expected: String) {
        val actual: Any = LaLoDegree(latH, latD, lonH, lonD).print()
        println(actual)
        assertEquals(expected, actual);
    }
}