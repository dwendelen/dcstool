package se.daan.dcstool.model

typealias LaLoDegree = LaLo<DegreePart, DegreePart>

object LaLoDegreeFactory : CoordinateFactory<LaLoDegree> {
    override fun fromLaLoDegree(laLoDegree: LaLoDegree): LaLoDegree {
        return laLoDegree
    }
}

data class DegreePart (
    val hemi: Hemisphere,
    val degree: Double,
    val type: PartType
) : LaLoPart {
    override fun print(): String {
        val h = hemi.abbreviation
        val d = format(degree, "${type.format}.0000")

        return "$h $d°"
    }

    override fun toDegreePart(): DegreePart {
        return this
    }
}