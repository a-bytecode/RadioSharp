package com.astro.radiosharp.ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.astro.radiosharp.MainViewModel
import com.astro.radiosharp.R
import com.astro.radiosharp.adapter.FavAdapter
import com.astro.radiosharp.databinding.FavFragmentBinding
import com.astro.radiosharp.model.FavClass
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
        //* die .getFav initilalisierung des FavRadios
        viewModel.getFav()
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val favAdapter = FavAdapter(requireContext(), viewModel::fillText)

        binding.radioRecyclerViewFav.adapter = favAdapter

        viewModel.favRadios.observe(viewLifecycleOwner, Observer {
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
        })


        binding.searchButtonFav.setOnClickListener {
            val inputText = binding.inputSearchTextFav.text.toString()

            if (inputText == "") {
                viewModel.getFav()
                Toast.makeText(requireContext(),
                    "Bitte Suchbegriff eingeben",
                    Toast.LENGTH_SHORT)
                    .show()
            } else {
                viewModel.getAllFavByName(inputText)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun showPopUp(view: View) {
        // Überprüfung der Android Version (SDK_INT) Verallgemeinert die Handy SDK
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){ // Wir Übeprüfen ob die aktuelle Mobile Version kleiner ist als Android 10

            val wrapper = ContextThemeWrapper(requireContext(),R.style.popupMenuStyle)
            val popupMenu = PopupMenu(wrapper, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.popup_menu_fav, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {

                    R.id.pop_up_home_fav -> {
                        findNavController().navigate(FavFragmentDirections.actionFavFragmentToHomeFragment())
                    }

                    R.id.pop_up_deleteAll_fav -> {
                        fun showEndDialog() {

                            // *** Custom Spannable Text für Fav-Löschen für niedrigere SDK Versionen unter SDK 29 *** //
                            val titleText = "Alle Favoriten löschen?"
                            val spannableTitle = SpannableString(titleText)
                            spannableTitle.setSpan(
                                ForegroundColorSpan(
                                Color.BLACK
                            ),
                                0,
                                titleText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(titleText)
                                .setIcon(R.drawable.ic_baseline_delete_24)
                                .setBackground(ContextCompat.getDrawable(requireContext(),
                                    R.drawable.custom_popup_backround))
                                .setCancelable(true)
                                .setNegativeButton("Nein") { _, _ ->
                                    findNavController().navigate(FavFragmentDirections.actionFavFragmentSelf())
                                }
                                .setPositiveButton("Ja") { _, _ ->
                                    viewModel.deleteAllFav()
                                }
                                .show()
                        }
                        showEndDialog()
                    }

                    R.id.pop_up_end_home -> {

                        fun showEndDialog() {

                            // *** Custom Spannable Text für App beenden für niedrigere SDK Versionen unter SDK 29 *** //
                            val titleText = "App wirklich Beenden?"
                            val spannableTitle = SpannableString(titleText)
                            spannableTitle.setSpan(
                                ForegroundColorSpan(
                                    Color.BLACK
                                ),
                                0,
                                titleText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(titleText)
                                .setIcon(R.drawable.ic_baseline_exit_to_app_24)
                                .setCancelable(true)
                                .setNegativeButton("Nein") { _, _ ->
                                    findNavController().navigate(
                                        FavFragmentDirections.actionFavFragmentSelf()
                                    )
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
        } else {
            val wrapper = ContextThemeWrapper(requireContext(),R.style.popupMenuStyle)
            val popupMenu = PopupMenu(wrapper, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.popup_menu_fav, popupMenu.menu)
            popupMenu.menu.findItem(R.id.pop_up_home_fav).setIcon(R.drawable.ic_baseline_home_24)
            popupMenu.menu.findItem(R.id.pop_up_deleteAll_fav).setIcon(R.drawable.ic_baseline_delete_24)
            popupMenu.menu.findItem(R.id.pop_up_end_home).setIcon(R.drawable.ic_baseline_exit_to_app_24)
            popupMenu.setForceShowIcon(true)

            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {

                    R.id.pop_up_home_fav -> {
                        findNavController().navigate(FavFragmentDirections.actionFavFragmentToHomeFragment())
                    }

                    R.id.pop_up_deleteAll_fav -> {
                        fun showEndDialog() {

                            // *** Custom Spannable Text für Fav-Löschen *** //
                            val titleText = "Alle Favoriten löschen?"
                            val spannableTitle = SpannableString(titleText)
                            spannableTitle.setSpan(
                                ForegroundColorSpan(
                                    Color.BLACK
                                ),
                                0,
                                titleText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(titleText)
                                .setIcon(R.drawable.ic_baseline_delete_24)
                                .setCancelable(true)
                                .setBackground(ContextCompat.
                                getDrawable(requireContext(),
                                    R.drawable.custom_popup_backround)
                                )
                                .setNegativeButton("Nein") { _, _ ->
                                    findNavController().navigate(
                                        FavFragmentDirections.actionFavFragmentSelf()
                                    )
                                }
                                .setPositiveButton("Ja") { _, _ ->
                                    viewModel.deleteAllFav()
                                }
                                .show()
                        }
                        showEndDialog()
                    }

                    R.id.pop_up_end_home -> {

                        fun showEndDialog() {

                            // *** Custom Spannable Text für App beenden *** //
                            val titleText = "App wirklich Beenden?"
                            val spannableTitle = SpannableString(titleText)
                            spannableTitle.setSpan(
                                ForegroundColorSpan(
                                    Color.BLACK
                                ),
                                0,
                                titleText.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(titleText)
                                .setIcon(R.drawable.ic_baseline_exit_to_app_24)
                                .setBackground(ContextCompat.
                                getDrawable(requireContext(),
                                    R.drawable.custom_popup_backround)
                                )
                                .setCancelable(true)
                                .setNegativeButton("Nein") { _, _ ->
                                    findNavController().navigate(
                                        FavFragmentDirections.actionFavFragmentSelf()
                                    )
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
}




