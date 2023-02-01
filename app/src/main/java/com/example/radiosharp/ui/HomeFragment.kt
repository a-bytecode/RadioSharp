package com.example.radiosharp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.radiosharp.ApiStatus
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.adapter.RadioAdapter
import com.example.radiosharp.databinding.HomeFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {


    private lateinit var binding: HomeFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun registerForContextMenu(view: View) {
        super.registerForContextMenu(view.findViewById(R.id.favList_image_fav))
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = HomeFragmentBinding.inflate(inflater)


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO), 42)
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.RECORD_AUDIO)
        }

        val radioAdapter = RadioAdapter(requireContext(),viewModel::fillText)

        binding.radioRecyclerView.adapter = radioAdapter

        binding.linearlayoutErrorHome.visibility = View.GONE



        viewModel.apiStatus.observe(viewLifecycleOwner) {

            when(it) {
                ApiStatus.LOADING -> {
                    binding.progressBarHome.visibility = View.VISIBLE
                    binding.radioRecyclerView.visibility = View.GONE
                    binding.linearlayoutErrorHome.visibility = View.GONE
                    binding.linearLayoutIntroHome.visibility = View.GONE
                }
                ApiStatus.DONE -> {
                    binding.progressBarHome.visibility = View.GONE
                    binding.linearlayoutErrorHome.visibility = View.GONE
                    binding.radioRecyclerView.visibility = View.VISIBLE
                    if(radioAdapter.itemCount > 0) {
                        binding.linearLayoutIntroHome.visibility = View.GONE
                        binding.linearlayoutErrorHome.visibility = View.GONE

                    } else {
                        binding.radioRecyclerView.visibility = View.VISIBLE
                        binding.linearlayoutErrorHome.visibility = View.GONE
                    }
                }
                ApiStatus.ERROR -> {
                    binding.progressBarHome.visibility = View.GONE
                    binding.radioRecyclerView.visibility = View.GONE
                    binding.linearlayoutErrorHome.visibility = View.VISIBLE
                    binding.linearLayoutIntroHome.visibility = View.GONE
                }
            }

        }


        viewModel.allRadios.observe(viewLifecycleOwner, Observer {
//            if(it.size > 0) {
//                binding.linearLayoutIntroHome.visibility = View.VISIBLE
//            } else {
//                binding.linearLayoutIntroHome.visibility = View.GONE
//            }
                radioAdapter.submitlist(it)
                Log.d("HomeFragment","$it")
        })

        binding.searchButton.setOnClickListener {
            viewModel.buttonAnimator(binding.searchButton)
            viewModel.loadText(binding.inputSearchText,requireContext(),binding.errortextHome)
        }

        binding.favListImageHome.setOnClickListener{
            showPopUp(binding.favListImageHome)
        }

}

    fun showPopUp(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){


                R.id.pop_up_fav_home -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFavFragment())
                }

                R.id.pop_up_deleteAll_home -> {
                    viewModel.deleteAll()
                }

                R.id.pop_up_end_home -> {
                    fun showEndDialog() {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Beenden")
                            .setMessage("App wirklich Beenden?")
                            .setCancelable(false)
                            .setNegativeButton("Nein") { _,_ ->
                                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentSelf())
                            }
                            .setPositiveButton("Ja") { _,_ ->
                                activity?.finish()
                            }
                            .show() }
                    showEndDialog()
                }
            }
            true
        }
        popupMenu.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.

        popupMenu.show()
    }
}
