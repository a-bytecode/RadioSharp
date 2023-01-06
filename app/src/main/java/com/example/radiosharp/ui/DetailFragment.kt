package com.example.radiosharp.ui

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import coil.load
import com.bumptech.glide.Glide
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.databinding.DetailFragmentBinding
import com.example.radiosharp.model.RadioClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailFragment: Fragment() {

    private lateinit var binding: DetailFragmentBinding

    private val viewModel : MainViewModel by activityViewModels()

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var audioManager: AudioManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailFragmentBinding.inflate(inflater)
        return binding.root
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

           val currentStation = it.find {
               it.stationuuid == serverid
           }
           //TODO Die "Null" Abfrage dient dazu um zu erkenen ob die current station gleich "null" ist
           if(currentStation != null) {

               binding.radioNameDetail.text = currentStation.name
               binding.countryTextDetail.text = currentStation.country
               binding.genreTextDetail.text = currentStation.tags

               viewModel.fillText(binding.countryTextDetail)
               viewModel.fillText(binding.genreTextDetail)

               //TODO Starten von animierten gifs in der Detailansicht
               val gif = ContextCompat.getDrawable(requireContext(),R.drawable.giphy3) as AnimatedImageDrawable
               gif.start()

               Glide.with(requireContext())
                   .load(currentStation.favicon)
                   .placeholder(gif)
                   .into(binding.iconImageDetail)


               binding.playButton.setOnClickListener {
                   viewModel.buttonAnimator(binding.playButton)
                   stopPlaying()

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
               binding.stopButton.setOnClickListener {
                   viewModel.buttonAnimator(binding.stopButton)
                   stopPlaying()
               }
           } else {
               binding.playButton.setOnClickListener {
               }
               binding.stopButton.setOnClickListener {
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

}