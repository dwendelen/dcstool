package daan.se.dcstool.model.parser

import daan.se.dcstool.model.*


class Parser {
    fun getCoordinateParser(): Thing<Coordinate> {
        val N = CharThing('N') { Hemisphere.NORTH }
        val S = CharThing('S') { Hemisphere.SOUTH }

        val space = CharThing(' ') { null }
        val maybeSpace = MaybeThing(space, setOf(null))

        val _60 = Digit(6).concat(Digit(0)) { a, b -> 10 * a + b }
        val _6 = Digit(6)

        val zone =
                DigitRange(1, 9)
                        .or(DigitRange(1, 5).concat(DigitRange(0, 9)) { a, b -> 10 * a + b })
                        .or(_6)
                        .or(_60)

        val mgrs =
                concatThing9(
                        zone,
                        EnumValues(LatitudeBand.values()),
                        maybeSpace,
                        EnumValues(ColumnLetter.values()),
                        EnumValues(RowLetter.values()),
                        maybeSpace,
                        DigitRange(0, 9).star(setOf(0)) { a, b -> 10 * a + b },
                        maybeSpace,
                        DigitRange(0, 9).star(setOf(0)) { a, b -> 10 * a + b }
                ) { z, l, _, c, r, _, n, _, e -> MGRS(z, l, c, r, n.toDouble(), e.toDouble()) as Coordinate }

        return mgrs
    }

    fun <A, B, C, D, E, F, G, H, I, R> concatThing9(
            a: Thing<A>,
            b: Thing<B>,
            c: Thing<C>,
            d: Thing<D>,
            e: Thing<E>,
            f: Thing<F>,
            g: Thing<G>,
            h: Thing<H>,
            i: Thing<I>,
            fn: (A, B, C, D, E, F, G, H, I) -> R
    ): Thing<R> {

        val things = ConcatThingListTerminator(i)
                .prepend(h)
                .prepend(g)
                .prepend(f)
                .prepend(e)
                .prepend(d)
                .prepend(c)
                .prepend(b)
                .prepend(a)

        return ConcatThing(things) {
            val aa = it.item
            val bb = it.tail.item
            val cc = it.tail.tail.item
            val dd = it.tail.tail.tail.item
            val ee = it.tail.tail.tail.tail.item
            val ff = it.tail.tail.tail.tail.tail.item
            val gg = it.tail.tail.tail.tail.tail.tail.item
            val hh = it.tail.tail.tail.tail.tail.tail.tail.item
            val ii = it.tail.tail.tail.tail.tail.tail.tail.tail.item

            fn(aa, bb, cc, dd, ee, ff, gg, hh, ii)
        }


    }

    fun Digit(digit: Int): Thing<Int> {
        return CharThing(digit.toString()[0], { digit })
    }

    fun DigitRange(from: Int, to: Int): Thing<Int> {
        val initial = Digit(from)

        return (from + 1..to)
                .fold(initial) { acc, d ->
                    acc.or(Digit(d))
                }
    }

    fun <E : Enum<E>> EnumValue(enumValue: E): Thing<E> {
        return CharThing(enumValue.name[0], { enumValue })
    }

    fun <E : Enum<E>> EnumValues(enumValues: Array<E>): Thing<E> {
        val first = EnumValue(enumValues[0])

        return enumValues.sliceArray(1..enumValues.size - 1)
                .fold(first) { acc, e ->
                    acc.or(EnumValue(e))
                }
    }
}

sealed class Thing<T> {
    fun or(other: Thing<T>): Thing<T> {
        return OrThing(listOf(this, other))
    }

    fun <O, R> concat(second: Thing<O>, f: (T, O) -> R): Thing<R> {
        val list = ConcatThingNode(this,
                ConcatThingListTerminator(second)
        )
        return ConcatThing(list) { f(it.item, it.tail.item) }
    }

    fun repeat(n: Int, f: (T, T) -> T): Thing<T> {
        var acc = this
        for (i in 2..n) {
            acc = acc.concat(this, f)
        }

        return acc
    }

