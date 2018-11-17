package daan.se.dcstool.model

import daan.se.dcstool.model.Hemisphere.EAST
import daan.se.dcstool.model.Hemisphere.NORTH
import daan.se.dcstool.model.parser.ParseResult
import daan.se.dcstool.model.parser.Parser
import daan.se.dcstool.model.parser.cross
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

@RunWith(Theories::class)
class IntegrationTest {
    companion object {
        val factories = listOf(
                LaLoDegreeFactory,
                LaLoMinuteFactory,
                LaLoSecondFactory,
                UTMFactory,
                MGRSFactory
        )

        val coordinates = listOf(
                LaLoSecond(NORTH, 44, 59, 45.0, EAST, 37, 20, 4.0),
                LaLoSecond(NORTH, 41, 37, 0.0, EAST, 41, 35, 23.0),
                LaLoSecond(NORTH, 43, 12, 31.0, EAST, 44, 35, 16.0),
                LaLoSecond(NORTH, 44, 34, 1.0, EAST, 38, 0, 12.0),
                LaLoSecond(NORTH, 43, 6, 14.0, EAST, 40, 34, 32.0)
        )

        @JvmField
        @DataPoints
        val dataPoints = cross(factories.toSet(), coordinates.toSet())
    }


    @Theory
    fun testConversion(input: Pair<CoordinateFactory<*>, LaLoSecond>) {
        val factory = input.first
        val laLo = input.second.toLaLoDegree()

        val fromFactory = factory.fromLaLoDegree(laLo)
        val toLaLo = fromFactory.toLaLoDegree()

        assertEquals(laLo.print(), toLaLo.print())
    }

    @Theory
    fun testParsing(input: Pair<CoordinateFactory<*>, LaLoSecond>) {
        val factory = input.first
        val laLo = input.second.toLaLoDegree()

        val fromFactory = factory.fromLaLoDegree(laLo)
        val printed = fromFactory.print()

        val state = Parser().getCoordinateParser().getParseState()
        var lastResult = ParseResult(emptySet<Coordinate>(), false)

        printed.forEach {
            assertTrue(state.getAcceptedChars().contains(it))

            lastResult = state.onChar(it)
        }

        assertTrue(lastResult.result.size == 1)
        assertEquals(fromFactory.print(), lastResult.result.iterator().next().print())
    }
}