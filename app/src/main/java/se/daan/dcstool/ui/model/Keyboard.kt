package se.daan.dcstool.ui.model

import se.daan.dcstool.model.Hemisphere
import se.daan.dcstool.model.parser.*

data class Keyboard(
        val rows: List<Row>
)

data class Row(
        val keys: List<Key>
)

sealed class Key {
    abstract val text: CharSequence
}
object EmptyKey : Key() {
    override val text = ""
}

data class DisabledKey(val char: Char) : Key() {
    override val text = char.toString()
}

object ModeKey : Key() {
    override val text = "mode"
}

object BackKey : Key() {
    override val text = "<"
}

data class InputKey(val input: Input) : Key() {
    override val text = input.charToDisplay.toString()
}

fun getKeyboards(inputs: Collection<Input>): List<Keyboard> {
    val sortedInputs = inputs.sortedBy { it.charToDisplay }

    val digitInputs = arrayListOf<DigitInput>()
    val hemiInputs = arrayListOf<HemiInput>()
    val northLatInputs = arrayListOf<LatitudeBandInput>()
    val southLatInputs = arrayListOf<LatitudeBandInput>()
    val gridInputs = arrayListOf<GridInput>()

    sortedInputs.forEach {
        when (it) {
            is DigitInput -> digitInputs.add(it)
            is HemiInput -> hemiInputs.add(it)
            is LatitudeBandInput ->
                if (it.latitudeBand.hemisphere == Hemisphere.NORTH)
                    northLatInputs.add(it)
                else
                    southLatInputs.add(it)
            is GridInput -> gridInputs.add(it)
        }
    }

    val keyboards = arrayListOf<Keyboard>()
    if (digitInputs.isNotEmpty() || hemiInputs.isNotEmpty()) {
        keyboards.add(createDigitKeyboard(digitInputs, hemiInputs))
    }
    if (northLatInputs.isNotEmpty()) {
        keyboards.add(createLetterKeyboard(northLatInputs))
    }
    if (southLatInputs.isNotEmpty()) {
        keyboards.add(createLetterKeyboard(southLatInputs))
    }
    if (gridInputs.isNotEmpty()) {
        keyboards.add(createLetterKeyboard(gridInputs))
    }

    return if (keyboards.isEmpty()) {
        listOf(emptyKeyboard())
    } else {
        keyboards
    }
}

fun emptyKeyboard(): Keyboard {
    return Keyboard(listOf(
            Row(listOf(ModeKey, EmptyKey, EmptyKey, BackKey))
    ))
}

fun createDigitKeyboard(
        digits: List<DigitInput>,
        hemis: List<HemiInput>
): Keyboard {
    if (hemis.size > 2) {
        throw IllegalStateException("There should be at most 2 hemi inputs")
    }
    if (digits.size > 10) {
        throw IllegalStateException("There should be at most 10 digits")
    }

    val num1 = ('1'..'3').map { digitToKey(digits, it) }
    val num2 = ('4'..'6').map { digitToKey(digits, it) }
    val num3 = ('7'..'9').map { digitToKey(digits, it) }

    val hemi1 = inputToKey(hemis, 0)
    val hemi2 = inputToKey(hemis, 1)

    val row1 = listOf(hemi1) + num1
    val row2 = listOf(hemi2) + num2
    val row3 = listOf(BackKey) + num3
    val row4 = listOf(
            ModeKey,
            digitToKey(digits, '0'),
            digitToKey(digits, '0'),
            digitToKey(digits, '.')
    )

    return Keyboard(listOf(
            Row(row1),
            Row(row2),
            Row(row3),
            Row(row4)
    ))
}


fun digitToKey(digits: List<DigitInput>, char: Char): Key {
    return if (digits.contains(DigitInput(char))) {
        InputKey(DigitInput(char))
    } else {
        DisabledKey(char)
    }
}

fun createLetterKeyboard(
        inputs: List<Input>
): Keyboard {
    if(inputs.size > 10) {
        throw IllegalStateException("No more than 10 letters allowed")
    }

    val row1 = (0..3).map { inputToKey(inputs, it) }
    val row2 = (4..7).map { inputToKey(inputs, it) }
    val row3 = (8..9).map { inputToKey(inputs, it) }

    return Keyboard(listOf(
            Row(row1),
            Row(row2),
            Row(listOf(ModeKey, row3[0], row3[1], BackKey))
    ))
}

fun inputToKey(
        inputs: List<Input>,
        idx: Int
): Key {
    return if (idx < inputs.size) {
        InputKey(inputs[idx])
    } else {
        EmptyKey
    }
}
