package daan.se.dcstool.model

import daan.se.dcstool.model.Hemisphere.EAST
import daan.se.dcstool.model.Hemisphere.NORTH
import daan.se.dcstool.model.parser.*
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

        val anapa            = LaLoSecond(NORTH, 44, 59, 45.0, EAST, 37, 20,  4.0)
        val batumi           = LaLoSecond(NORTH, 41, 37,  0.0, EAST, 41, 35, 23.0)
        val beslan           = LaLoSecond(NORTH, 43, 12, 31.0, EAST, 44, 35, 16.0)
        val gelendzhik       = LaLoSecond(NORTH, 44, 34,  1.0, EAST, 38,  0, 12.0)
        val gudauta          = LaLoSecond(NORTH, 43,  6, 14.0, EAST, 40, 34, 32.0)
        val kobuleti         = LaLoSecond(NORTH, 41, 55, 39.0, EAST, 41, 50, 58.0)
        val kutaisi          = LaLoSecond(NORTH, 42, 10, 33.0, EAST, 42, 27, 58.0)
        val krasnodar_c      = LaLoSecond(NORTH, 45,  5, 11.0, EAST, 38, 57, 21.0)
        val krasnodar_pkk    = LaLoSecond(NORTH, 45,  1, 46.0, EAST, 39, 10, 20.0)
        val krymsk           = LaLoSecond(NORTH, 44, 58, 34.0, EAST, 38,  0, 23.0)
        val maykop           = LaLoSecond(NORTH, 44, 41, 28.0, EAST, 40,  2, 57.0)
        val mineral_nye_vody = LaLoSecond(NORTH, 44, 13, 04.0, EAST, 43,  6, 08.0)
        val mozdok           = LaLoSecond(NORTH, 43, 47, 32.0, EAST, 44, 34, 40.0)
        val nalchik          = LaLoSecond(NORTH, 43, 30, 34.0, EAST, 43, 37, 25.0)
        val novorossiysk     = LaLoSecond(NORTH, 44, 39, 45.0, EAST, 37, 46, 11.0)
        val senaki           = LaLoSecond(NORTH, 42, 14, 35.0, EAST, 42,  2, 01.0)
        val sochi            = LaLoSecond(NORTH, 43, 26, 58.0, EAST, 39, 57, 33.0)
        val soganlug         = LaLoSecond(NORTH, 41, 39, 29.0, EAST, 44, 55, 45.0)
        val sukhumi          = LaLoSecond(NORTH, 42, 51, 08.0, EAST, 41,  8, 35.0)
        val tbilisi          = LaLoSecond(NORTH, 41, 39, 31.0, EAST, 44, 57, 59.0)
        val vaziani          = LaLoSecond(NORTH, 41, 38, 17.0, EAST, 45,  1, 07.0)


        val coordinates = listOf(
                anapa,
                batumi,
                beslan,
                gelendzhik,
                gudauta,
                kobuleti,
                kutaisi,
                krasnodar_c,
                krasnodar_pkk,
                krymsk,
                maykop,
                mineral_nye_vody,
                mozdok,
                nalchik,
                novorossiysk,
                senaki,
                sochi,
                soganlug,
                sukhumi,
                tbilisi,
                vaziani
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