package se.daan.dcstool.model.parser

import se.daan.dcstool.model.*


class Parser {
    val composeNumber = { a: Int, b: Int -> 10 * a + b }
    val composeNumber3 = { a: Int, b: Int, c: Int -> 100 * a + 10 * b + c }

    fun parseChars(input: CharSequence): Set<Coordinate> {
        val state = getCoordinateParser().getParseState()
        var lastResult = ParseResult(emptySet<Coordinate>(), false)

        println(state.getAcceptedChars()) //TODO REMOVE
        input.forEach {
            if (lastResult.done) {
                return emptySet()
            }

            println(it) //TODO REMOVE
            if (!state.getAcceptedChars().contains(it)) {
                return emptySet()
            }

            lastResult = state.onChar(it)
            println(state.getAcceptedChars()) //TODO REMOVE
        }

        return lastResult.result
    }

    fun getCoordinateParser(): Thing<Coordinate> {
        val N = CharThing('N', Hemisphere.NORTH)
        val S = CharThing('S', Hemisphere.SOUTH)
        val E = CharThing('E', Hemisphere.EAST)
        val W = CharThing('W', Hemisphere.WEST)
        val ns = N.or(S)
        val ew = E.or(W)

        val degree = CharThing('°', null)
        val minute = CharThing('\'', null)
        val second = CharThing('"', null)

        val space = CharThing(' ', null)
        val maybeSpace = MaybeThing(space, null)

        val int0_9 = DigitRange(0, 9)
        val int0_59 =
                int0_9.or(DigitRange(0, 5).concat(int0_9, composeNumber))
        val int0_179 =
                OrThing(setOf(
                        concatThing3(Digit(0), int0_9, int0_9, composeNumber3),
                        concatThing3(Digit(1), DigitRange(0, 7), int0_9, composeNumber3),
                        int0_9.concat(int0_9, composeNumber),
                        int0_9
                ))

        val integer = int0_9.star(0, composeNumber)

        fun toDecimal(int: Thing<Int>): Thing<Double> {
            val afterComma =
                    CharThing('.', null)
                            .concat(DigitRangeAsString(0, 9).star("0.") { a, b -> a + b }) { _, a ->
                                a.toDouble()
                            }
            return int.concat(MaybeThing(afterComma, 0.0)) { a, b -> a + b }
        }

        data class LaLoDegPart(val h: Hemisphere, val d: Double)

        fun laLoDegPart(hemi: Thing<Hemisphere>, degreeRange: Thing<Int>): Thing<LaLoDegPart> {
            return concatThing4(
                    hemi,
                    maybeSpace,
                    toDecimal(degreeRange),
                    MaybeThing(degree, null)
            ) { h, _, d, _ ->
                LaLoDegPart(h, d)
            }
        }

        val laLoDegree = concatThing3(
                laLoDegPart(ns, int0_59),
                maybeSpace,
                laLoDegPart(ew, int0_179)
        ) { lat, _, lon ->
            LaLoDegree(lat.h, lat.d, lon.h, lon.d) as Coordinate
        }


        data class LaLoMinPart(val h: Hemisphere, val d: Int, val m: Double)

        fun laLoMinPart(hemi: Thing<Hemisphere>, degreeRange: Thing<Int>): Thing<LaLoMinPart> {
            return concatThing6(
                    hemi,
                    maybeSpace,
                    degreeRange,
                    degree.or(space),
                    toDecimal(int0_59),
                    MaybeThing(minute, null)
            ) { h, _, d, _, m, _ ->
                LaLoMinPart(h, d, m)
            }
        }

        val laLoMinute = concatThing3(
                laLoMinPart(ns, int0_59),
                maybeSpace,
                laLoMinPart(ew, int0_179)
        ) { lat, _, lon ->
            LaLoMinute(lat.h, lat.d, lat.m, lon.h, lon.d, lon.m) as Coordinate
        }


        data class LaLoSecPart(val h: Hemisphere, val d: Int, val m: Int, val s: Int)

        fun laLoSecPart(hemi: Thing<Hemisphere>, degreeRange: Thing<Int>): Thing<LaLoSecPart> {
            return concatThing8(
                    hemi,
                    maybeSpace,
                    degreeRange,
                    degree.or(space),
                    int0_59,
                    minute.or(space),
                    int0_59,
                    MaybeThing(second, null)
            ) { h, _, d, _, m, _, s, _ ->
                LaLoSecPart(h, d, m, s)
            }
        }

        val laLoSecond = concatThing3(
                laLoSecPart(ns, int0_59),
                maybeSpace,
                laLoSecPart(ew, int0_179)
        ) { lat, _, lon ->
            LaLoSecond(lat.h, lat.d, lat.m, lat.s.toDouble(), lon.h, lon.d, lon.m, lon.s.toDouble()) as Coordinate
        }


        val zone =
                DigitRange(1, 9)
                        .or(DigitRange(1, 5).concat(int0_9, composeNumber))
                        .or(Digit(6).concat(Digit(0), composeNumber))

        val utm =
                concatThing8(
                        ns,
                        maybeSpace,
                        zone,
                        MaybeThing(EnumValues(LatitudeBand.values()) as Thing<LatitudeBand?>, null as LatitudeBand?),
                        maybeSpace,
                        integer,
                        space,
                        integer
                ) { h, _, z, l, _, e, _, n ->
                    UTM(h, z, l, e.toDouble(), n.toDouble()) as Coordinate
                }


        val mgrs =
                concatThing9(
                        zone,
                        EnumValues(LatitudeBand.values()),
                        maybeSpace,
                        EnumValues(ColumnLetter.values()),
                        EnumValues(RowLetter.values()),
                        maybeSpace,
                        integer,
                        space,
                        integer
                ) { z, l, _, c, r, _, n, _, e -> MGRS(z, l, c, r, n.toDouble() + 0.5, e.toDouble() + 0.5) as Coordinate }

        val result = OrThing(setOf(
                laLoDegree,
                laLoMinute,
                laLoSecond,
                utm,
                mgrs
        ))

        return result.optimise()
    }


