package daan.se.dcstool.model

import java.text.DecimalFormat

data class LaLoDegree
(
        val latitudeHemisphere: Hemisphere,
        val latitudeDegrees: Double,
        val longitudeHemisphere: Hemisphere,
        val longitudeDegrees: Double
) {
    fun print(): String {
        val latChar = latitudeHemisphere.abbreviation
        val lonChar = longitudeHemisphere.abbreviation

        val latStr = DecimalFormat("00.0000").format(latitudeDegrees)
        val longStr = DecimalFormat("000.0000").format(longitudeDegrees)

        return "$latChar $latStr\u00b0 $lonChar $longStr\u00b0"
    }
}