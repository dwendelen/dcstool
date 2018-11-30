package se.daan.dcstool.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.truncate

data class LaLoMinute
(
        val latitudeHemisphere: Hemisphere,
        val latitudeDegrees: Int,
        val latitudeMinutes: Double,
        val longitudeHemisphere: Hemisphere,
        val longitudeDegrees: Int,
        val longitudeMinutes: Double
): Coordinate
{

    override fun print(): String {
        val latChar = latitudeHemisphere.abbreviation
        val lonChar = longitudeHemisphere.abbreviation

        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = '.'

        val latDStr = DecimalFormat("00", symbols).format(latitudeDegrees)
        val latMStr = DecimalFormat("00.00", symbols).format(latitudeMinutes)
        val lonDStr = DecimalFormat("000", symbols).format(longitudeDegrees)
        val lonMStr = DecimalFormat("00.00", symbols).format(longitudeMinutes)

        return "$latChar $latDStr° $latMStr' $lonChar $lonDStr° $lonMStr'"
    }

    override fun toLaLoDegree(): LaLoDegree {
        val lat = latitudeDegrees.toDouble() + (latitudeMinutes / 60.0)

        val lon = longitudeDegrees.toDouble() + (longitudeMinutes / 60.0)

        return LaLoDegree(latitudeHemisphere,lat, longitudeHemisphere, lon)
    }
}

object LaLoMinuteFactory: CoordinateFactory<LaLoMinute> {
    override  fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLoMinute {
        val latD = truncate(laLoDegree.latitudeDegrees)
        val latM = 60 * (laLoDegree.latitudeDegrees - latD)

        val lonD = truncate(laLoDegree.longitudeDegrees)
        val lonM = 60 * (laLoDegree.longitudeDegrees - lonD)

        return LaLoMinute(laLoDegree.latitudeHemisphere, latD.toInt(), latM, laLoDegree.longitudeHemisphere, lonD.toInt(), lonM)
    }
}