    fun <A, B, C, R> concatThing3(
            a: Thing<A>,
            b: Thing<B>,
            c: Thing<C>,
            fn: (A, B, C) -> R
    ): Thing<R> {

        val things =
                ConcatThingListTerminator(c)
                        .prepend(b)
                        .prepend(a)

        return ConcatThing(things) {
            val aa = it.item
            val bb = it.tail.item
            val cc = it.tail.tail.item

            fn(aa, bb, cc)
        }
    }

    fun <A, B, C, D, E, F, R> concatThing6(
            a: Thing<A>,
            b: Thing<B>,
            c: Thing<C>,
            d: Thing<D>,
            e: Thing<E>,
            f: Thing<F>,
            fn: (A, B, C, D, E, F) -> R
    ): Thing<R> {

        val things = ConcatThingListTerminator(f)
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

            fn(aa, bb, cc, dd, ee, ff)
        }
    }

    fun <A, B, C, D, R> concatThing4(
            a: Thing<A>,
            b: Thing<B>,
            c: Thing<C>,
            d: Thing<D>,
            fn: (A, B, C, D) -> R
    ): Thing<R> {

        val things = ConcatThingListTerminator(d)
                .prepend(c)
                .prepend(b)
                .prepend(a)

        return ConcatThing(things) {
            val aa = it.item
            val bb = it.tail.item
            val cc = it.tail.tail.item
            val dd = it.tail.tail.tail.item

            fn(aa, bb, cc, dd)
        }
    }

    fun <A, B, C, D, E, F, G, H, R> concatThing8(
            a: Thing<A>,
            b: Thing<B>,
            c: Thing<C>,
            d: Thing<D>,
            e: Thing<E>,
            f: Thing<F>,
            g: Thing<G>,
            h: Thing<H>,
            fn: (A, B, C, D, E, F, G, H) -> R
    ): Thing<R> {

        val things = ConcatThingListTerminator(h)
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

            fn(aa, bb, cc, dd, ee, ff, gg, hh)
        }
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
        return CharThing(digit.toString()[0], digit)
    }

    fun DigitRange(from: Int, to: Int): Thing<Int> {
        val initial = Digit(from)

        return (from + 1..to)
                .fold(initial) { acc, d ->
                    acc.or(Digit(d))
                }
    }

    fun DigitAsString(digit: Int): Thing<String> {
        val asString = digit.toString()

        return CharThing(asString[0], asString)
    }

    fun DigitRangeAsString(from: Int, to: Int): Thing<String> {
        val initial = DigitAsString(from)

        return (from + 1..to)
                .fold(initial) { acc, d ->
                    acc.or(DigitAsString(d))
                }
    }

    fun <E : Enum<E>> EnumValue(enumValue: E): Thing<E> {
        return CharThing(enumValue.name[0], enumValue)
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
        return OrThing(setOf(this, other))
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

    fun star(initial: T, f: (T, T) -> T): Thing<T> {
        return StarThing(this, initial, f)
    }

    fun <O> map(f: (T) -> O): Thing<O> {
        return MapThing(this, f)
    }

    abstract fun getParseState(): ParseState<T>

    open fun getInitialValue(): Set<T>? {
        return null
    }

    abstract fun optimise(): Thing<T>
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
        val char: Char,
        val result: T
) : Thing<T>() {
    override fun getParseState(): ParseState<T> {
        return object : ParseState<T> {
            override fun getAcceptedChars(): Set<Char> {
                return setOf(char)
            }

            override fun onChar(char: Char): ParseResult<T> {
                return ParseResult(setOf(result), true)
            }

            override fun canSkip(): Boolean {
                return false
            }
        }
    }

    override fun optimise(): Thing<T> {
        return this
    }
}

