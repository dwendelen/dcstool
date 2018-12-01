package se.daan.dcstool.model

object LaLoDegreeFactory : CoordinateFactory<LaLo<DegreeLaPart, DegreeLoPart>> {
    override fun fromLaLoDegree(laLoDegree: LaLo<DegreeLaPart, DegreeLoPart>): LaLo<DegreeLaPart, DegreeLoPart> {
        return laLoDegree
    }
}

abstract class DegreePart : LaLoPart {
    abstract val hemi: Hemisphere
    abstract val degree: Double
    abstract val degreePatternPrefix: String

    override fun print(): String {
        val h = hemi.abbreviation
        val d = format(degree, "${degreePatternPrefix}00.0000")

        return "$h $d°"
    }
}

data class DegreeLaPart(
        override val hemi: Hemisphere,
        override val degree: Double
) : DegreePart(), LaPart {
    override val degreePatternPrefix = ""

    override fun toDegreeLaPart(): DegreeLaPart {
        return this
    }
}

data class DegreeLoPart(
        override val hemi: Hemisphere,
        override val degree: Double
) : DegreePart(), LoPart {
    override val degreePatternPrefix = "0"

    override fun toDegreeLoPart(): DegreeLoPart {
        return this
    }
}