package se.daan.dcstool.model.parser.lalodegree

import se.daan.dcstool.model.*
import se.daan.dcstool.model.parser.*
import se.daan.dcstool.model.parser.IntRange

fun newLaLoDegreeFormat(): LaLoFormat0 {
    val ns = HemiPiece0(Hemisphere.NORTH, Hemisphere.SOUTH)
    val ns_range = IntRange("00", "89")
    val lat = DegreePart0(ns, ns_range)

    val ew = HemiPiece0(Hemisphere.EAST, Hemisphere.WEST)
    val ew_range = IntRange("000", "179")
    val lon = DegreePart0(ew, ew_range)

    return LaLoFormat0(lat, lon)
}

interface DegreePart1Done: PartPieceNotInit

data class DegreePart0(
        val hemi: HemiPiece0,
        val intRange: IntRange
) : DelegatingPiece<HemiInput, Hemisphere, DegreePart1>(),
        PartPieceInit<DegreePart1>
{
    override val currentPiece = hemi

    override fun print(): CharSequence {
        return hemi.print()
    }

    override fun handleCurrent(newPiece: Hemisphere): DegreePart1 {
        return DegreePart1(newPiece, Decimal0(intRange, ""))
    }
}


data class DegreePart1(
        val hemi: Hemisphere,
        val degree: Decimal0
) : DelegatingPiece<DigitInput, DecimalHelper<*>, DegreePart1Done>(),
        DegreePart1Done,
        PartPieceIntermediate<DegreePart1Done>
{
    override val currentPiece = degree

    override fun handleCurrent(newPiece: DecimalHelper<*>): DegreePart1Done {
        return when (newPiece) {
            is Decimal0 -> DegreePart1(
                    hemi,
                    newPiece
            )
            is DecimalDone -> DegreePartDone(
                    hemi,
                    newPiece
            )
        }
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} ${degree.print()}°"
    }
}

data class DegreePartDone(
        val hemi: Hemisphere,
        val degree: DecimalDone
) : DelegatingPiece<DigitInput, Decimal2, DegreePartDone>(),
        DegreePart1Done,
        PartPieceDone<DegreePartDone, DegreePart>
{
    override val currentPiece = degree

    override fun handleCurrent(newPiece: Decimal2): DegreePartDone {
        return DegreePartDone(hemi, newPiece)
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} ${degree.print()}°"
    }

    override fun getPart(type: PartType): DegreePart {
        return DegreePart(hemi, degree.double, type)
    }
}