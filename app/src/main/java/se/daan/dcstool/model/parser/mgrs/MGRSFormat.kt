package se.daan.dcstool.model.parser.mgrs

import se.daan.dcstool.model.*
import se.daan.dcstool.model.parser.*
import se.daan.dcstool.model.parser.IntRange
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

interface MGRSFormat01

fun newMGRSFormat(): MGRSFormat0 {
    return MGRSFormat0(
            Int0(IntRange("01", "60"), "")
    )
}

data class MGRSFormat0(
        val zone: Int0
) : DelegatingPiece<DigitInput, IntHelper, MGRSFormat01>(),
        MGRSFormat01 {
    override val currentPiece = zone

    override fun handleCurrent(newPiece: IntHelper): MGRSFormat01 {
        return when (newPiece) {
            is Int0 -> MGRSFormat0(newPiece)
            is IntDone -> MGRSFormat1(newPiece.int)
        }
    }

    override fun print(): CharSequence {
        return zone.print()
    }
}

data class MGRSFormat1(
        val zone: Int
) : DelegatingPiece<LatitudeBandInput, LatitudeBand, MGRSFormat2>(),
        MGRSFormat01 {
    override val currentPiece = LatitudeBandPiece(zone)

    override fun handleCurrent(newPiece: LatitudeBand): MGRSFormat2 {
        return MGRSFormat2(zone, newPiece)
    }

    override fun print(): CharSequence {
        return "${format(zone, "00")}${currentPiece.print()}"
    }
}

data class MGRSFormat2(
        val zone: Int,
        val latitudeBand: LatitudeBand
) : DelegatingPiece<ColumnLetterInput, ColumnLetter, MGRSFormat3>() {
    override val currentPiece = ColumnLetterPiece(zone)

    override fun handleCurrent(newPiece: ColumnLetter): MGRSFormat3 {
        return MGRSFormat3(zone, latitudeBand, newPiece)
    }

    override fun print(): CharSequence {
        return "${format(zone, "00")}${latitudeBand.name} ${currentPiece.print()}"
    }
}

data class MGRSFormat3(
        val zone: Int,
        val latitudeBand: LatitudeBand,
        val columnLetter: ColumnLetter
) : DelegatingPiece<RowLetterInput, RowLetter, MGRSFormat4>() {
    override val currentPiece = RowLetterPiece(zone, latitudeBand)

    override fun handleCurrent(newPiece: RowLetter): MGRSFormat4 {
        return MGRSFormat4(zone, latitudeBand, columnLetter, newPiece)
    }

    override fun print(): CharSequence {
        return "${format(zone, "00")}${latitudeBand.name} ${columnLetter.name}${currentPiece.print()}"
    }
}

data class MGRSFormat4(
        val zone: Int,
        val latitudeBand: LatitudeBand,
        val columnLetter: ColumnLetter,
        val rowLetter: RowLetter
) : DelegatingPiece<DigitInput, ENPieceOdd, MGRSFormatOdd>() {
    override val currentPiece = ENPiece0

    override fun handleCurrent(newPiece: ENPieceOdd): MGRSFormatOdd {
        return MGRSFormatOdd(zone, latitudeBand, columnLetter, rowLetter, newPiece)
    }

    override fun print(): CharSequence {
        return "${format(zone, "00")}${latitudeBand.name} ${columnLetter.name}${rowLetter.name} ${currentPiece.print()}"
    }
}

data class MGRSFormatOdd(
        val zone: Int,
        val latitudeBand: LatitudeBand,
        val columnLetter: ColumnLetter,
        val rowLetter: RowLetter,
        val en: ENPieceOdd
) : DelegatingPiece<DigitInput, ENPieceEvenOrDone<*>, MGRSFormatEven<*>>() {
    override val currentPiece = en

    override fun handleCurrent(newPiece: ENPieceEvenOrDone<*>): MGRSFormatEven<*> {
        return MGRSFormatEven(zone, latitudeBand, columnLetter, rowLetter, newPiece)
    }

    override fun print(): CharSequence {
        return "${format(zone, "00")}${latitudeBand.name} ${columnLetter.name}${rowLetter.name} ${currentPiece.print()}"
    }
}

