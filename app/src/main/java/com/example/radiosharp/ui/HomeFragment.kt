package com.example.radiosharp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.adapter.RadioAdapter
import com.example.radiosharp.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {


    private lateinit var binding: HomeFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = HomeFragmentBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val radioAdapter = RadioAdapter(requireContext(),viewModel::fillText)

        binding.radioRecyclerView.adapter = radioAdapter

        viewModel.loadTheRadio.observe(viewLifecycleOwner, Observer {
            radioAdapter.submitlist(it)
            Log.d("HomeFragment","$it")
        })

            binding.searchButton.setOnClickListener {

                viewModel.buttonAnimator(binding.searchButton)

                viewModel.loadText(binding.inputSearchText,requireContext())


    }
}

}