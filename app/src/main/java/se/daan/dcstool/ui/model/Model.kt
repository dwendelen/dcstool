package se.daan.dcstool.ui.model;

import android.arch.lifecycle.ViewModel
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import se.daan.dcstool.model.*
import se.daan.dcstool.model.parser.Parser
import se.daan.dcstool.model.parser.ParserState
import java.util.*

class Model : ViewModel() {
    private var stack: Deque<ParserState> = LinkedList()

    val lastState: Subject<ParserState> = BehaviorSubject.create()

    init {
        pushState(Parser.newState())
    }

    val parserState: ParserState get() {
        return stack.peek()
    }

    fun popState() {
        stack.remove()
        lastState.onNext(parserState)
    }

    fun pushState(state: ParserState) {
        stack.addFirst(state)
        lastState.onNext(parserState)
    }

    fun canPop(): Boolean {
        return stack.size > 1
    }

    val addedFavorites: PublishSubject<Favorite> = PublishSubject.create()

    val favorites: MutableList<Favorite> = arrayListOf()
    var favoriteFactory: CoordinateFactory<*> = coordinateSystems[defaultCoordinateSystemIdx].factory

    fun saveCoordinate(name: CharSequence) {
        parserState.coordinate?.let {
            val favorite = Favorite(name, it.toLaLoDegree())
            favorites.add(favorite)
            addedFavorites.onNext(favorite)

            stack.retainAll(listOf(stack.last))
            lastState.onNext(parserState)
        }
    }


}

const val defaultCoordinateSystemIdx = 5
val coordinateSystems = listOf(
        CoordinateSystem("AJS-37", LaLoSecondFactory),
        CoordinateSystem("M-2000C", LaLoMinuteFactory),
        CoordinateSystem("F/A-18C in", LaLoSecondFactory),
        CoordinateSystem("F/A-18C out", LaLoMinuteFactory),
        CoordinateSystem("A-10C LaLo", LaLoMinuteFactory),
        CoordinateSystem("A-10C MGRS", MGRSFactory),
        CoordinateSystem("La Lo degrees", LaLoDegreeFactory),
        CoordinateSystem("La Lo minutes", LaLoMinuteFactory),
        CoordinateSystem("La Lo seconds", LaLoSecondFactory),
        CoordinateSystem("UTM", UTMFactory),
        CoordinateSystem("MGRS", MGRSFactory)
)

data class CoordinateSystem(val name: CharSequence, val factory: CoordinateFactory<*>)
data class Favorite(val name: CharSequence, val coordinate: LaLoDegree)