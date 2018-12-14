package se.daan.dcstool.ui.model;

import android.arch.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject
import se.daan.dcstool.model.*
import se.daan.dcstool.model.parser.Parser
import se.daan.dcstool.model.parser.ParserState
import java.util.*

class Model : ViewModel() {
    var stack: Deque<ParserState> = LinkedList<ParserState>(listOf(Parser.newState()))

    val parserState: ParserState get() {
        return stack.peek()
    }

    val addedFavorites: PublishSubject<Favorite> = PublishSubject.create()

    val favorites: MutableList<Favorite> = arrayListOf()
    var favoriteFactory: CoordinateFactory<*> = coordinateSystems[defaultCoordinateSystemIdx].factory

    fun saveCoordinate(name: CharSequence) {
        parserState.coordinate?.let {
            val favorite = Favorite(name, it.toLaLoDegree())
            favorites.add(favorite)
            addedFavorites.onNext(favorite)
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