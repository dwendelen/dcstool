package se.daan.dcstool.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UTMTest {
    @Test
    fun testFromLaLo() {
        val sec = laLoSecond(
                Hemisphere.NORTH, 43,38,33.0,
                Hemisphere.WEST, 79,23,13.0
        )
        val laLoDegree = sec.toLaLoDegree()
        val actual = UTMFactory.fromLaLoDegree(laLoDegree)

        assertEquals("N 17T 630100 4833431", actual.print())
    }

    @Test
    fun testFromLaLo2() {
        val sec = LaLo(MinutePart(Hemisphere.NORTH, 41, 36.359, PartType.Latitude), MinutePart(Hemisphere.EAST, 41, 36.730, PartType.Longitude))
        val laLoDegree = sec.toLaLoDegree()
        val actual = UTMFactory.fromLaLoDegree(laLoDegree)

        assertEquals("N 37T 717672 4609326", actual.print())
    }

    @Test
    fun testToLaLo() {
        val utm = UTM(Hemisphere.NORTH, 17, LatitudeBand.T, 630100.0, 4833431.0)
        val actual = utm.toLaLoDegree()

        assertEquals("N 43.6425° W 079.3869°", actual.print())
    }

    @Test
    fun testToLaLo2() {
        val utm = UTM(Hemisphere.NORTH, 37, LatitudeBand.T, 717672.0, 4609327.0)
        val actual = utm.toLaLoDegree()

        assertEquals("N 41.6060° E 041.6122°", actual.print())
    }

    @Test
    fun testLatitudeBand() {
        assertEquals(LatitudeBand.T, LatitudeBand.getBandForLatitude(43.0))
    }



}