package com.example.radiosharp.ui

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
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
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.databinding.DetailFragmentBinding
import com.example.radiosharp.model.RadioClass

class DetailFragment: Fragment() {

    private lateinit var binding: DetailFragmentBinding

    private val viewModel : MainViewModel by activityViewModels()

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var currentStation: RadioClass

    private lateinit var mediaController : MediaController

    private lateinit var audioManager: AudioManager

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

        //TODO aktiviert den Audio Manager um Sound abzuspielen
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //TODO Um die Argumete zu übergeben, speichern wir den Station-key in der variable serverid
        val serverid = requireArguments().getString("stationuuid")

        //TODO wir schauen die Liste an Radio Stationen durch den observer an, wir vergleichen die Server ID mit der
        // jeder StationsID in der Liste bis die Station die die gleiche ID hat wie die Server ID gefunden wird,
        // und in der Variable currentSation speichern wir das Ergebnis.
       viewModel.loadTheRadio.observe(viewLifecycleOwner, Observer {

            currentStation = it.find {
                it.stationuuid == serverid
            }!!
           //TODO Die "Null" Abfrage dient dazu um zu erkenen ob die current station gleich "null" ist
           if(currentStation != null) {

               binding.radioNameDetail.text = currentStation.name
//               binding.countryTextDetail.text = currentStation.country
//               binding.genreTextDetail.text = currentStation.tags
               binding.headerTextDialogDetail.text = currentStation.name
               binding.countryTextDialogDetail.text = currentStation.country
               binding.genreTextDialogDetail.text = currentStation.tags

               viewModel.fillText(binding.countryTextDialogDetail)
               viewModel.fillText(binding.genreTextDialogDetail)

               //TODO Starten von animierten gifs in der Detailansicht
               val gif = ContextCompat.getDrawable(requireContext(),R.drawable.giphy3) as AnimatedImageDrawable
               gif.start()

               Glide.with(requireContext())
                   .load(currentStation.favicon)
                   .placeholder(gif)
                   .into(binding.iconImageDetail)


               binding.playImageDetail.setOnClickListener {

                   binding.playImageDetail.visibility = View.GONE
                   binding.stopImageDetail.visibility = View.VISIBLE

                   mediaPlayer = MediaPlayer().apply {
                       setAudioAttributes(
                           AudioAttributes.Builder()
                               .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                               .setUsage(AudioAttributes.USAGE_MEDIA)
                               .build()
                       )
                   }
                       mediaPlayer!!.setDataSource(requireContext(),currentStation.playRadio.toUri())
                       mediaPlayer!!.prepareAsync()
                       mediaPlayer!!.setOnPreparedListener {
                           it.start()
                       }
               }
               binding.stopImageDetail.setOnClickListener {
                   binding.stopImageDetail.visibility = View.GONE
                   binding.playImageDetail.visibility = View.VISIBLE
                   stopPlaying()
               }
           } else {
               binding.playImageDetail.setOnClickListener {
               }
               binding.playImageDetail.setOnClickListener {
               }
           }

           binding.skipNextImageDetail.setOnClickListener {
           }

           binding.skipPreviousImageDetail.setOnClickListener {
           }

           if (currentStation.favorite == false) {
               binding.favOffImageDetail.visibility = View.VISIBLE
               binding.favOnImageDetail.visibility = View.GONE
           } else {
               binding.favOnImageDetail.visibility = View.VISIBLE
               binding.favOffImageDetail.visibility = View.GONE
           }
           binding.favOnImageDetail.setOnClickListener {
               binding.favOffImageDetail.visibility = View.VISIBLE
               binding.favOnImageDetail.visibility = View.GONE
               currentStation.favorite = false
               viewModel.removeFav(currentStation)
           }
           binding.favOffImageDetail.setOnClickListener {
               binding.favOnImageDetail.visibility = View.VISIBLE
               binding.favOffImageDetail.visibility = View.GONE
               currentStation.favorite = true
               viewModel.addFav(currentStation)
           }

           viewModel.favoritenListe.observe(viewLifecycleOwner, Observer {
               Log.d("removeFavorite","${viewModel.favoritenListe.value?.size}")
           })

           binding.informationImageDetail.setOnClickListener {
               binding.informationDialogDetail.visibility = View.VISIBLE
               binding.okButtonDialog.setOnClickListener {
                 binding.informationDialogDetail.visibility = View.GONE
               }
           }

       })
        //TODO Damit die VolumeSeekbar die Laustärke regulieren kann definieren wir hier,
        // die Maximale und die aktuelle Lautstärke.
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        binding.volumeSeekBar.max = maxVolume
        binding.volumeSeekBar.progress = curVolume

        binding.volumeSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
    //TODO um Abstürze beim drücken vom Stop des Tracks zu beseitigen definieren wir hier eine Funktion
    // die den Mediaplayer stoppt und weiterspielen lässt wenn es nicht "null" ist.
    private fun stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
}
    private fun resetPlaying(){
        if(mediaPlayer != null){
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
    // TODO Fehler der Multiplen Wiedergabe beheben
    private fun resetAllPlayers(mediaPlayer: MediaPlayer) {
        if(mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        } else {
            mediaPlayer.start()
        }
    }
    //TODO previous und next sound abspielen fixen
    private fun nextIndex(list: List<RadioClass>) {
        for (i in list) {

        }
    }


}