package com.example.radiosharp.adapter

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
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
import coil.load
import com.bumptech.glide.Glide
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.databinding.DetailFragmentBinding
import com.example.radiosharp.model.RadioClass
import com.example.radiosharp.ui.HomeFragmentDirections

class RadioAdapter(val context: Context, val defaultText : (text:TextView)-> Unit): RecyclerView.Adapter<RadioAdapter.ItemViewHodler>() {


    private var dataset = listOf<RadioClass>()


    fun submitlist(radioList:List<RadioClass>) {
        dataset = radioList
        notifyDataSetChanged()
    }

    class ItemViewHodler(view:View):RecyclerView.ViewHolder(view) {

        val radioName = view.findViewById<TextView>(R.id.radio_name_detail)
        val genreName = view.findViewById<TextView>(R.id.genreText)
        val countryName = view.findViewById<TextView>(R.id.countryText)
        val iconImage = view.findViewById<ImageView>(R.id.icon_image_detail)
        val radioCardView = view.findViewById<CardView>(R.id.radioCardView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHodler {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.radio_item,parent,false)
        return ItemViewHodler(itemLayout)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(holder: ItemViewHodler, position: Int) {

        val radioData : RadioClass = dataset[position]

        if (position < dataset.size -1 && position > 0){
            val previousRadioData : RadioClass = dataset[position -1]
            val nextRadioData : RadioClass = dataset[position +1]
            Log.d("TEST","${nextRadioData.stationuuid}")
            radioData.previousStation = previousRadioData.stationuuid
            radioData.nextStation = nextRadioData.stationuuid
        }

        holder.radioName.text = radioData.name
        holder.countryName.text = radioData.country
        holder.genreName.text = radioData.tags
        holder.radioCardView.setOnClickListener {
            holder.itemView.findNavController().navigate(
                HomeFragmentDirections
                    .actionHomeFragmentToDetailFragment(radioData.stationuuid,radioData.nextStation,radioData.previousStation))
        }



        val gif = ContextCompat.getDrawable(context,R.drawable.giphy4) as AnimatedImageDrawable

        gif.start()
        Glide.with(context).load(radioData.favicon).placeholder(gif).into(holder.iconImage)
        defaultText(holder.countryName)
        defaultText(holder.genreName)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    //        if (radioData.favicon == "") {
////           Glide.with(context).load(R.drawable.giphy).
////           into(holder.iconImage)
//            holder.iconImage.setImageResource(R.drawable.giphy)
//        }else {
////            Glide.with(context).clear(holder.iconImage)
//            holder.iconImage.load(radioData.favicon)
//        }

    //        holder.iconImage.load(radioData.favicon){
//
//            target(
//                onStart = { placeholder ->
//                    if (placeholder == null) {
//                        Glide.with(context).load(R.drawable.giphy).into(holder.iconImage)
//                    }
//                }, onError = { placeholder ->
//                    if (placeholder == null) {
//                        Glide.with(context).load(R.drawable.giphy2).into(holder.iconImage)
//                    }
//
//                }, onSuccess = { result ->
//                    Glide.with(context).clear(holder.iconImage)
//                    holder.iconImage.setImageDrawable(result)
//                }
//
//            )
//        }


}