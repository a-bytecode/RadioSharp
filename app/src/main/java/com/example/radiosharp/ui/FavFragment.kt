package com.example.radiosharp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.adapter.FavAdapter
import com.example.radiosharp.databinding.FavFragmentBinding
import com.example.radiosharp.model.FavClass
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class FavFragment : Fragment() {

    private lateinit var binding: FavFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FavFragmentBinding.inflate(inflater)
        viewModel.getFav()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val favAdapter = FavAdapter(requireContext(), viewModel::fillText)

        binding.radioRecyclerViewFav.adapter = favAdapter

        viewModel.favRadios.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                favAdapter.submitlist(it)

                if(it.size == 0) {
                    binding.favTotalTextFav.visibility = View.GONE
                } else {
                    binding.favTotalTextFav.visibility = View.VISIBLE
                }
                if(it.size == 1){
                    binding.favTotalTextFav.text = "item: ${it.size}"
                } else {
                    binding.favTotalTextFav.text = "items: ${it.size}"
                }
            }
        })

        binding.searchButtonFav.setOnClickListener {
            val searchText = binding.inputSearchTextFav.text.toString()
            if(searchText == "") {
                viewModel.getFav()
            } else {
                viewModel.getFavSearch(searchText)
            }
        }

        binding.favListImageFav.setOnClickListener {
            showPopUp(binding.favListImageFav)
        }


        // Swipe to Delete Funktion
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val deletedCourse: FavClass =
                    viewModel.favRadios.value!![viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition

                if (viewModel.favRadios.value != null) {

                    viewModel.favRadios.value?.removeAt(position)
                    viewModel.removeFav(
                        FavClass(
                            deletedCourse.stationuuid,
                            deletedCourse.country,
                            deletedCourse.name,
                            deletedCourse.radioUrl,
                            deletedCourse.favicon,
                            deletedCourse.tags,
                            deletedCourse.nextStation,
                            deletedCourse.previousStation
                        )
                    )
                    favAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                    Snackbar.make(
                        binding.radioRecyclerViewFav,
                        "Deleted" + deletedCourse.name,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(
                            "Undo",
                            View.OnClickListener {

                                viewModel.favRadios.value?.add(
                                    position,
                                    deletedCourse
                                )
                                favAdapter.notifyItemInserted(position)

                            }).show()
                    Log.d("PositionLog", "$position")
                    Log.d("DeletedCourseLog_Name", "${deletedCourse.name}")
                    Log.d("DeletedCourseLog_UID", "${deletedCourse.stationuuid}")
                }
            }
        }).attachToRecyclerView(binding.radioRecyclerViewFav)

    }

    fun showPopUp(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.popup_menu_fav, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {

            when (it.itemId) {

                R.id.pop_up_home_fav -> {
                    findNavController().navigate(FavFragmentDirections.actionFavFragmentToHomeFragment())
                }

                R.id.pop_up_deleteAll_fav -> {
                    viewModel.deleteAllFav()
                }

                R.id.pop_up_end_home -> {
                    fun showEndDialog() {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Beenden")
                            .setMessage("App wirklich Beenden?")
                            .setCancelable(false)
                            .setNegativeButton("Nein") { _, _ ->
                                findNavController().navigate(FavFragmentDirections.actionFavFragmentSelf())
                            }
                            .setPositiveButton("Ja") { _, _ ->
                                activity?.finish()
                            }
                            .show()
                    }
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




