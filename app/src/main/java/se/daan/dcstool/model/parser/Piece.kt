package se.daan.dcstool.model.parser

import se.daan.dcstool.model.Hemisphere


interface Piece<I : Input, out O> {
    val inputs: Collection<I>
    fun handle(input: I): O
    fun print(): CharSequence
}


data class HemiPiece0(
        val hemi1: Hemisphere,
        val hemi2: Hemisphere
) : Piece<HemiInput, Hemisphere> {
    override val inputs = listOf(
            HemiInput(hemi1),
            HemiInput(hemi2)
    )

    override fun handle(input: HemiInput): Hemisphere {
        return input.hemisphere
    }

    override fun print(): String {
        return "_"
    }
}

data class IntRange(
        val min: String,
        val max: String
) {
    val inputs: Collection<Char>
        get() {
            if (min.isEmpty()) {
                return emptySet()
            }

            //TODO with smaller size

            return (min[0]..max[0])
                    .map { it }
        }

    fun handle(c: Char): IntRange {
        //TODO with smaller size

        if (c == min[0]) {
            return IntRange(min.substring(1), repeat9())
        } else if (c == max[0]) {
            return IntRange(repeat0(), max.substring(1))
        } else {
            return IntRange(repeat0(), repeat9())
        }
    }

    private fun repeat9() = "9".repeat(min.length - 1)

    private fun repeat0() = "0".repeat(min.length - 1)

    val isDone: Boolean get() {
        return min.isEmpty()
    }
}

sealed class IntHelper
data class Int0(
        val range: IntRange,
        val string: String
):IntHelper(), Piece<DigitInput, IntHelper> {
    override val inputs: Collection<DigitInput>
        get() {
            return range.inputs.map { DigitInput(it) }
        }

    override fun handle(input: DigitInput): IntHelper {
        val char = input.char
        val newRange = range.handle(char)
        val newString = string + char

        return if (newRange.isDone) {
            IntDone(newString.toInt())
        } else {
            Int0(newRange, newString)
        }
    }

    override fun print(): String {
        return "${string}_"
    }
}

data class IntDone(val int: Int) : IntHelper()

sealed class DecimalHelper<O>: Piece<DigitInput, O> {
    abstract val chars: Collection<Char>
    abstract fun handleChar(char: Char): O

    override val inputs: Collection<DigitInput>
        get() = chars.map { DigitInput(it) }

    override fun handle(input: DigitInput): O {
        return handleChar(input.char)
    }
}

data class Decimal0(
        val range: IntRange,
        val string: String
) : DecimalHelper<DecimalHelper<*>>() {
    override val chars: Collection<Char>
        get() {
            return range.inputs
        }

    override fun handleChar(char: Char): DecimalHelper<*> {
        val newRange = range.handle(char)
        val newString = string + char

        return if (newRange.isDone) {
            Decimal1(newString)
        } else {
            Decimal0(newRange, newString)
        }
    }

    override fun print(): String {
        return "${string}_"
    }
}

abstract class DecimalDone: DecimalHelper<Decimal2>() {
    abstract val double: Double
}

data class Decimal1(
        val string: String
) : DecimalDone() {
    override val chars = listOf('.')

    override fun handleChar(char: Char): Decimal2 {
        return Decimal2(string + char)
    }

    override val double: Double get() {
        return string.toDouble()
    }

    override fun print(): String {
        return "${string}_"
    }
}

data class Decimal2(
        val string: String
) : DecimalDone() {
    override val chars = ('0'..'9').toList()

    override fun handleChar(char: Char): Decimal2 {
        return Decimal2(string + char)
    }

    override val double: Double get() {
        return string.toDouble()
    }

    override fun print(): String {
        return "${string}_"
    }
}


abstract class DelegatingPiece<I : Input, P, O> : Piece<I, O> {
    protected abstract val currentPiece: Piece<I, P>

    protected abstract fun handleCurrent(newPiece: P): O

    override fun handle(input: I): O {
        return handleCurrent(currentPiece.handle(input))
    }

    override val inputs: Collection<I>
        get() {
            return currentPiece.inputs
        }
}

abstract class Delegating2Piece<I: Input, F, IC : I, IN : I, PC, PN> : Piece<I, F> {
    protected abstract val currentPiece: Piece<IC, PC>
    protected abstract val nextPiece: Piece<IN, PN>

    protected abstract fun handleCurrent(newPiece: PC): F
    protected abstract fun handleNext(newPiece: PN): F

    override fun handle(input: I): F {
        return when {
            currentPiece.inputs.contains(input) -> handleCurrent(currentPiece.handle(input as IC))
            nextPiece.inputs.contains(input) -> handleNext(nextPiece.handle(input as IN))
            else -> throw IllegalStateException("Bad input")
        }
    }

    override val inputs: Collection<I>
        get() {
            val currentInputs = currentPiece.inputs
            val nextInputs = nextPiece.inputs
            return currentInputs + nextInputs
        }
}