data class MaybeThing<E>(
        val element: Thing<E>,
        val initial: E
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
        return setOf(initial)
    }

    override fun optimise(): Thing<E> {
        return MaybeThing(element.optimise(), initial)
    }
}

data class MapThing<O, T>(
        val thing: Thing<O>,
        val fn: (O) -> T
) : Thing<T>() {
    override fun getParseState(): ParseState<T> {
        return object : ParseState<T> {
            val state = thing.getParseState()

            override fun getAcceptedChars(): Set<Char> {
                return state.getAcceptedChars()
            }

            override fun onChar(char: Char): ParseResult<T> {
                return state.onChar(char)
                        .map(fn)
            }

            override fun canSkip(): Boolean {
                return state.canSkip()
            }
        }
    }

    override fun optimise(): Thing<T> {
        return MapThing(thing.optimise(), fn)
    }
}

data class OrThing<T>(
        val things: Set<Thing<T>>
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

    override fun optimise(): Thing<T> {
        val optimisedThings = things.map { it.optimise() }

        val newThings = optimisedThings.fold(emptySet()) { newThings: Set<Thing<T>>, thing: Thing<T> ->
            when (thing) {
                is OrThing -> newThings + thing.things
                else -> newThings + thing
            }
        }

        return OrThing(newThings)
    }
}

data class OrState<T>(val state: ParseState<T>, var done: Boolean) {
    fun getAcceptedChars(): Set<Char> {
        return if (done) {
            emptySet()
        } else {
            state.getAcceptedChars()
        }
    }

