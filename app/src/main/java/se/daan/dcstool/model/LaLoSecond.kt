package se.daan.dcstool.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import kotlin.math.truncate

data class LaLoSecond
(
        val latitudeHemisphere: Hemisphere,
        val latitudeDegrees: Int,
        val latitudeMinutes: Int,
        val latitudeSeconds: Double,
        val longitudeHemisphere: Hemisphere,
        val longitudeDegrees: Int,
        val longitudeMinutes: Int,
        val longitudeSeconds: Double
) : Coordinate
{
    override fun print(): String {
        val latChar = latitudeHemisphere.abbreviation
        val lonChar = longitudeHemisphere.abbreviation

        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = '.'

        val latDStr = DecimalFormat("00", symbols).format(abs(latitudeDegrees))
        val latMStr = DecimalFormat("00", symbols).format(abs(latitudeMinutes))
        val latSStr = DecimalFormat("00", symbols).format(abs(latitudeSeconds))
        val lonDStr = DecimalFormat("000", symbols).format(abs(longitudeDegrees))
        val lonMStr = DecimalFormat("00", symbols).format(abs(longitudeMinutes))
        val lonSStr = DecimalFormat("00", symbols).format(abs(longitudeSeconds))

        return "$latChar $latDStr° $latMStr' $latSStr\" $lonChar $lonDStr° $lonMStr' $lonSStr\""
    }

    override fun toLaLoDegree(): LaLoDegree {
        return toLaLoMinute().toLaLoDegree();
    }

    fun toLaLoMinute(): LaLoMinute {
        val latM = latitudeMinutes.toDouble() + (latitudeSeconds / 60.0)

        val lonM = longitudeMinutes.toDouble() + (longitudeSeconds / 60.0)

        return LaLoMinute(latitudeHemisphere,latitudeDegrees, latM, longitudeHemisphere, longitudeDegrees, lonM)
    }
}

object LaLoSecondFactory: CoordinateFactory<LaLoSecond> {
    override fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLoSecond {
        return fromLaLoMinute(LaLoMinuteFactory.fromLaLoDegree(laLoDegree));
    }

    fun fromLaLoMinute(laLoMinute: LaLoMinute): LaLoSecond {
        val latM = truncate(laLoMinute.latitudeMinutes)
        val latS = 60.0 * (laLoMinute.latitudeMinutes - latM)

        val lonM = truncate(laLoMinute.longitudeMinutes)
        val lonS = 60.0 * (laLoMinute.longitudeMinutes - lonM)

        return LaLoSecond(
                laLoMinute.latitudeHemisphere, laLoMinute.latitudeDegrees, latM.toInt(), latS,
                laLoMinute.longitudeHemisphere, laLoMinute.longitudeDegrees, lonM.toInt(), lonS
        )
    }
}