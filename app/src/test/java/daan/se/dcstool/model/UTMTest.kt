package daan.se.dcstool.model

import org.junit.Assert.*
import org.junit.Test

class UTMTest {
    @Test
    fun testFromLaLo() {
        val sec = LaLoSecond(
                Hemisphere.NORTH, 43,38,33,
                Hemisphere.WEST, 79,23,13
        )
        val laLoDegree = sec.toLaLoDegree()
        val actual = UTM.fromLaLoDegree(laLoDegree)

        assertEquals("17N 630100 4833431", actual.print())
    }

    @Test
    fun testFromLaLo2() {
        val sec = LaLoMinute(
                Hemisphere.NORTH, 41,36.359,
                Hemisphere.EAST, 41 ,36.730
        )
        val laLoDegree = sec.toLaLoDegree()
        val actual = UTM.fromLaLoDegree(laLoDegree)

        assertEquals("37N 717672 4609326", actual.print())
    }

    @Test
    fun testToLaLo() {
        val utm = UTM(17, Hemisphere.NORTH, LatitudeBand.T, 630100, 4833431)
        val actual = utm.toLaLoDegree()

        assertEquals("N 43.6425° W 079.3869°", actual.print())
    }

    @Test
    fun testToLaLo2() {
        val utm = UTM(37, Hemisphere.NORTH, LatitudeBand.T, 717672, 4609327)
        val actual = utm.toLaLoDegree()

        assertEquals("N 41.6060° E 041.6122°", actual.print())
    }

    @Test
    fun testLatitudeBand() {
        assertEquals(LatitudeBand.T, LatitudeBand.getBandForLatitude(43.0))
    }



}