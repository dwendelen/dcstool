package daan.se.dcstool.model

import org.junit.Assert.*
import org.junit.Test

class MGRSTest {
    @Test
    fun fromUTM() {
        val utm = UTM(37, Hemisphere.NORTH, LatitudeBand.T, 717672, 4609327)
        val actual = MGRS.fromUTM(utm)

        assertEquals("37T GG 17672 09327", actual.print())
    }

    @Test
    fun fromUTM2() {
        val utm = UTM(17, Hemisphere.NORTH, LatitudeBand.T, 630100, 4833431)
        val actual = MGRS.fromUTM(utm)

        assertEquals("17T PJ 30100 33431", actual.print())
    }
}