data class MGRSFormatEven<O>(
        val zone: Int,
        val latitudeBand: LatitudeBand,
        val columnLetter: ColumnLetter,
        val rowLetter: RowLetter,
        val en: ENPieceEvenOrDone<O>
) : DelegatingPiece<DigitInput, O, MGRSFormatOdd>(),
        FinalFormat {
    override val currentPiece = en

    override fun handleCurrent(newPiece: O): MGRSFormatOdd {
        return when(newPiece) {
            is ENPieceOdd -> MGRSFormatOdd(zone, latitudeBand, columnLetter, rowLetter, newPiece)
            else -> throw IllegalStateException("Bad input")
        }
    }

    override fun print(): CharSequence {
        return "${format(zone, "00")}${latitudeBand.name} ${columnLetter.name}${rowLetter.name} ${currentPiece.print()}"
    }

    override val coordinate: Coordinate
        get() {
            val done = en.toDone()

            return MGRS(zone, latitudeBand, columnLetter, rowLetter, done.easting.toDouble(), done.northing.toDouble())
        }
}

data class LatitudeBandPiece(
        val zone: Int
) : Piece<LatitudeBandInput, LatitudeBand> {
    override val inputs: Collection<LatitudeBandInput>
        get() = LatitudeBand.values().map { LatitudeBandInput(it) }

    override fun handle(input: LatitudeBandInput): LatitudeBand {
        return input.latitudeBand
    }

    override fun print(): CharSequence {
        return "_"
    }
}

data class ColumnLetterPiece(
        val zone: Int
) : Piece<ColumnLetterInput, ColumnLetter> {
    override val inputs: Collection<ColumnLetterInput>
        get() = ColumnLetter.inZone(zone)
                .map { ColumnLetterInput(it) }

    override fun handle(input: ColumnLetterInput): ColumnLetter {
        return input.columnLetter
    }

    override fun print(): CharSequence {
        return "_"
    }
}

data class RowLetterPiece(
        val zone: Int,
        val latitudeBand: LatitudeBand
) : Piece<RowLetterInput, RowLetter> {
    override val inputs: Collection<RowLetterInput>
        get() = RowLetter.inZoneAndLatitudeBand(zone, latitudeBand)
                .map { RowLetterInput(it) }

    override fun handle(input: RowLetterInput): RowLetter {
        return input.rowLetter
    }

    override fun print(): CharSequence {
        return "_"
    }
}


sealed class ENPiece<O> : Piece<DigitInput, O>
abstract class ENPieceEvenOrDone<O> : ENPiece<O>() {
    abstract fun toDone(): ENPieceDone
}

object ENPiece0 : ENPiece<ENPieceOdd>() {
    override val inputs = ('0'..'9').map { DigitInput(it) }

    override fun handle(input: DigitInput): ENPieceOdd {
        return ENPieceOdd(input.char.toString())
    }

    override fun print(): CharSequence {
        return "_"
    }
}

data class ENPieceOdd(
        val string: String
) : ENPiece<ENPieceEvenOrDone<*>>() {
    override val inputs = ('0'..'9').map { DigitInput(it) }

    override fun handle(input: DigitInput): ENPieceEvenOrDone<*> {
        val newString = string + input.char

        return if (newString.length == 10)
            ENPieceDone(newString.substring(0, 5).toInt(), newString.substring(5, 10).toInt())
        else
            ENPieceEven(newString)
    }

    override fun print(): CharSequence {
        val idx = string.length / 2 + 1
        val part1 = string.substring(0, idx)
        val part2 = string.substring(idx, string.length)

        return "$part1 ${part2}_"
    }
}

data class ENPieceEven(
        val string: String
) : ENPieceEvenOrDone<ENPieceOdd>() {
    override val inputs = ('0'..'9').map { DigitInput(it) }

    override fun handle(input: DigitInput): ENPieceOdd {
        val newString = string + input.char

        return ENPieceOdd(newString)
    }

    override fun print(): CharSequence {
        val idx = string.length / 2
        val part1 = string.substring(0, idx)
        val part2 = string.substring(idx, string.length)

        return "$part1 ${part2}_"
    }

    override fun toDone(): ENPieceDone {
        val idx = string.length / 2
        val part1 = string.substring(0, idx)
        val part2 = string.substring(idx, string.length)

        val padded1 = (part1 + "5").padEnd(5, '0')
        val padded2 = (part2 + "5").padEnd(5, '0')

        return ENPieceDone(padded1.toInt(), padded2.toInt())
    }
}

data class ENPieceDone(
        val easting: Int,
        val northing: Int
) : ENPieceEvenOrDone<Nothing>() {
    override fun toDone(): ENPieceDone {
        return this
    }

    override val inputs = emptySet<DigitInput>()

    override fun handle(input: DigitInput): Nothing {
        throw IllegalStateException("Bad input")
    }

    override fun print(): CharSequence {
        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = '.'

        val decimalFormat = DecimalFormat("00000", symbols)
        decimalFormat.roundingMode = RoundingMode.DOWN

        val e = decimalFormat.format(easting)
        val n = decimalFormat.format(northing)

        return "$e $n"
    }
}