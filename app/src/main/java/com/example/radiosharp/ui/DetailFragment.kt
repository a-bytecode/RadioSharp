package com.example.radiosharp.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import coil.load
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.databinding.DetailFragmentBinding
import com.example.radiosharp.model.RadioClass

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
        //TODO aktiviert den Audio Manager
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //TODO Audio-regulation für die VolumeSeekbar
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        //TODO wir speichern den station-key in der variable serverID
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
               binding.iconImageDetail.load(currentStation.favicon)
               binding.countryTextDetail.text = currentStation.country
               binding.genreTextDetail.text = currentStation.tags
           }
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
               mediaPlayer!!.setDataSource(requireContext(),currentStation!!.playRadio.toUri())
               mediaPlayer!!.prepare()
               mediaPlayer!!.start()
           }
           binding.stopButton.setOnClickListener {
               viewModel.buttonAnimator(binding.stopButton)
               stopPlaying()
           }
       })
        //TODO Damit die Seekbar die Laustärke regulieren kann definieren wir hier,
        // die Maximale und die aktuelle Lautstärke.
        binding.volumeSeekBar.max = maxVolume
        binding.volumeSeekBar.progress = curVolume

        binding.volumeSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
}

}