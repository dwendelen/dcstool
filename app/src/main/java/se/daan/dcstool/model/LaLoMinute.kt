package se.daan.dcstool.model

object LaLoMinuteFactory : CoordinateFactory<LaLo<MinutePart, MinutePart>> {
    override fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLo<MinutePart, MinutePart> {
        val latDM = splitDecimal(laLoDegree.lat.degree)
        val lat = MinutePart(laLoDegree.lat.hemi, latDM.first, latDM.second, PartType.Latitude)

        val lonDM = splitDecimal(laLoDegree.lon.degree)
        val lon = MinutePart(laLoDegree.lon.hemi, lonDM.first, lonDM.second, PartType.Longitude)

        return LaLo(lat, lon)
    }
}

data class MinutePart(
        val hemi: Hemisphere,
        val degree: Int,
        val minute: Double,
        val type: PartType
) : LaLoPart {
    override fun print(): String {
        val h = hemi.abbreviation
        val d = format(degree, type.format)
        val m = format(minute, "00.00")

        return "$h $d° $m'"
    }

    override fun toDegreePart(): DegreePart {
        return DegreePart(hemi, toDecimal(degree, minute), type)
    }
}
