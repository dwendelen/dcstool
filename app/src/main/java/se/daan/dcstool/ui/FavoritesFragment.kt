package se.daan.dcstool.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import io.reactivex.disposables.Disposable
import se.daan.dcstool.ui.model.Model

class FavoritesFragment : Fragment() {
    private var subscriptions: List<Disposable> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val model: Model = ViewModelProviders.of(activity!!).get(Model::class.java)

        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        val spinner: Spinner = view.findViewById(R.id.fav_spinner)
        val listView: RecyclerView = view.findViewById(R.id.fav_list)

        listView.setHasFixedSize(false)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.adapter = FavoriteListAdapter(model)


        val selectedCoordinateSystem = CoordinateSystemDropdownBuilder.build(view.context, spinner)
        val coordinateSystemDisposable = selectedCoordinateSystem.subscribe {
            model.favoriteFactory = it
            listView.adapter?.notifyDataSetChanged()
        }

        val favoriteSubscription = model.addedFavorites
                .subscribe {
                    listView.adapter?.notifyDataSetChanged()
                }

        subscriptions = listOf(favoriteSubscription, coordinateSystemDisposable)

        return view
    }

    override fun onDestroyView() {
        subscriptions.forEach(Disposable::dispose)
        subscriptions = emptyList()
        super.onDestroyView()
    }
}
