package se.daan.dcstool.model.parser

import se.daan.dcstool.model.ColumnLetter
import se.daan.dcstool.model.Hemisphere
import se.daan.dcstool.model.LatitudeBand
import se.daan.dcstool.model.RowLetter

sealed class Input {
    abstract val charToDisplay: Char
}

data class DigitInput(val char: Char) : Input() {
    override val charToDisplay = char
}

data class HemiInput(val hemisphere: Hemisphere) : Input() {
    override val charToDisplay
        get() = hemisphere.abbreviation
}

data class LatitudeBandInput(val latitudeBand: LatitudeBand): Input() {
    override val charToDisplay: Char
        get() = latitudeBand.name[0]
}

data class RowLetterInput(val rowLetter: RowLetter): Input() {
    override val charToDisplay: Char
        get() = rowLetter.name[0]
}

data class ColumnLetterInput(val columnLetter: ColumnLetter): Input() {
    override val charToDisplay: Char
        get() = columnLetter.name[0]
}