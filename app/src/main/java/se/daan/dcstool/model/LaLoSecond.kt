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
): LaLo<SecondLaPart, SecondLoPart> {
    return LaLo(
            SecondLaPart(latitudeHemisphere, latitudeDegrees, latitudeMinutes, latitudeSeconds),
            SecondLoPart(longitudeHemisphere, longitudeDegrees, longitudeMinutes, longitudeSeconds)
    )
}

object LaLoSecondFactory : CoordinateFactory<LaLo<SecondLaPart, SecondLoPart>> {
    override fun fromLaLoDegree(laLoDegree: LaLo<DegreeLaPart, DegreeLoPart>): LaLo<SecondLaPart, SecondLoPart> {
        return fromLaLoMinute(LaLoMinuteFactory.fromLaLoDegree(laLoDegree));
    }

    fun fromLaLoMinute(laLoMinute: LaLo<MinuteLaPart, MinuteLoPart>): LaLo<SecondLaPart, SecondLoPart> {
        val latMS = splitDecimal(laLoMinute.lat.minute)
        val lonMS = splitDecimal(laLoMinute.lon.minute)

        return laLoSecond(
                laLoMinute.lat.hemi, laLoMinute.lat.degree, latMS.first, latMS.second,
                laLoMinute.lon.hemi, laLoMinute.lon.degree, lonMS.first, lonMS.second
        )
    }
}

abstract class SecondPart : LaLoPart {
    abstract val hemi: Hemisphere
    abstract val degree: Int
    abstract val minute: Int
    abstract val second: Double
    abstract val degreePatternPrefix: String

    override fun print(): String {
        val latChar = hemi.abbreviation

        val latDStr = format(degree, "${degreePatternPrefix}00")
        val latMStr = format(minute, "00")
        val latSStr = format(second, "00")

        return "$latChar $latDStr° $latMStr' $latSStr\""
    }
}

data class SecondLaPart(
        override val hemi: Hemisphere,
        override val degree: Int,
        override val minute: Int,
        override val second: Double
) : SecondPart(), LaPart {
    override val degreePatternPrefix = ""

    override fun toDegreeLaPart(): DegreeLaPart {
        return toMinuteLaPart().toDegreeLaPart()
    }

    fun toMinuteLaPart(): MinuteLaPart {
        return MinuteLaPart(hemi, degree, toDecimal(minute, second))
    }
}

data class SecondLoPart(
        override val hemi: Hemisphere,
        override val degree: Int,
        override val minute: Int,
        override val second: Double
) : SecondPart(), LoPart {
    override val degreePatternPrefix = "0"

    override fun toDegreeLoPart(): DegreeLoPart {
        return toMinuteLoPart().toDegreeLoPart()
    }

    fun toMinuteLoPart(): MinuteLoPart {
        return MinuteLoPart(hemi, degree, toDecimal(minute, second))
    }
}