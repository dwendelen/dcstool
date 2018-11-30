package se.daan.dcstool.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

data class LaLoDegree
(
        val latitudeHemisphere: Hemisphere,
        val latitudeDegrees: Double,
        val longitudeHemisphere: Hemisphere,
        val longitudeDegrees: Double
) : Coordinate
{
    override fun print(): String {
        val latChar = latitudeHemisphere.abbreviation
        val lonChar = longitudeHemisphere.abbreviation

        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = '.'

        val latStr = DecimalFormat("00.0000", symbols).format(latitudeDegrees)
        val longStr = DecimalFormat("000.0000", symbols).format(longitudeDegrees)

        return "$latChar $latStr° $lonChar $longStr°"
    }

    override fun toLaLoDegree(): LaLoDegree {
        return this
    }
}

object LaLoDegreeFactory : CoordinateFactory<LaLoDegree> {
    override fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLoDegree {
        return laLoDegree
    }
}