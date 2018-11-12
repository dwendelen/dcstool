package daan.se.dcstool.model

import java.text.DecimalFormat
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
) {
    fun print(): String {
        val latChar = latitudeHemisphere.abbreviation
        val lonChar = longitudeHemisphere.abbreviation

        val latDStr = DecimalFormat("00").format(abs(latitudeDegrees))
        val latMStr = DecimalFormat("00").format(abs(latitudeMinutes))
        val latSStr = DecimalFormat("00").format(abs(latitudeSeconds))
        val lonDStr = DecimalFormat("000").format(abs(longitudeDegrees))
        val lonMStr = DecimalFormat("00").format(abs(longitudeMinutes))
        val lonSStr = DecimalFormat("00").format(abs(longitudeSeconds))

        return "$latChar $latDStr\u00b0$latMStr'$latSStr\" $lonChar $lonDStr\u00b0$lonMStr'$lonSStr\""
    }

    fun toLaLoDegree(): LaLoDegree {
        return toLaLoMinute().toLaLoDegree();
    }

    fun toLaLoMinute(): LaLoMinute {
        val latM = latitudeMinutes.toDouble() + (latitudeSeconds / 60.0)

        val lonM = longitudeMinutes.toDouble() + (longitudeSeconds / 60.0)

        return LaLoMinute(latitudeHemisphere,latitudeDegrees, latM, longitudeHemisphere, longitudeDegrees, lonM)
    }

    companion object {
        fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLoSecond {
            return fromLaLoMinute(LaLoMinute.fromLaLoDegree(laLoDegree));
        }

        fun fromLaLoMinute(laLoMinute: LaLoMinute): LaLoSecond {
            val latM = truncate(laLoMinute.latitudeMinutes)
            val latS = 60.0 * (laLoMinute.latitudeMinutes - latM)

            val lonM = truncate(laLoMinute.longitudeMinutes)
            val lonS = 60.0 * (laLoMinute.longitudeMinutes - latM)

            return LaLoSecond(
                    laLoMinute.latitudeHemisphere, laLoMinute.latitudeDegrees, latM.toInt(), latS,
                    laLoMinute.longitudeHemisphere, laLoMinute.longitudeDegrees, lonM.toInt(), lonS
            )
        }
    }
}