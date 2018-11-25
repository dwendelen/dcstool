package se.daan.dcstool.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import io.reactivex.disposables.Disposable
import se.daan.dcstool.ui.model.Favorite
import se.daan.dcstool.ui.model.Model

class FavoritesFragment : Fragment() {
    private var subscriptions: List<Disposable> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        val model: Model = ViewModelProviders.of(activity!!).get(Model::class.java)

        val spinner: Spinner = view.findViewById(R.id.fav_spinner)

        CoordinateSystemDropdownBuilder.build(view.context, spinner, 0) //TODO index

        model.favorites.forEach(this::addFavorite)
        val favoriteSubscription = model.addedFavorites
                .subscribe(this::addFavorite)

        subscriptions = listOf(favoriteSubscription)

        return view
    }

    private fun addFavorite(favorite: Favorite) {

    }

    override fun onDestroyView() {
        subscriptions.forEach(Disposable::dispose)
        subscriptions = emptyList()
        super.onDestroyView()
    }
}
