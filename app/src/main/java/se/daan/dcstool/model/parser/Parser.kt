package se.daan.dcstool.model.parser

import se.daan.dcstool.model.Coordinate
import se.daan.dcstool.model.parser.lalodegree.newLaLoDegreeFormat
import se.daan.dcstool.model.parser.lalominute.newLaLoMinuteFormat
import se.daan.dcstool.model.parser.lalosecond.newLaLoSecondFormat
import se.daan.dcstool.model.parser.mgrs.newMGRSFormat

class Parser {
    fun newState(): ParserState {
        val formats = listOf(
                newLaLoDegreeFormat() as Piece<Input, *>,
                newLaLoMinuteFormat() as Piece<Input, *>,
                newLaLoSecondFormat() as Piece<Input, *>,
                newMGRSFormat() as Piece<Input, *>
        )
        return ParserState(formats, "")
    }

    fun parseChars(chars: CharSequence): Coordinate? {
        var state = newState()

        chars.forEach {
            val nextState = oneIteration(state, it)

            if(nextState == null) {
                return null
            } else {
                state = nextState
            }
        }

        return state.coordinate
    }

    private fun oneIteration(state: ParserState, char: Char): ParserState? {
        val input = state.inputs.find {
            char == it.charToDisplay
        }

        return input?.let {
            state.handle(it)
        }
    }
}

data class ParserState(
        val validFormats: List<Piece<Input, *>>,
        val string: String
) {
    val inputs: Collection<Input>
        get() {
            return validFormats
                    .flatMap { it.inputs }
                    .toSet()
        }

    fun handle(input: Input): ParserState {
        val newStates = validFormats
                .flatMap {
                    if (it.inputs.contains(input)) {
                        setOf(it.handle(input) as Piece<Input, *>)
                    } else {
                        emptySet()
                    }
                }

        return ParserState(newStates, string + input.charToDisplay)
    }

    fun print(): CharSequence {
        return if (validFormats.size == 1) {
            validFormats[0].print()
        } else {
            "${string}_ (${validFormats.size})"
        }
    }

    val coordinate: Coordinate?
        get() =
            if (validFormats.size == 1) {
                val format = validFormats[0]

                if (format is FinalFormat) {
                    format.coordinate
                } else {
                    null
                }
            } else {
                null
            }
}
