package daan.se.dcstool.model

import daan.se.dcstool.model.Hemisphere.*
import org.junit.Assert.*
import org.junit.Test

class LaLoMinuteTest {
    @Test
    fun test() {
        test(NORTH, 0,0.0, EAST, 0, 0.0, "N 00°00.00' E 000°00.00'")
        test(SOUTH, 10,50.0, WEST, 10, 30.156846846324, "S 10°50.00' W 010°30.16'")
        test(SOUTH, 10,0.0, WEST, 100,0.0, "S 10°00.00' W 100°00.00'")
    }

    fun test(latH: Hemisphere, latD: Int, latM: Double, lonH: Hemisphere, lonD: Int, lonM: Double, expected: String) {
        val actual: Any = LaLoMinute(latH, latD, latM, lonH, lonD, lonM).print()
        println(actual)
        assertEquals(expected, actual);
    }

    @Test
    fun testFromLaLoDegree() {
        val laLoD = LaLoDegree(SOUTH, 0.5, EAST, 11.9915)
        val actual = LaLoMinuteFactory.fromLaLoDegree(laLoD)
        println(actual.print())
        assertEquals(LaLoMinute(SOUTH, 0, 30.0,EAST, 11,59.49).print(), actual.print())
    }

    @Test
    fun testToLaLoDegree() {
        val laLoM = LaLoMinute(SOUTH, 10, 30.0,EAST, 0,59.49)
        val actual = laLoM.toLaLoDegree()
        println(actual.print())
        assertEquals(LaLoDegree(SOUTH, 10.5, EAST, 0.9915).print(), actual.print())
    }
}