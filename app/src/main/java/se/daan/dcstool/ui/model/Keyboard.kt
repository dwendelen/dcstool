package se.daan.dcstool.ui.model

import se.daan.dcstool.model.parser.*

data class Keyboard(
        val id: CharSequence,
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
    val latInputs = arrayListOf<LatitudeBandInput>()
    val gridInputs = arrayListOf<GridInput>()

    sortedInputs.forEach {
        when (it) {
            is DigitInput -> digitInputs.add(it)
            is HemiInput -> hemiInputs.add(it)
            is LatitudeBandInput -> latInputs.add(it)
            is GridInput -> gridInputs.add(it)
        }
    }

    val keyboards = arrayListOf<Keyboard>()
    createDigitKeyboard(digitInputs, hemiInputs)?.let(keyboards::add)
    createLetterKeyboard1(latInputs)?.let(keyboards::add)
    createLetterKeyboard2(latInputs)?.let(keyboards::add)
    createLetterKeyboard1(gridInputs)?.let(keyboards::add)
    createLetterKeyboard2(gridInputs)?.let(keyboards::add)

    return if (keyboards.isEmpty()) {
        listOf(emptyKeyboard())
    } else {
        keyboards
    }
}

fun emptyKeyboard(): Keyboard {
    return Keyboard("empty", listOf(
            Row(listOf(EmptyKey, EmptyKey, EmptyKey, EmptyKey)),
            Row(listOf(EmptyKey, EmptyKey, EmptyKey, EmptyKey)),
            Row(listOf(BackKey, EmptyKey, EmptyKey, EmptyKey)),
            Row(listOf(ModeKey, EmptyKey, EmptyKey, EmptyKey))
    ))
}

fun createDigitKeyboard(
        digits: List<DigitInput>,
        hemis: List<HemiInput>
): Keyboard? {
    if (hemis.isEmpty() && digits.isEmpty()) {
        return null
    }

    if (hemis.size > 2) {
        throw IllegalStateException("There should be at most 2 hemi inputs")
    }
    if (digits.size > 10) {
        throw IllegalStateException("There should be at most 10 digits")
    }

    val num1 = ('1'..'3').map { digitToKey(digits, it) }
    val num2 = ('4'..'6').map { digitToKey(digits, it) }
    val num3 = ('7'..'9').map { digitToKey(digits, it) }

    val hemi1 = hemiToKey(hemis, 0)
    val hemi2 = hemiToKey(hemis, 1)

    val row1 = listOf(hemi1) + num1
    val row2 = listOf(hemi2) + num2
    val row3 = listOf(BackKey) + num3
    val row4 = listOf(
            ModeKey,
            digitToKey(digits, '0'),
            digitToKey(digits, '0'),
            digitToKey(digits, '.')
    )

    return Keyboard("digit", listOf(
            Row(row1),
            Row(row2),
            Row(row3),
            Row(row4)
    ))
}

fun hemiToKey(
        inputs: List<HemiInput>,
        idx: Int
): Key {
    return if (idx < inputs.size) {
        InputKey(inputs[idx])
    } else {
        EmptyKey
    }
}

fun digitToKey(digits: List<DigitInput>, char: Char): Key {
    return if (digits.contains(DigitInput(char))) {
        InputKey(DigitInput(char))
    } else {
        DisabledKey(char)
    }
}


fun createLetterKeyboard1(
        inputs: List<Input>
): Keyboard? {
    if (inputs.isEmpty()) {
        return null
    }

    val row1 = listOf('A', 'B', 'C', 'D').map { inputToKey(inputs, it) }
    val row2 = listOf('E', 'F', 'G', 'H').map { inputToKey(inputs, it) }
    val row3 = listOf('J', 'K', 'L').map { inputToKey(inputs, it) }
    val row4 = listOf('M', 'N', 'P').map { inputToKey(inputs, it) }

    return Keyboard("letter1", listOf(
            Row(row1),
            Row(row2),
            Row(listOf(BackKey, row3[0], row3[1], row3[2])),
            Row(listOf(ModeKey, row4[0], row4[1], row4[2]))
    )
    )
}

fun createLetterKeyboard2(
        inputs: List<Input>
): Keyboard? {
    if (inputs.isEmpty()) {
        return null
    }

    val row1 = listOf('L', 'M', 'N', 'P').map { inputToKey(inputs, it) }
    val row2 = listOf('Q', 'R', 'S', 'T').map { inputToKey(inputs, it) }
    val row3 = listOf('U', 'V', 'W').map { inputToKey(inputs, it) }
    val row4 = listOf('X', 'Y', 'Z').map { inputToKey(inputs, it) }

    return Keyboard("letter2", listOf(
            Row(row1),
            Row(row2),
            Row(listOf(BackKey, row3[0], row3[1], row3[2])),
            Row(listOf(ModeKey, row4[0], row4[1], row4[2]))
    ))
}

fun inputToKey(inputs: List<Input>, char: Char): Key {
    val inputAsChars = inputs.firstOrNull { it.charToDisplay == char }

    return inputAsChars?.let { InputKey(it) } ?: DisabledKey(char)
}