    fun star(initial: Set<T>, f: (T, T) -> T): Thing<T> {
        return StarThing(this, initial, f)
    }

    abstract fun getParseState(): ParseState<T>

    open fun getInitialValue(): Set<T>? {
        return null
    }
}

interface ParseState<T> {
    fun getAcceptedChars(): Set<Char>
    fun onChar(char: Char): ParseResult<T>
    fun canSkip(): Boolean
}

data class ParseResult<out T>(val result: Set<T>, val done: Boolean) {
    fun <O> map(fn: (T) -> O): ParseResult<O> {
        return ParseResult(result.map(fn).toSet(), done)
    }
}

data class CharThing<T>(
        val char_: Char,
        val f: () -> T
) : Thing<T>() {
    override fun getParseState(): ParseState<T> {
        return object : ParseState<T> {
            override fun getAcceptedChars(): Set<Char> {
                return setOf(char_)
            }

            override fun onChar(char: Char): ParseResult<T> {
                return ParseResult(setOf(f()), true)
            }

            override fun canSkip(): Boolean {
                return false
            }
        }
    }
}

data class MaybeThing<E>(
        val element: Thing<E>,
        val initial: Set<E>
) : Thing<E>() {
    override fun getParseState(): ParseState<E> {
        return object : ParseState<E> {
            val state = element.getParseState()

            override fun getAcceptedChars(): Set<Char> {
                return state.getAcceptedChars()
            }

            override fun onChar(char: Char): ParseResult<E> {
                return state.onChar(char)
            }

            override fun canSkip(): Boolean {
                return true
            }
        }
    }

    override fun getInitialValue(): Set<E>? {
        return initial
    }
}

data class OrThing<T>(
        val things: List<Thing<T>>
) : Thing<T>() {
    override fun getParseState(): ParseState<T> {
        return object : ParseState<T> {
            var states = things.map { OrState(it.getParseState(), false) }

            override fun getAcceptedChars(): Set<Char> {
                return states.fold(setOf()) { s1, state ->
                    s1.union(state.getAcceptedChars())
                }
            }

            override fun onChar(char: Char): ParseResult<T> {
                val fold = states.fold(setOf<T>()) { data, state ->
                    data.union(state.onChar(char))
                }

                return ParseResult(fold, states.all { it.done })
            }

            override fun canSkip(): Boolean {
                return states.any { it.canSkip() }
            }
        }
    }
}

data class OrState<T>(val state: ParseState<T>, var done: Boolean) {
    fun getAcceptedChars(): Set<Char> {
        if (done) {
            return emptySet()
        } else {
            return state.getAcceptedChars()
        }
    }

    fun onChar(char: Char): Set<T> {
        if (done) {
            return emptySet()
        } else {
            if (!state.getAcceptedChars().contains(char)) {
                done = true
                return emptySet()
            } else {
                val result = state.onChar(char)
                done = result.done

                return result.result
            }
        }
    }

    fun canSkip(): Boolean {
        return done || state.canSkip()
    }
}

data class ConcatThing<R, RL : ResultList>(
        val things: ConcatThingList<RL>,
        val fn: (RL) -> R
) : Thing<R>() {
    override fun getParseState(): ParseState<R> {
        return object : ParseState<R> {
            val list = things.createList()

            override fun getAcceptedChars(): Set<Char> {
                return list.acceptedChars()
            }

            override fun onChar(char: Char): ParseResult<R> {
                return list.onChar(char).map { fn(it) }
            }

            override fun canSkip(): Boolean {
                return list.canSkip()
            }
        }
    }
}

sealed class ConcatThingList<RL : ResultList> {
    abstract fun createList(): ConcatList<RL>

    fun <O> prepend(other: Thing<O>): ConcatThingNode<O, RL> {
        return ConcatThingNode(other, this)
    }
}

