package com.example.citypulse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.citypulse.R
import com.example.citypulse.model.Place

class PlaceAdapter(
    private var places: List<Place>,
    private val onItemClick: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    // --- AJOUTE CETTE MÉTHODE ICI ---
    fun getPlaceAt(position: Int): Place {
        return places[position]
    }

    fun updateData(newPlaces: List<Place>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.bind(place, onItemClick)
    }

    override fun getItemCount(): Int = places.size

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.placeName)

        fun bind(place: Place, onClick: (Place) -> Unit) {
            nameText.text = place.name
            itemView.setOnClickListener { onClick(place) }
        }
    }
}