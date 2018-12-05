package se.daan.dcstool.model.parser.lalominute

import se.daan.dcstool.model.Hemisphere
import se.daan.dcstool.model.MinutePart
import se.daan.dcstool.model.PartType
import se.daan.dcstool.model.parser.*
import se.daan.dcstool.model.parser.IntRange




fun newLaLoMinuteFormat(): LaLoFormat0 {
    val ns = HemiPiece0(Hemisphere.NORTH, Hemisphere.SOUTH)
    val ns_range = IntRange("00", "89")
    val lat = MinutePart0(ns, ns_range)

    val ew = HemiPiece0(Hemisphere.EAST, Hemisphere.WEST)
    val ew_range = IntRange("000", "179")
    val lon = MinutePart0(ew, ew_range)

    return LaLoFormat0(lat, lon)
}

interface MinutePart12: PartPieceNotInit
interface MinutePart2Done: PartPieceNotInit

data class MinutePart0(
        val hemi: HemiPiece0,
        val intRange: IntRange
) : DelegatingPiece<HemiInput, Hemisphere, MinutePart1>(),
    PartPieceInit<MinutePart1>
{
    override val currentPiece = hemi

    override fun print(): CharSequence {
        return hemi.print()
    }

    override fun handleCurrent(newPiece: Hemisphere): MinutePart1 {
        return MinutePart1(newPiece, Int0(intRange, ""))
    }
}

data class MinutePart1(
        val hemi: Hemisphere,
        val degree: Int0
) : DelegatingPiece<DigitInput, IntHelper, MinutePart12>(),
        MinutePart12,
        PartPieceIntermediate<MinutePart12>
{
    override val currentPiece = degree

    override fun handleCurrent(newPiece: IntHelper): MinutePart12 {
        return when (newPiece) {
            is Int0 -> MinutePart1(hemi, newPiece)
            is IntDone -> MinutePart2(hemi, newPiece.int, Decimal0(IntRange("00", "59"), ""))
        }
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} ${degree.print()}°"
    }
}

data class MinutePart2(
        val hemi: Hemisphere,
        val degree: Int,
        val minute: Decimal0
) : DelegatingPiece<DigitInput, DecimalHelper<*>, MinutePart2Done>(),
        MinutePart12,
        MinutePart2Done,
        PartPieceIntermediate<MinutePart2Done>
{
    override val currentPiece = minute

    override fun handleCurrent(newPiece: DecimalHelper<*>): MinutePart2Done {
        return when (newPiece) {
            is Decimal0 -> MinutePart2(
                    hemi,
                    degree,
                    newPiece
            )
            is DecimalDone -> MinutePartDone(
                    hemi,
                    degree,
                    newPiece
            )
        }
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} $degree° ${minute.print()}'"
    }
}

data class MinutePartDone(
        val hemi: Hemisphere,
        val degree: Int,
        val minute: DecimalDone
) : DelegatingPiece<DigitInput, Decimal2, MinutePartDone>(),
        MinutePart2Done,
        PartPieceDone<MinutePartDone, MinutePart>
{
    override val currentPiece = minute

    override fun handleCurrent(newPiece: Decimal2): MinutePartDone {
        return MinutePartDone(hemi, degree, newPiece)
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} $degree° ${minute.print()}'"
    }

    override fun getPart(type: PartType): MinutePart {
        return MinutePart(hemi, degree, minute.double, type)
    }
}