data class ConcatThingListTerminator<R>(val thing: Thing<R>) : ConcatThingList<ResultTerminal<R>>() {
    override fun createList(): ConcatListTerminator<R> {
        return ConcatListTerminator(thing.getParseState())
    }
}

data class ConcatThingNode<E, RL : ResultList>(
        val thing: Thing<E>,
        val tail: ConcatThingList<RL>
) : ConcatThingList<ResultNode<E, RL>>() {
    override fun createList(): ConcatList<ResultNode<E, RL>> {
        val initial = thing.getInitialValue()

        val initialLastValue =
                if (initial != null)
                    initial
                else
                    emptySet()

        return ConcatListNode(thing.getParseState(), initialLastValue, false, tail.createList())
    }
}

sealed class ResultList
data class ResultTerminal<R>(val item: R) : ResultList()
data class ResultNode<R, L : ResultList>(val item: R, val tail: L) : ResultList()

sealed class ConcatList<RL : ResultList> {
    abstract fun acceptedChars(): Set<Char>
    abstract fun onChar(char: Char): ParseResult<RL>
    abstract fun canSkip(): Boolean
}

class ConcatListTerminator<E>(val state: ParseState<E>) : ConcatList<ResultTerminal<E>>() {
    override fun canSkip(): Boolean {
        return state.canSkip()
    }

    override fun acceptedChars(): Set<Char> {
        return state.getAcceptedChars()
    }

    override fun onChar(char: Char): ParseResult<ResultTerminal<E>> {
        return state
                .onChar(char)
                .map { ResultTerminal(it) }
    }
}

class ConcatListNode<E, LR : ResultList>(
        val state: ParseState<E>,
        var lastResult: Set<E>,
        var done: Boolean,
        val tail: ConcatList<LR>
) : ConcatList<ResultNode<E, LR>>() {
    override fun acceptedChars(): Set<Char> {
        if (done) {
            return tail.acceptedChars()
        } else {
            val acceptedChars = state.getAcceptedChars()

            if (state.canSkip()) {
                val otherChars = tail.acceptedChars()
                return otherChars.union(acceptedChars)
            } else {
                return acceptedChars
            }
        }
    }

    override fun onChar(char: Char): ParseResult<ResultNode<E, LR>> {
        if (done) {
            return onCharOnTail(char)
        } else {
            if (!state.getAcceptedChars().contains(char)) {
                done = true

                return onCharOnTail(char)
            } else {
                val result = state.onChar(char)
                lastResult = result.result
                if (result.done) {
                    done = true
                }

                return ParseResult(emptySet(), false)
            }
        }
    }

    private fun onCharOnTail(char: Char): ParseResult<ResultNode<E, LR>> {
        val tailResult = tail.onChar(char)

        val result = cross(lastResult, tailResult.result)
                .map { pair -> ResultNode(pair.first, pair.second) }
                .toSet()

        return ParseResult(result, tailResult.done)
    }

    override fun canSkip(): Boolean {
        if (state.canSkip()) {
            return tail.canSkip()
        } else {
            return false
        }
    }
}

fun <A, B> cross(first: Set<A>, second: Set<B>): List<Pair<A, B>> {
    return first.flatMap { a -> second.map { b -> Pair(a, b) } }
}

data class StarThing<T>(
        val element: Thing<T>,
        val initial: Set<T>,
        val f: (T, T) -> T
) : Thing<T>() {
    override fun getParseState(): ParseState<T> {
        return object : ParseState<T> {
            var state = element.getParseState()
            var result = initial

            override fun getAcceptedChars(): Set<Char> {
                return state.getAcceptedChars()
            }

            override fun onChar(char: Char): ParseResult<T> {
                val fromState = state.onChar(char)
                result = cross(result, fromState.result)
                        .map { pair -> f(pair.first, pair.second) }
                        .toSet()

                if (fromState.done) {
                    state = element.getParseState()
                }

                return ParseResult(result, false)
            }

            override fun canSkip(): Boolean {
                return true
            }
        }
    }

    override fun getInitialValue(): Set<T>? {
        return initial
    }
}
