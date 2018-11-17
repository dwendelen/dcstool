package daan.se.dcstool.model

import org.junit.Assert.*
import org.junit.Test

class MGRSTest {
    @Test
    fun fromUTM() {
        val utm = UTM(Hemisphere.NORTH, 37, LatitudeBand.T, 717672.0, 4609327.0)
        val actual = MGRSFactory.fromUTM(utm)

        assertEquals("37T GG 17672 09327", actual.print())
    }

    @Test
    fun fromUTM2() {
        val utm = UTM(Hemisphere.NORTH, 17, LatitudeBand.T, 630100.0, 4833431.0)
        val actual = MGRSFactory.fromUTM(utm)

        assertEquals("17T PJ 30100 33431", actual.print())
    }

    @Test
    fun toUTM1() {
        val mgrs = MGRS(37, LatitudeBand.T, ColumnLetter.G, RowLetter.G, 17672.0, 9327.0)
        val actual = mgrs.toUTM()

        assertEquals("N 37T 717672 4609327", actual.print())
    }

    @Test
    fun toUTM2() {
        val mgrs = MGRS(17, LatitudeBand.T, ColumnLetter.P, RowLetter.J, 30100.0, 33431.0)
        val actual = mgrs.toUTM()

        assertEquals("N 17T 630100 4833431", actual.print())
    }
}