package com.example.radiosharp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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

        val radioAdapter = RadioAdapter(requireContext(),viewModel::fillText)

        binding.radioRecyclerView.adapter = radioAdapter


        viewModel.allRadios.observe(viewLifecycleOwner, Observer {
            radioAdapter.submitlist(it)
            Log.d("HomeFragment","$it")
        })

            binding.searchButton.setOnClickListener {

                viewModel.buttonAnimator(binding.searchButton)

                viewModel.loadText(binding.inputSearchText,requireContext())

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