    fun onChar(char: Char): Set<T> {
        return if (done) {
            emptySet()
        } else {
            if (!state.getAcceptedChars().contains(char)) {
                done = true
                emptySet()
            } else {
                val result = state.onChar(char)
                done = result.done

                result.result
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

    override fun optimise(): Thing<R> {
        return ConcatThing(things.optimise(), fn)
    }
}

sealed class ConcatThingList<RL : ResultList> {
    abstract fun createList(): ConcatList<RL>
    abstract fun optimise(): ConcatThingList<RL>

    fun <O> prepend(other: Thing<O>): ConcatThingNode<O, RL> {
        return ConcatThingNode(other, this)
    }
}

data class ConcatThingListTerminator<R>(val thing: Thing<R>) : ConcatThingList<ResultTerminal<R>>() {
    override fun createList(): ConcatListTerminator<R> {
        return ConcatListTerminator(thing.getParseState(), thing.getInitialValue())
    }

    override fun optimise(): ConcatThingList<ResultTerminal<R>> {
        return ConcatThingListTerminator(thing.optimise())
    }
}

data class ConcatThingNode<E, RL : ResultList>(
        val thing: Thing<E>,
        val tail: ConcatThingList<RL>
) : ConcatThingList<ResultNode<E, RL>>() {
    override fun createList(): ConcatList<ResultNode<E, RL>> {
        val initial = thing.getInitialValue()

        return ConcatListNode(thing.getParseState(), initial, false, tail.createList())
    }

    override fun optimise(): ConcatThingList<ResultNode<E, RL>> {
        return ConcatThingNode(thing.optimise(), tail.optimise())
    }
}

sealed class ResultList
data class ResultTerminal<R>(val item: R) : ResultList()
data class ResultNode<R, L : ResultList>(val item: R, val tail: L) : ResultList()

sealed class ConcatList<RL : ResultList> {
    abstract fun acceptedChars(): Set<Char>
    abstract fun onChar(char: Char): ParseResult<RL>
    abstract fun canSkip(): Boolean
    abstract fun getInitialValue(): Set<RL>?
}

class ConcatListTerminator<E>(val state: ParseState<E>, val initial: Set<E>?) : ConcatList<ResultTerminal<E>>() {
    override fun getInitialValue(): Set<ResultTerminal<E>>? {
        return initial?.map { ResultTerminal(it) }?.toSet()
    }

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
        val initial: Set<E>?,
        var done: Boolean,
        val tail: ConcatList<LR>
) : ConcatList<ResultNode<E, LR>>() {
    var lastResult: Set<E> = initial ?: emptySet()

    override fun getInitialValue(): Set<ResultNode<E, LR>>? {
        val tailInit = tail.getInitialValue()

        return if (initial != null && tailInit != null) {
            cross(initial, tailInit)
                    .map { pair -> ResultNode(pair.first, pair.second) }
                    .toSet()
        } else {
            null
        }
    }

    override fun acceptedChars(): Set<Char> {
        return if (done) {
            tail.acceptedChars()
        } else {
            val acceptedChars = state.getAcceptedChars()

            if (state.canSkip()) {
                val otherChars = tail.acceptedChars()
                otherChars.union(acceptedChars)
            } else {
                acceptedChars
            }
        }
    }

    override fun onChar(char: Char): ParseResult<ResultNode<E, LR>> {
        return if (done) {
            onCharOnTail(char)
        } else {
            if (!state.getAcceptedChars().contains(char)) {
                done = true

                onCharOnTail(char)
            } else {
                val result = state.onChar(char)
                lastResult = result.result
                if (result.done) {
                    done = true
                }

                val tailInit = tail.getInitialValue()

                if (tailInit == null) {
                    ParseResult(emptySet(), false)
                } else {
                    ParseResult(
                            cross(lastResult, tailInit)
                                    .map { pair -> ResultNode(pair.first, pair.second) }
                                    .toSet(),
                            false
                    )
                }
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
        return if (state.canSkip()) {
            tail.canSkip()
        } else {
            false
        }
    }
}

fun <A, B> cross(first: Set<A>, second: Set<B>): List<Pair<A, B>> {
    return first.flatMap { a -> second.map { b -> Pair(a, b) } }
}

data class StarThing<T>(
        val element: Thing<T>,
        val initial: T,
        val f: (T, T) -> T
) : Thing<T>() {
    override fun getParseState(): ParseState<T> {
        return object : ParseState<T> {
            var state = element.getParseState()
            var result = setOf(initial)

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
        return setOf(initial)
    }

    override fun optimise(): Thing<T> {
        return StarThing(element.optimise(), initial, f)
    }
}
