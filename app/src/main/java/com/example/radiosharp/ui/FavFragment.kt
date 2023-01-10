package com.example.radiosharp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.adapter.FavAdapter
import com.example.radiosharp.databinding.FavFragmentBinding

class FavFragment: Fragment() {

    private lateinit var binding: FavFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FavFragmentBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val favAdapter = FavAdapter(requireContext(),viewModel::fillText)

        binding.radioRecyclerViewFav.adapter = favAdapter

        viewModel.favoritenListe.observe(viewLifecycleOwner, Observer {
            favAdapter.submitlist(it)
        })

        viewModel.radioDatabase.observe(viewLifecycleOwner, Observer {
            favAdapter.submitlist(it)
        })
    }

}


