package se.daan.dcstool.ui.model;

import android.arch.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject
import se.daan.dcstool.model.*

class Model : ViewModel() {
    var input: CharSequence = ""
    var coordinate: LaLoDegree? = null
    val spinnerIdx: Array<Int> = arrayOf(0, 0, 0)

    val addedFavorites: PublishSubject<Favorite> = PublishSubject.create()

    val favorites: MutableList<Favorite> = arrayListOf()

    fun saveCoordinate(name: CharSequence) {
        coordinate?.let {
            val favorite = Favorite(name, it)
            favorites.add(favorite)
            addedFavorites.onNext(favorite)
        }
    }
}

val coordinateSystems = listOf(
        CoordinateSystem("La Lo degrees", LaLoDegreeFactory),
        CoordinateSystem("La Lo minutes", LaLoMinuteFactory),
        CoordinateSystem("La Lo seconds", LaLoSecondFactory),
        CoordinateSystem("UTM", UTMFactory),
        CoordinateSystem("MGRS", MGRSFactory),
        CoordinateSystem("AJS-37", LaLoSecondFactory),
        CoordinateSystem("M-2000C", LaLoMinuteFactory),
        CoordinateSystem("F/A-18C", LaLoMinuteFactory),
        CoordinateSystem("A-10C LaLo", LaLoMinuteFactory),
        CoordinateSystem("A-10C MGRS", MGRSFactory)
)

data class CoordinateSystem(val name: CharSequence, val factory: CoordinateFactory<*>)
data class Favorite(val name: CharSequence, val coordinate: LaLoDegree)