package com.example.radiosharp.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.radiosharp.ApiStatus
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.databinding.DetailFragmentBinding
import com.example.radiosharp.model.RadioClass
import com.example.radiosharp.visualizer.SquareBarVisualizer

class DetailFragment : Fragment() {


    private lateinit var binding: DetailFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var currentStation: RadioClass

    private lateinit var mediaController: MediaController

    private lateinit var audioManager: AudioManager

//    private lateinit var squareBarVisualizer: SquareBarVisualizer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPlaying()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Das definieren des Audio Managers um den Sound in der SeekBar zu regulieren
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Um die Argumete zu übergeben, speichern wir den Station-key in der variable serverid
        val serverid = requireArguments().getString("stationuuid")


        viewModel.loadingprogress.observe(viewLifecycleOwner) {
            when (it) {
                com.example.radiosharp.ApiStatus.LOADING -> {
                    binding.playImageDetail.visibility = android.view.View.GONE
                    binding.progressBarDetail.visibility = android.view.View.VISIBLE
                }
                com.example.radiosharp.ApiStatus.DONE -> {
                    binding.playImageDetail.visibility = android.view.View.VISIBLE
                    binding.progressBarDetail.visibility = android.view.View.GONE
                }
                com.example.radiosharp.ApiStatus.ERROR -> {

                    binding.progressBarDetail.visibility = android.view.View.VISIBLE
                    binding.playImageDetail.visibility = android.view.View.GONE
                }
            }
        }

        // Wir schauen die Liste an Radio Stationen durch den observer an, wir vergleichen die Server ID mit der
        // jeder StationsID in der Liste bis die Station die die gleiche ID hat wie die Server ID gefunden wird,
        // und in der Variable currentSation speichern wir das Ergebnis.
        viewModel.loadTheRadio.observe(viewLifecycleOwner, Observer {

            currentStation =
                it.find { radiostation -> // "radiostation" ist die Betitelung der jeweiligen Variable um die es sich handelt ersatz für "it"
                    radiostation.stationuuid == serverid
                }!!

            binding.radioNameDetail.text = currentStation.name
            binding.headerTextDialogDetail.text = currentStation.name
            binding.countryTextDialogDetail.text = currentStation.country
            binding.genreTextDialogDetail.text = currentStation.tags

            viewModel.fillText(binding.countryTextDialogDetail)
            viewModel.fillText(binding.genreTextDialogDetail)
            // Starten von animierten Gifs in der Detailansicht
            val gif = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.giphy3
            ) as AnimatedImageDrawable
            gif.start()

            Glide.with(requireContext())
                .load(currentStation.favicon)
                .placeholder(gif)
                .into(binding.iconImageDetail)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
            // Durch diese Variable sagen wir der Uri das sie anstatt "http" -> "https:" laden soll.
            // Da die normale "http" nicht in der Lage war "https" Url abzurufen.
            val uri = if (currentStation.playRadio.contains("https:")) {
                currentStation.playRadio
            } else {
                currentStation.playRadio.replace("http:", "https:")
            }

            mediaPlayer!!.setDataSource(requireContext(), uri.toUri())
            mediaPlayer!!.prepareAsync()
            mediaPlayer!!.setOnPreparedListener {

                binding.playImageDetail.setOnClickListener {
                    mediaPlayer!!.start()
                    binding.playImageDetail.visibility = View.GONE
                    binding.stopImageDetail.visibility = View.VISIBLE
                }
            }
            binding.stopImageDetail.setOnClickListener {
                binding.stopImageDetail.visibility = View.GONE
                binding.playImageDetail.visibility = View.VISIBLE
                mediaPlayer!!.pause()
            }
            binding.skipNextImageDetail.setOnClickListener {
                if (currentStation.nextStation.isNotEmpty()) {
                    findNavController().navigate(
                        DetailFragmentDirections.actionDetailFragmentSelf(
                            currentStation.nextStation
                        )
                    )
                }
            }
            binding.skipPreviousImageDetail.setOnClickListener {
                if (currentStation.previousStation.isNotEmpty()) {
                    findNavController().navigate(
                        DetailFragmentDirections.actionDetailFragmentSelf(
                            currentStation.previousStation
                        )
                    )
                }
            }
            binding.favListImageDetail.setOnClickListener {
                findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToFavFragment())
            }
            // Veränderung der Zustände an dem Favoriten Symbol durch die if Verzweigung
            if (currentStation.favorite == false) {
                binding.favOffImageDetail.visibility = View.VISIBLE
                binding.favOnImageDetail.visibility = View.GONE
            } else {
                binding.favOnImageDetail.visibility = View.VISIBLE
                binding.favOffImageDetail.visibility = View.GONE
            }
            //  Implementierung der remove & add Funktionen an dem Favoriten Symbol
            binding.favOnImageDetail.setOnClickListener {
                binding.favOffImageDetail.visibility = View.VISIBLE
                binding.favOnImageDetail.visibility = View.GONE
                viewModel.removeFav(currentStation)
            }
            binding.favOffImageDetail.setOnClickListener {
                binding.favOnImageDetail.visibility = View.VISIBLE
                binding.favOffImageDetail.visibility = View.GONE
                viewModel.addFav(currentStation)
            }

            // Hier sucht die Favoritenliste den Markierten Favorit
            // indem es durch eine "Boolean" Abfrage nachprüft.
            viewModel.favoritenListe.observe(viewLifecycleOwner, Observer {
                Log.d("removeFavorite", "${viewModel.favoritenListe.value?.size}")
                it.find {
                    it.favorite
                }
            })
            binding.informationImageDetail.setOnClickListener {
                binding.informationDialogDetail.visibility = View.VISIBLE
                binding.okButtonDialog.setOnClickListener {
                    binding.informationDialogDetail.visibility = View.GONE
                }
            }
            binding.homeImageDetail.setOnClickListener {
                findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToHomeFragment())
            }
//            if (mediaPlayer != null) {
//                squareBarVisualizer.visualizer.enabled
//                squareBarVisualizer.animation.start()
//                squareBarVisualizer.setColor(R.drawable.gradient_yellow_pink)
//                squareBarVisualizer.setDensity(150F)
//                squareBarVisualizer.setGap(2)
//                squareBarVisualizer.setPlayer(mediaPlayer!!.audioSessionId)
//                squareBarVisualizer.release()
//            }
        })
        // Damit die VolumeSeekbar die Laustärke regulieren kann definieren wir hier,
        // die Maximale und die aktuelle Lautstärke.
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        binding.volumeSeekBar.max = maxVolume
        binding.volumeSeekBar.progress = curVolume

        binding.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    // Um Abstürze beim drücken vom Stop des Tracks zu beseitigen definieren wir hier eine Funktion
    // die den Mediaplayer stoppt und weiterspielen lässt wenn es nicht "null" ist.
    private fun stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
    private fun resetPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
    // Fehler der Multiplen Wiedergabe beheben
    private fun resetAllPlayers(mediaPlayer: MediaPlayer) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        } else {
            mediaPlayer.start()
        }
    }
}
