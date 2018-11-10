package daan.se.dcstool.model

import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.truncate

data class LaLoMinute
(
        val latitudeHemisphere: Hemisphere,
        val latitudeDegrees: Int,
        val latitudeMinutes: Double,
        val longitudeHemisphere: Hemisphere,
        val longitudeDegrees: Int,
        val longitudeMinutes: Double
) {


    fun print(): String {
        val latChar = latitudeHemisphere.abbreviation
        val lonChar = longitudeHemisphere.abbreviation

        val latDStr = DecimalFormat("00").format(latitudeDegrees)
        val latMStr = DecimalFormat("00.00").format(latitudeMinutes)
        val lonDStr = DecimalFormat("000").format(longitudeDegrees)
        val lonMStr = DecimalFormat("00.00").format(longitudeMinutes)

        return "$latChar $latDStr\u00b0$latMStr' $lonChar $lonDStr\u00b0$lonMStr'"
    }

    fun toLaLoDegree(): LaLoDegree {
        val lat = latitudeDegrees.toDouble() + (latitudeMinutes / 60.0)

        val lon = longitudeDegrees.toDouble() + (longitudeMinutes / 60.0)

        return LaLoDegree(latitudeHemisphere,lat, longitudeHemisphere, lon)
    }

    companion object {
        fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLoMinute {
            val latD = truncate(laLoDegree.latitudeDegrees)
            val latM = 60 * (laLoDegree.latitudeDegrees - latD)

            val lonD = truncate(laLoDegree.longitudeDegrees)
            val lonM = 60 * (laLoDegree.longitudeDegrees - lonD)

            return LaLoMinute(laLoDegree.latitudeHemisphere, latD.toInt(), latM, laLoDegree.longitudeHemisphere, lonD.toInt(), lonM)
        }
    }
}