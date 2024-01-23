package com.astro.radiosharp.ui

import CustomDialog
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.astro.radiosharp.ApiStatus
import com.astro.radiosharp.MainViewModel
import com.astro.radiosharp.R
import com.astro.radiosharp.adapter.RadioAdapter
import com.astro.radiosharp.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {


    private lateinit var binding: HomeFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var customDialog : CustomDialog


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


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

       //* um die Funktionalität des Visualizers zu gewährleisten setzten wir hier die benötigten Permissions.

        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO), 42)
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.RECORD_AUDIO)
        }

        //* initializierung bei des RadioAdapters

        val radioAdapter = RadioAdapter(requireContext(),
            viewModel::fillText,viewModel)

        binding.radioRecyclerView.adapter = radioAdapter

        //* hier setzten wir unseren ApiStatus um mögliche
        // Verbindungsabbrüche und leere Suchergebnisse mit visuellen Kennzeichnungen abzudecken

        viewModel.apiStatus.observe(viewLifecycleOwner) {

            when(it) {
                ApiStatus.LOADING -> {
                    binding.progressBarHome.visibility = View.VISIBLE
                    binding.radioRecyclerView.visibility = View.GONE
                    binding.linearlayoutErrorHome.visibility = View.GONE
                    binding.linearLayoutIntroHome.visibility = View.GONE
                    binding.linearLayoutNoResultHome.visibility = View.GONE
                }

                //Wir interpretieren "START" als "es liegen Ergebnisse an."
                ApiStatus.FOUND_RESULTS -> {
                    binding.progressBarHome.visibility = View.GONE
                    binding.linearLayoutIntroHome.visibility = View.VISIBLE
                    binding.linearlayoutErrorHome.visibility = View.GONE
                    binding.radioRecyclerView.visibility = View.VISIBLE
                    binding.linearLayoutNoResultHome.visibility = View.GONE
                    binding.homeTotalTextHome.visibility = View.VISIBLE
                }

                ApiStatus.ERROR -> {
                    binding.progressBarHome.visibility = View.GONE
                    binding.radioRecyclerView.visibility = View.GONE
                    binding.linearlayoutErrorHome.visibility = View.VISIBLE
                    binding.linearLayoutIntroHome.visibility = View.GONE
                    binding.linearLayoutNoResultHome.visibility = View.GONE
                    binding.homeTotalTextHome.visibility = View.GONE
                }

                ApiStatus.START -> {
                    binding.progressBarHome.visibility = View.GONE
                    binding.linearlayoutErrorHome.visibility = View.GONE
                    binding.radioRecyclerView.visibility = View.GONE
                    binding.linearLayoutNoResultHome.visibility = View.GONE
                    binding.homeTotalTextHome.visibility = View.GONE
                    binding.linearLayoutIntroHome.visibility = View.VISIBLE
                }

                ApiStatus.FOUND_NO_RESULTS -> {
                    binding.progressBarHome.visibility = View.GONE
                    binding.linearLayoutIntroHome.visibility = View.GONE
                    binding.radioRecyclerView.visibility = View.GONE
                    binding.homeTotalTextHome.visibility = View.GONE
                    binding.linearLayoutNoResultHome.visibility = View.VISIBLE
                    binding.noResultTextHome.text = "We´re sorry, nothing found in our database"
                }
            }
        }

        //* hier beobachten wir die Radioliste mit und setzten unserem item Count für die Suchergebnisse
        viewModel.allRadios.observe(viewLifecycleOwner, Observer {
            radioAdapter.submitlist(it)

            if (radioAdapter.itemCount == 1){
                binding.homeTotalTextHome.text = "item: ${radioAdapter.itemCount}"
            } else{
                binding.homeTotalTextHome.text = "items: ${radioAdapter.itemCount}"
            }
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
    //* in der Funktion "showPopUp" inflaten wir das Popup Menu und setzten verschiedene output Bedingungen
    @RequiresApi(Build.VERSION_CODES.Q)
    fun showPopUp(view: View) {

        customDialog = CustomDialog(requireContext(),requireActivity())

        // Überprüfung der Android Version (SDK_INT) Verallgemeinert die Handy SDK
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            val wrapper = ContextThemeWrapper(requireContext(),R.style.popupMenuStyle)
            val popupMenu = PopupMenu(wrapper, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){

                    R.id.pop_up_fav_home -> {
                        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFavFragment())
                    }

                    R.id.pop_up_deleteAll_home -> {

                        // *** Custom Dialog für Fav löschen für niedrigere SDK Versionen unter SDK 29 *** //
                        customDialog.showDialog()

                        customDialog.setTextDialog("Suchergebnisse löschen?")

                        customDialog.setIcon(R.drawable.ic_baseline_delete_24)

                        customDialog.setAnswerYesAction { viewModel.deleteAll() }
                    }

                    R.id.pop_up_end_home -> {

                        // *** Custom Dialog für App beenden für niedrigere SDK Versionen unter SDK 29 *** //
                        customDialog.showDialog()

                        customDialog.setTextDialog("App wirklich Beenden?")

                        customDialog.setIcon(R.drawable.ic_baseline_exit_to_app_24)

                        customDialog.doINeedExitApp = true
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
            inflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.menu.findItem(R.id.pop_up_fav_home).setIcon(R.drawable.ic_baseline_favorite_24)
            popupMenu.menu.findItem(R.id.pop_up_deleteAll_home).setIcon(R.drawable.ic_baseline_delete_24)
            popupMenu.menu.findItem(R.id.pop_up_end_home).setIcon(R.drawable.ic_baseline_exit_to_app_24)

            popupMenu.setForceShowIcon(true)

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){


                    R.id.pop_up_fav_home -> {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToFavFragment()
                        )
                    }

                    R.id.pop_up_deleteAll_home -> {

                        // *** Custom Dialog für Suchergebnisse löschen für SDK 29 *** //
                        customDialog.showDialog()

                        customDialog.setTextDialog("Suchergebnisse löschen?")

                        customDialog.setIcon(R.drawable.ic_baseline_delete_24)

                        customDialog.setAnswerYesAction { viewModel.deleteAll() }
                    }

                    R.id.pop_up_end_home -> {

                        // *** Custom Dialog für App beenden für SDK 29 *** //
                        customDialog.showDialog()

                        customDialog.setTextDialog("App wirklich Beenden?")

                        customDialog.setIcon(R.drawable.ic_baseline_exit_to_app_24)

                        customDialog.doINeedExitApp = true
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



