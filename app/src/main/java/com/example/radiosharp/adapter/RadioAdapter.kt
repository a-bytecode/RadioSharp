package com.example.radiosharp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.radiosharp.R
import com.example.radiosharp.model.RadioClass

class RadioAdapter: RecyclerView.Adapter<RadioAdapter.ItemViewHodler>() {

    private var dataset = listOf<RadioClass>()

    class ItemViewHodler(view:View):RecyclerView.ViewHolder(view) {

        val radioName = view.findViewById<TextView>(R.id.radio_name_detail)
        val genreName = view.findViewById<TextView>(R.id.genreText)
        val countryName = view.findViewById<TextView>(R.id.countryText)
        val iconImage = view.findViewById<ImageView>(R.id.icon_image_detail)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHodler {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.radio_item,parent,false)
        return ItemViewHodler(itemLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHodler, position: Int) {

        val radioData : RadioClass = dataset[position]
        holder.radioName.setText(radioData.name)
        holder.countryName.setText(radioData.country)
        holder.genreName.setText(radioData.tags)
        holder.iconImage.load(radioData.favicon)

    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}