package se.daan.dcstool.model

fun laLoSecond(
        latitudeHemisphere: Hemisphere,
        latitudeDegrees: Int,
        latitudeMinutes: Int,
        latitudeSeconds: Double,
        longitudeHemisphere: Hemisphere,
        longitudeDegrees: Int,
        longitudeMinutes: Int,
        longitudeSeconds: Double
): LaLo<SecondPart, SecondPart> {
    return LaLo(
            SecondPart(
                    latitudeHemisphere,
                    latitudeDegrees,
                    latitudeMinutes,
                    latitudeSeconds,
                    PartType.Latitude
            ),
            SecondPart(
                    longitudeHemisphere,
                    longitudeDegrees,
                    longitudeMinutes,
                    longitudeSeconds,
                    PartType.Longitude
            )
    )
}

object LaLoSecondFactory : CoordinateFactory<LaLo<SecondPart, SecondPart>> {
    override fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLo<SecondPart, SecondPart> {
        return fromLaLoMinute(LaLoMinuteFactory.fromLaLoDegree(laLoDegree));
    }

    fun fromLaLoMinute(laLoMinute: LaLo<MinutePart, MinutePart>): LaLo<SecondPart, SecondPart> {
        val latMS = splitDecimal(laLoMinute.lat.minute)
        val lonMS = splitDecimal(laLoMinute.lon.minute)

        return laLoSecond(
                laLoMinute.lat.hemi, laLoMinute.lat.degree, latMS.first, latMS.second,
                laLoMinute.lon.hemi, laLoMinute.lon.degree, lonMS.first, lonMS.second
        )
    }
}

data class SecondPart (
     val hemi: Hemisphere,
     val degree: Int,
     val minute: Int,
     val second: Double,
     val type: PartType
) : LaLoPart {
    override fun toDegreePart(): DegreePart {
        return toMinutePart().toDegreePart()
    }

    fun toMinutePart(): MinutePart {
        return MinutePart(hemi, degree, toDecimal(minute, second), type)
    }

    override fun print(): String {
        val latChar = hemi.abbreviation

        val latDStr = format(degree, type.format)
        val latMStr = format(minute, "00")
        val latSStr = format(second, "00")

        return "$latChar $latDStr° $latMStr' $latSStr\""
    }
}