package com.example.radiosharp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        val serverid = requireArguments().getString("changeuuid")

        viewModel.getServerid(binding.radioNameDetail,serverid)

    }
}