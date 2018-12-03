package se.daan.dcstool.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.truncate

interface LaLoPart {
    fun print(): String
    fun toDegreePart(): DegreePart
}

enum class PartType(val format: String) {
    Latitude("00"),
    Longitude("000");
}



data class LaLo<La : LaLoPart, Lo : LaLoPart>(
        val lat: La,
        val lon: Lo
) : Coordinate {
    override fun toLaLoDegree(): LaLoDegree {
        val latD = lat.toDegreePart()
        val lonD = lon.toDegreePart()

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