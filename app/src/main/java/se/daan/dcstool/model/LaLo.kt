package se.daan.dcstool.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.truncate

interface LaLoPart {
    fun print(): String
}

interface LaPart : LaLoPart {
    fun toDegreeLaPart(): DegreeLaPart
}

interface LoPart : LaLoPart {
    fun toDegreeLoPart(): DegreeLoPart
}

data class LaLo<La : LaPart, Lo : LoPart>(
        val lat: La,
        val lon: Lo
) : Coordinate {
    override fun toLaLoDegree(): LaLo<DegreeLaPart, DegreeLoPart> {
        val latD = lat.toDegreeLaPart()
        val lonD = lon.toDegreeLoPart()

        return LaLo(latD, lonD)
    }

    override fun print(): String {
        return "${lat.print()} ${lon.print()}"
    }
}

fun splitDecimal(decimal: Double): Pair<Int, Double> {
    val int = truncate(decimal)
    val frac = 60 * (decimal - int)

    return Pair(int.toInt(), frac)
}

fun toDecimal(integer: Int, fraction: Double): Double {
    return integer.toDouble() + fraction / 60.0
}

fun format(number: Number, pattern: String): String {
    return DecimalFormat(pattern, symbols).format(number)
}

private val symbols: DecimalFormatSymbols = {
    val s = DecimalFormatSymbols()
    s.decimalSeparator = '.'
    s
}()