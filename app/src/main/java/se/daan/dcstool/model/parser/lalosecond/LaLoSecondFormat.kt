package se.daan.dcstool.model.parser.lalosecond

import se.daan.dcstool.model.Hemisphere
import se.daan.dcstool.model.SecondPart
import se.daan.dcstool.model.PartType
import se.daan.dcstool.model.parser.*
import se.daan.dcstool.model.parser.IntRange


interface SecondPart12: PartPieceNotInit
interface SecondPart23: PartPieceNotInit
interface SecondPart3Done: PartPieceNotInit

fun newLaLoSecondFormat(): LaLoFormat0 {
    val ns = HemiPiece0(Hemisphere.NORTH, Hemisphere.SOUTH)
    val ns_range = IntRange("00", "89")
    val lat = SecondPart0(ns, ns_range)

    val ew = HemiPiece0(Hemisphere.EAST, Hemisphere.WEST)
    val ew_range = IntRange("000", "179")
    val lon = SecondPart0(ew, ew_range)

    return LaLoFormat0(lat, lon)
}


data class SecondPart0(
        val hemi: HemiPiece0,
        val intRange: IntRange
) : DelegatingPiece<HemiInput, Hemisphere, SecondPart1>(),
    PartPieceInit<SecondPart1>
{
    override val currentPiece = hemi

    override fun print(): CharSequence {
        return hemi.print()
    }

    override fun handleCurrent(newPiece: Hemisphere): SecondPart1 {
        return SecondPart1(newPiece, Int0(intRange, ""))
    }
}

data class SecondPart1(
        val hemi: Hemisphere,
        val degree: Int0
) : DelegatingPiece<DigitInput, IntHelper, SecondPart12>(),
        SecondPart12,
        PartPieceIntermediate<SecondPart12>
{
    override val currentPiece = degree

    override fun handleCurrent(newPiece: IntHelper): SecondPart12 {
        return when (newPiece) {
            is Int0 -> SecondPart1(hemi, newPiece)
            is IntDone -> SecondPart2(hemi, newPiece.int, Int0(IntRange("00", "59"), ""))
        }
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} ${degree.print()}°"
    }
}

data class SecondPart2(
        val hemi: Hemisphere,
        val degree: Int,
        val minute: Int0
) : DelegatingPiece<DigitInput, IntHelper, SecondPart23>(),
        SecondPart12,
        SecondPart23,
        PartPieceIntermediate<SecondPart23>
{
    override val currentPiece = minute

    override fun handleCurrent(newPiece: IntHelper): SecondPart23 {
        return when (newPiece) {
            is Int0 -> SecondPart2(hemi, degree, newPiece)
            is IntDone -> SecondPart3(hemi, degree, newPiece.int, Decimal0(IntRange("00", "59"), ""))
        }
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} $degree° ${minute.print()}'"
    }
}

data class SecondPart3(
        val hemi: Hemisphere,
        val degree: Int,
        val minute: Int,
        val second: Decimal0
) : DelegatingPiece<DigitInput, DecimalHelper<*>, SecondPart3Done>(),
        SecondPart23,
        SecondPart3Done,
        PartPieceIntermediate<SecondPart3Done>
{
    override val currentPiece = second

    override fun handleCurrent(newPiece: DecimalHelper<*>): SecondPart3Done {
        return when (newPiece) {
            is Decimal0 -> SecondPart3(
                    hemi,
                    degree,
                    minute,
                    newPiece
            )
            is DecimalDone -> SecondPartDone(
                    hemi,
                    degree,
                    minute,
                    newPiece
            )
        }
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} $degree° $minute' ${second.print()}\""
    }
}

data class SecondPartDone(
        val hemi: Hemisphere,
        val degree: Int,
        val minute: Int,
        val second: DecimalDone
) : DelegatingPiece<DigitInput, Decimal2, SecondPartDone>(),
        SecondPart3Done,
        PartPieceDone<SecondPartDone, SecondPart>
{
    override val currentPiece = second

    override fun handleCurrent(newPiece: Decimal2): SecondPartDone {
        return SecondPartDone(hemi, degree, minute, newPiece)
    }

    override fun print(): CharSequence {
        return "${hemi.abbreviation} $degree° $minute' ${second.print()}\""
    }

    override fun getPart(type: PartType): SecondPart {
        return SecondPart(hemi, degree, minute, second.double, type)
    }
}