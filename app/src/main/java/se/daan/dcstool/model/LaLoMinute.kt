package se.daan.dcstool.model

object LaLoMinuteFactory : CoordinateFactory<LaLo<MinuteLaPart, MinuteLoPart>> {
    override fun fromLaLoDegree(laLoDegree: LaLo<DegreeLaPart, DegreeLoPart>): LaLo<MinuteLaPart, MinuteLoPart> {
        val latDM = splitDecimal(laLoDegree.lat.degree)
        val lat = MinuteLaPart(laLoDegree.lat.hemi, latDM.first, latDM.second)

        val lonDM = splitDecimal(laLoDegree.lon.degree)
        val lon = MinuteLoPart(laLoDegree.lon.hemi, lonDM.first, lonDM.second)

        return LaLo(lat, lon)
    }
}

abstract class MinutePart : LaLoPart {
    abstract val hemi: Hemisphere
    abstract val degree: Int
    abstract val minute: Double
    abstract val degreePatternPrefix: String

    override fun print(): String {
        val h = hemi.abbreviation
        val d = format(degree, "${degreePatternPrefix}00")
        val m = format(minute, "00.00")

        return "$h $d° $m'"
    }
}

data class MinuteLaPart(
        override val hemi: Hemisphere,
        override val degree: Int,
        override val minute: Double
) : MinutePart(), LaPart {
    override val degreePatternPrefix = ""

    override fun toDegreeLaPart(): DegreeLaPart {
        return DegreeLaPart(hemi, toDecimal(degree, minute))
    }
}

data class MinuteLoPart(
        override val hemi: Hemisphere,
        override val degree: Int,
        override val minute: Double
) : MinutePart(), LoPart {
    override val degreePatternPrefix = "0"

    override fun toDegreeLoPart(): DegreeLoPart {
        return DegreeLoPart(hemi, toDecimal(degree, minute))
    }
}
