package com.example.radiosharp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.adapter.FavAdapter
import com.example.radiosharp.databinding.FavFragmentBinding
import com.example.radiosharp.model.FavClass
import com.example.radiosharp.model.RadioClass
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val favAdapter = FavAdapter(requireContext(), viewModel::fillText)

        binding.radioRecyclerViewFav.adapter = favAdapter


        viewModel.favoritenListe.observe(viewLifecycleOwner, Observer {
            favAdapter.submitlist(it)
        })

        binding.deleteAllButtonFav.setOnClickListener {
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

                val deletedCourse: RadioClass =
                    viewModel.favoritenListe.value!!.get(viewHolder.adapterPosition)
                val position = viewHolder.adapterPosition

                if (viewModel.favoritenListe.value != null) {

                    viewModel.favoritenListe.value?.removeAt(position)
                    viewModel.removeFav(FavClass(deletedCourse.stationuuid))
                    favAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                    Snackbar.make(
                        binding.radioRecyclerViewFav,
                        "Deleted" + deletedCourse.name,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(
                            "Undo",
                            View.OnClickListener {

                                viewModel.favoritenListe.value?.add(position, deletedCourse)
                                favAdapter.notifyItemInserted(position)

                            }).show()
                    Log.d("PositionLog", "$position")
                    Log.d("DeletedCourseLog_Name", "${deletedCourse.name}")
                    Log.d("DeletedCourseLog_UID", "${deletedCourse.stationuuid}")
                }
            }
        }).attachToRecyclerView(binding.radioRecyclerViewFav)

    }
}




