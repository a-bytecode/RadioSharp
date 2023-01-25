package com.example.radiosharp.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.chibde.visualizer.BarVisualizer
import com.chibde.visualizer.LineVisualizer
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.databinding.DetailFragmentBinding
import com.example.radiosharp.model.FavClass
import com.example.radiosharp.model.RadioClass

class DetailFragment : Fragment() {

    private lateinit var binding: DetailFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var currentStation: RadioClass

    private lateinit var mediaController: MediaController

    private lateinit var audioManager: AudioManager

    private lateinit var lineVisualizer: LineVisualizer

    private lateinit var barVisualizer: BarVisualizer

    private lateinit var visualizer: Visualizer

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

        // Wir schauen die Liste an Radio Stationen durch den observer an, wir vergleichen die Server ID mit der
        // jeder StationsID in der Liste bis die Station die die gleiche ID hat wie die Server ID gefunden wird,
        // und in der Variable currentSation speichern wir das Ergebnis.
        viewModel.allRadios.observe(viewLifecycleOwner, Observer {

            currentStation =
                it.find { radiostation -> // "radiostation" ist die Betitelung der jeweiligen Variable um die es sich handelt ersatz für "it"
                    radiostation.stationuuid == serverid
                }!!
            //Hier holen wir einen Boolean aus der Favoritenliste und
            // verknüpfen ihn mit der "currentStation"
            // um "is Favorite" für die Optische Anzeige der Favoriten zu benutzen.
            val isFavorite: Boolean =
                viewModel.favoritenListeRadioClass.value!!.contains(currentStation)

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

            //Zuweisung des Visualizers
            barVisualizer = view.findViewById(R.id.myVisualizer)

            mediaPlayer = MediaPlayer().apply {
                binding.playImageDetail.visibility = View.GONE
                binding.progressBarDetail.visibility = View.VISIBLE
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
            // Durch diese Variable sagen wir der Uri das sie anstatt "http" -> "https:" laden soll.
            // Da die normale "http" nicht in der Lage war "https" Url abzurufen.
            val uri = if (currentStation.radioUrl.contains("https:")) {
                currentStation.radioUrl
            } else {
                currentStation.radioUrl.replace("http:", "https:")
            }

            mediaPlayer!!.setDataSource(requireContext(), uri.toUri())
            mediaPlayer!!.prepareAsync()
            mediaPlayer!!.setOnPreparedListener {

                binding.progressBarDetail.visibility = View.GONE
                binding.playImageDetail.visibility = View.VISIBLE

                binding.playImageDetail.setOnClickListener {
                    mediaPlayer!!.start()
                    binding.playImageDetail.visibility = View.GONE
                    binding.stopImageDetail.visibility = View.VISIBLE
                }

            }
            //Visualizer prüft ob die permissions "Granted" sind und gibt bei der Wiedergabe des
            // Media Players den Effekt frei.
            if (mediaPlayer != null &&
                ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    ) {
                barVisualizer.visibility = View.VISIBLE
                barVisualizer.apply {
                    isEnabled
                    setColor(requireContext().getColor(R.color.white))
                    setDensity(15F)
//                    setStrokeWidth(1)
                    setPlayer(mediaPlayer!!.audioSessionId)
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
                        DetailFragmentDirections.actionDetailFragmentSelf(currentStation.nextStation)
                    )
                }
            }
            binding.skipPreviousImageDetail.setOnClickListener {
                if (currentStation.previousStation.isNotEmpty()) {
                    findNavController().navigate(
                        DetailFragmentDirections.actionDetailFragmentSelf(currentStation.previousStation)
                    )
                }
            }
            binding.favListImageDetail.setOnClickListener {
                findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToFavFragment())
            }

            // Veränderung der Zustände an dem Favoriten Symbol durch die if Verzweigung
            if (isFavorite) {
                // Zustände der Favoriten sind in der Funktion "toggleFav" ausgelagert
                toggleFav(true)

            } else {

                toggleFav(false)
            }
            //  Implementierung der remove & add Funktionen an dem Favoriten Symbol
            binding.favOnImageDetail.setOnClickListener {
                toggleFav(false)
                viewModel.removeFav(
                    FavClass(
                        currentStation.stationuuid,
                        currentStation.country,
                        currentStation.name,
                        currentStation.radioUrl,
                        currentStation.favicon,
                        currentStation.tags
                    )
                )
            }
            binding.favOffImageDetail.setOnClickListener {
                toggleFav(true)
                viewModel.addFav(
                    FavClass(
                        currentStation.stationuuid,
                        currentStation.country,
                        currentStation.name,
                        currentStation.radioUrl,
                        currentStation.favicon,
                        currentStation.tags
                    )
                )
            }

            binding.informationImageDetail.setOnClickListener {
                binding.informationDialogDetail.visibility = View.VISIBLE
                binding.okButtonDialog.setOnClickListener {
                    binding.informationDialogDetail.visibility = View.GONE
                }
            }
            binding.homeImageDetail.setOnClickListener {
                findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToHomeFragment())
            }
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

    fun toggleFav(on: Boolean) {
        if (on) {
            binding.favOnImageDetail.visibility = View.VISIBLE
            binding.favOffImageDetail.visibility = View.GONE
        } else {
            binding.favOffImageDetail.visibility = View.VISIBLE
            binding.favOnImageDetail.visibility = View.GONE
        }
    }

    fun createVisualizer() {
        barVisualizer = barVisualizer.apply {
            isEnabled
            setColor(requireContext().getColor(R.color.white))
            setDensity(15F)
//                    setStrokeWidth(1)
            setPlayer(mediaPlayer!!.audioSessionId)
        }
    }
}


