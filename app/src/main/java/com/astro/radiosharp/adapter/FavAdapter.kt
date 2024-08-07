package com.astro.radiosharp.adapter

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.astro.radiosharp.MainViewModel
import com.bumptech.glide.Glide
import com.astro.radiosharp.R
import com.astro.radiosharp.model.FavClass
import com.astro.radiosharp.ui.FavFragmentDirections

class FavAdapter(val context: Context,
                 val defaultText: (text: TextView) -> Unit,
                 val viewModel: MainViewModel) :
    RecyclerView.Adapter<FavAdapter.ItemViewHodler>() {


    private var dataset = listOf<FavClass>()


    fun submitlist(radioList: List<FavClass>) {
        dataset = radioList
        notifyDataSetChanged()
    }

    class ItemViewHodler(view: View) : RecyclerView.ViewHolder(view) {

        val radioName = view.findViewById<TextView>(R.id.radio_name_detail)
        val genreName = view.findViewById<TextView>(R.id.genreText)
        val countryName = view.findViewById<TextView>(R.id.countryText)
        val iconImage = view.findViewById<ImageView>(R.id.icon_image_detail)
        val radioCardView = view.findViewById<CardView>(R.id.radioCardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHodler {
        val itemLayout = LayoutInflater.from(parent.context) // Es ist eine Komponente die dazu verwendet wird eine Brücke zwischen
            // XML Layout und Kotlin Code zu machen. Zusammengefasst bereitet der Layoutinflater das Layout vor damit XML Dateien
            // in View Objekte umgewandelt werden können.
            .inflate(R.layout.radio_item, parent, false)
        return ItemViewHodler(itemLayout)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: ItemViewHodler, position: Int) {

        val radioData: FavClass = dataset[position]

        holder.radioName.text = viewModel.limitTextTo50Chars(radioData.name)
        holder.countryName.text = radioData.country
        holder.genreName.text = viewModel.limitTextTo50Chars(radioData.tags)
        holder.radioCardView.setOnClickListener {
            holder.itemView.findNavController()
                .navigate(FavFragmentDirections.actionFavFragmentToDetailFragment(radioData.stationuuid,
                openingFav = true))
        }

        val gif = ContextCompat.getDrawable(context, R.drawable.giphy4) as AnimatedImageDrawable

        gif.start()
        Glide.with(context).load(radioData.favicon).placeholder(gif).into(holder.iconImage)
        defaultText(holder.countryName)
        defaultText(holder.genreName)
    }

    override fun getItemCount(): Int { // Hier wird Intern die Anzahl der aktuellen Listenelemente
        // der RecyclerView gespeist. Damit die RecyclerView weiss wieviel Elemente sie insgesamt beinhaltet.
        return dataset.size
    }
}