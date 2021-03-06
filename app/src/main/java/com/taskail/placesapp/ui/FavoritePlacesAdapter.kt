package com.taskail.placesapp.ui

import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.taskail.placesapp.R
import com.taskail.placesapp.data.models.FavoritePlace
import com.taskail.placesapp.ui.FavoritePlacesAdapter.ItemsViewHolder
import kotlinx.android.synthetic.main.item_place.view.*

/**
 *Created by ed on 4/12/18.
 */

class FavoritePlacesAdapter(favorites: List<FavoritePlace>,
                            private val getDistanceString: (LatLng) -> String,
                            private val openResult: (FavoritePlace) -> Unit) :
        Adapter<ItemsViewHolder>() {

    var favorites: List<FavoritePlace> = favorites

    set(value) {
        field = value
        notifyDataSetChanged()
    }

    class ItemsViewHolder(itemView: View) : ViewHolder(itemView) {

        fun setItem(favorite: FavoritePlace,
                    getDistanceString: (LatLng) -> String,
                    openResult: (FavoritePlace) -> Unit) {

            with(itemView) {
                with(favorite) {
                    placeName.text = name

                    // users current location is  not available right away.
                    //distanceFrom.text = getDistanceString(LatLng(lat, lng))

                    if (icon.isNotBlank()) {
                        Glide.with(itemView).load(icon).into(circularImageView)
                    }

                    setOnClickListener {
                        openResult(this)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_place, parent, false)

        return ItemsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        holder.setItem(favorites[position],
                getDistanceString,
                openResult)
    }

    override fun getItemCount(): Int {
        return favorites.size
    }
}