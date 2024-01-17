package com.astro.radiosharp.adapter

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.astro.radiosharp.R
import com.astro.radiosharp.model.RadioClass
import com.astro.radiosharp.ui.HomeFragmentDirections

class RadioAdapter(val context: Context, val defaultText: (text: TextView) -> Unit) :
    RecyclerView.Adapter<RadioAdapter.ItemViewHodler>() {


    private var dataset = listOf<RadioClass>()


    fun submitlist(radioList: List<RadioClass>) {
        dataset = radioList
        notifyDataSetChanged()
    }

    class ItemViewHodler(view: View) : RecyclerView.ViewHolder(view) {

        val radioName = view.findViewById<TextView>(R.id.radio_name_detail)
        val genreName = view.findViewById<TextView>(R.id.genreText)
        val countryName = view.findViewById<TextView>(R.id.countryText)
        val iconImage = view.findViewById<ImageView>(R.id.icon_image_detail)
        val radioCardView = view.findViewById<CardView>(R.id.radioCardView) // Für die Navigation um die Station UUID zu
    // übergeben und die Unterscheidung ob es aus der FavoritenClass kommt oder aus der RadioClass (OpeningFav).

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHodler {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.radio_item, parent, false)// Es ist eine Komponente die dazu verwendet wird eine Brücke zwischen
        // XML Layout und Kotlin Code zu machen. Zusammengefasst bereitet der Layoutinflater das Layout vor damit XML Dateien in View Objekte umgewandelt werden können.
        return ItemViewHodler(itemLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHodler, position: Int) {

        val radioData: RadioClass = dataset[position]

        holder.radioName.text = radioData.name
        holder.countryName.text = radioData.country
        holder.genreName.text = radioData.tags
        holder.radioCardView.setOnClickListener {
            holder.itemView.findNavController().navigate(
                HomeFragmentDirections
                    .actionHomeFragmentToDetailFragment(
                        radioData.stationuuid,
                        openingFav = false
                    )
            )
        }

        val gif = ContextCompat.getDrawable(context, R.drawable.giphy4) as AnimatedImageDrawable

        gif.start()
        Glide.with(context).load(radioData.favicon).placeholder(gif).into(holder.iconImage)
        defaultText(holder.countryName)
        defaultText(holder.genreName)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}