package se.daan.dcstool.ui
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import se.daan.dcstool.ui.model.Model

class FavoriteListAdapter(val model: Model): Adapter<FavoriteListAdapter.FavoriteViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.fav_list_item, parent, false)

        return FavoriteViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return model.favorites.size
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favorite = model.favorites[position]
        val factory = model.favoriteFactory

        val coordinate = factory.fromLaLoDegree(favorite.coordinate)

        val nameOutput: TextView = holder.view.findViewById(R.id.fav_list_item_name)
        val coordinateOutput: TextView = holder.view.findViewById(R.id.fav_list_item_coordinate)

        nameOutput.text = favorite.name
        coordinateOutput.text = coordinate.print()
    }

    data class FavoriteViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}