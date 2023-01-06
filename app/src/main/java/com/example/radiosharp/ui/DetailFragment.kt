package com.example.radiosharp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import coil.load
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.databinding.DetailFragmentBinding

class DetailFragment: Fragment() {


    private lateinit var binding: DetailFragmentBinding

    private val viewModel : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //TODO wir speichern den station-key in der variable serverID
        val serverid = requireArguments().getString("stationuuid")

        //TODO wir schauen die Liste an Radio Stationen durch den observer an, wir vergleichen die Server ID mit der
        // jeder StationsID in der Liste bis die Station die die gleiche ID hat wie die Server ID gefunden wird, und in der Variable currentSation speichern wir das Ergebnis.
       viewModel.loadTheRadio.observe(viewLifecycleOwner, Observer {

           val currentStation = it.find {
               it.stationuuid == serverid
           }
           //TODO Null abfrage dient dazu um zu erkenen ob die current station gleich "null" ist
           if(currentStation != null) {
               binding.radioNameDetail.text = currentStation.name
               binding.iconImageDetail.load(currentStation.favicon)
               binding.countryTextDetail.text = currentStation.country
               binding.genreTextDetail.text = currentStation.tags
           }
       })

    }
}