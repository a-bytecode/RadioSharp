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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.chibde.visualizer.BarVisualizer
import com.chibde.visualizer.CircleBarVisualizer
import com.chibde.visualizer.LineVisualizer
import com.chibde.visualizer.SquareBarVisualizer
import com.example.radiosharp.MainViewModel
import com.example.radiosharp.R
import com.example.radiosharp.databinding.DetailFragmentBinding
import com.example.radiosharp.model.FavClass
import com.example.radiosharp.model.RadioClass

class DetailFragment : Fragment() {

    private lateinit var binding: DetailFragmentBinding

    private val viewModel: MainViewModel by activityViewModels()

    private var mediaPlayer: MediaPlayer? = null

    // Überklasse von RadioClass und FavClass mit Methoden zum auffangen der Daten des geöffneten Radio Senders.
    // Notwendig weil, sowohl vom Typ RadioClass als auch vom Typ FavClass sein kann.
    private data class Radio(
        var stationuuid: String = "",
        var country: String = "",
        var name: String = "",
        var radioUrl: String = "",
        var favicon: String = "",
        var tags: String = "",
        var nextStation: String = "",
        var previousStation: String = "",
        var currentIndex: Int? = null
    ) {
        //"fromRadioClass" Falls wir aus der RadioClass kommen
        fun fromRadioClass(radioClass: RadioClass?) {
            if (radioClass != null) {
                stationuuid = radioClass.stationuuid
                country = radioClass.country
                name = radioClass.name
                radioUrl = radioClass.radioUrl
                favicon = radioClass.favicon
                tags = radioClass.tags
                nextStation = radioClass.nextStation
                previousStation = radioClass.previousStation
            }

        }

        //"fromFavClass" Falls wir aus der FavClass kommen
        fun fromFavClass(favClass: FavClass?) {
            if (favClass != null) {
                stationuuid = favClass.stationuuid
                country = favClass.country
                name = favClass.name
                radioUrl = favClass.radioUrl
                favicon = favClass.favicon
                tags = favClass.tags
                nextStation = favClass.nextStation
                previousStation = favClass.previousStation
            }
        }
    }

    // Hier erstellen wir ein leeres Objekt um es später zu füllen.
    private var currentStation: Radio = Radio()

    private lateinit var mediaController: MediaController

    private lateinit var audioManager: AudioManager

    private lateinit var lineVisualizer: LineVisualizer

    private lateinit var barVisualizer: BarVisualizer

    private lateinit var squareBarVisualizer: SquareBarVisualizer

    private lateinit var circleBarVisualizer: CircleBarVisualizer

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
        //Wir haben ein Boolean Argument erstellt um die Klassen voneinander schließen zu können woher das Radio kommt.
        //"FavClass oder RadioClass" -> OpeningFav Gibt an ob wir aus der Favoritenliste kommen.
        val openingFav = requireArguments().getBoolean("openingFav")

        // Wir schauen die Liste an Radio Stationen durch den observer an, wir vergleichen die Server ID mit der
        // jeder StationsID in der Liste bis die Station die die gleiche ID hat wie die Server ID gefunden wird,
        // und in der Variable currentSation speichern wir das Ergebnis.

        //Das Laden bzw. finden der Radios im Homescreen. (search funktion)
        //TODO Favoriten müssen aus der FavoritenTabelle geladen werden, nicht bedingungslos aus der "RadioClass"
        //      Grund: eine neue Suche kann einen Favoriten aus der RadioClass löschen
        if (openingFav) {
            //Wir kommen aus der Favoritenliste

            val favorites = viewModel.favRadios.value!!
            // Mit currentIndex -> "indexOfFirst" wollen wir das nächste Element auslesen.
            val currentIndex = favorites.indexOfFirst { it.stationuuid == serverid }

            //setzen die jetzige Station auf den jeweiligen Favoriten
            currentStation.fromFavClass(favorites[currentIndex])

            //Berechnen den nächsten Index der "stationuuid"
            val nextStationIndex = if (currentIndex == favorites.size - 1) { // Der Wert des letzten Elemnts
                // beim letzten "next" drücken fängt wieder vom Anfang der Liste an.
                0
            } else {
                currentIndex + 1
            }

            // Lösung mit Modulo Operator -->
//            val nextStationIndex = (currentIndex + 1) % favorites.size // Der Modulo-Operator (%),
            // auch Modulo-Funktion genannt,
            // berechnet den Rest einer Division von zwei Zahlen.
            // Der Ausdruck a % b gibt den Rest zurück, wenn man a durch b teilt.

            val previousStationIndex = if (currentIndex == 0) { // Der Wert des Ersten Elements
                // beim ersten Element "previous" drücken fängt wieder vom Ende der Liste an.
                favorites.size - 1 // Binäre Minus
            } else {
                currentIndex - 1
            }

            currentStation.nextStation = favorites[nextStationIndex].stationuuid
            currentStation.previousStation = favorites[previousStationIndex].stationuuid

        } else {

            val searchResults = viewModel.allRadios.value!!
            // Mit currentIndex -> "indexOfFirst" wollen wir das nächste Element auslesen.
            val currentIndex = searchResults.indexOfFirst { it.stationuuid == serverid }

            //setzen die jetzige Station auf den jeweiligen Favoriten
            currentStation.fromRadioClass(searchResults[currentIndex])

            //Berechnen den nächsten Index der "stationuuid"
            val nextStationIndex = if (currentIndex == searchResults.size - 1) { // Der Wert des letzten Elemnts
                // beim letzten "next" drücken fängt wieder vom Anfang der Liste an.
                0
            } else {
                currentIndex + 1
            }

            // Lösung mit Modulo Operator -->
//            val nextStationIndex = (currentIndex + 1) % favorites.size // Der Modulo-Operator (%),
            // auch Modulo-Funktion genannt,
            // berechnet den Rest einer Division von zwei Zahlen.
            // Der Ausdruck a % b gibt den Rest zurück, wenn man a durch b teilt.

            val previousStationIndex = if (currentIndex == 0) { // Der Wert des Ersten Elements
                // beim ersten Element "previous" drücken fängt wieder vom Ende der Liste an.
                searchResults.size - 1
            } else {
                currentIndex - 1
            }

            currentStation.nextStation = searchResults[nextStationIndex].stationuuid
            currentStation.previousStation = searchResults[previousStationIndex].stationuuid
        }

        if (currentStation != null) {
            // Mit .find finden wir die Radiostationen in den Favoriten.
            // .find gibt den Wert eines Elements einer Liste zurück wenn er ihn findet, ansonsten "null"
            // .find such anhand einer Kondition in diesem Fall "it.stationuuid == serverid"
            val foundStationInFavorites = viewModel.favRadios.value?.find {
                it.stationuuid == serverid
            } != null

            val isFavorite: Boolean = openingFav or foundStationInFavorites

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

            //Initialisierung des Visualizers
            barVisualizer = view.findViewById(R.id.BarVisualizer)
            lineVisualizer = view.findViewById(R.id.LineVisualizer)
            squareBarVisualizer = view.findViewById(R.id.SquareBarVisualizer)
            circleBarVisualizer = view.findViewById(R.id.CircleBarVisualizer)

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
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                barVisualizer.visibility = View.VISIBLE
                lineVisualizer.visibility = View.GONE
                barVisualizer.apply {
                    isEnabled
                    setColor(requireContext().getColor(R.color.white))
                    setDensity(15F)
                    setPlayer(mediaPlayer!!.audioSessionId)
                }
            }

            binding.visualizerSwitch1ImageDetail.setOnClickListener {
                binding.visualizerSwitch1ImageDetail.visibility = View.GONE
                binding.visualizerSwitch2ImageDetail.visibility = View.VISIBLE
                binding.visualizerSwitch3ImageDetail.visibility = View.GONE
                binding.visualizerSwitch4ImageDetail.visibility = View.GONE
                squareBarVisualizer.visibility = View.GONE
                barVisualizer.visibility = View.GONE
                lineVisualizer.visibility = View.VISIBLE
                lineVisualizer.apply {
                    isEnabled
                    setColor(requireContext().getColor(R.color.white))
                    setStrokeWidth(1)
                    setPlayer(mediaPlayer!!.audioSessionId)
                }
            }

            binding.visualizerSwitch2ImageDetail.setOnClickListener {
                binding.visualizerSwitch1ImageDetail.visibility = View.GONE
                binding.visualizerSwitch2ImageDetail.visibility = View.GONE
                binding.visualizerSwitch3ImageDetail.visibility = View.VISIBLE
                binding.visualizerSwitch4ImageDetail.visibility = View.GONE
                squareBarVisualizer.visibility = View.VISIBLE
                barVisualizer.visibility = View.GONE
                lineVisualizer.visibility = View.GONE
                squareBarVisualizer.apply {
                    isEnabled
                    setColor(requireContext().getColor(R.color.white))
                    setDensity(90F)
                    setGap(3)
                    setPlayer(mediaPlayer!!.audioSessionId)
                }
            }

            binding.visualizerSwitch3ImageDetail.setOnClickListener {
                binding.visualizerSwitch1ImageDetail.visibility = View.GONE
                binding.visualizerSwitch2ImageDetail.visibility = View.GONE
                binding.visualizerSwitch3ImageDetail.visibility = View.GONE
                binding.visualizerSwitch4ImageDetail.visibility = View.VISIBLE
                binding.visualizerSwitchOFFImageDetail.visibility = View.GONE
                barVisualizer.visibility = View.GONE
                lineVisualizer.visibility = View.GONE
                squareBarVisualizer.visibility = View.GONE
                circleBarVisualizer.visibility = View.VISIBLE
                circleBarVisualizer.apply {
                    isEnabled
                    setColor(requireContext().getColor(R.color.white))
                    setPlayer(mediaPlayer!!.audioSessionId)
                }

            }

            binding.visualizerSwitch4ImageDetail.setOnClickListener {
                binding.visualizerSwitch1ImageDetail.visibility = View.GONE
                binding.visualizerSwitch2ImageDetail.visibility = View.GONE
                binding.visualizerSwitch3ImageDetail.visibility = View.GONE
                binding.visualizerSwitch4ImageDetail.visibility = View.GONE
                binding.visualizerSwitchOFFImageDetail.visibility = View.VISIBLE
                barVisualizer.visibility = View.GONE
                lineVisualizer.visibility = View.GONE
                squareBarVisualizer.visibility = View.GONE
                circleBarVisualizer.visibility = View.GONE
            }

            binding.visualizerSwitchOFFImageDetail.setOnClickListener {
                binding.visualizerSwitch1ImageDetail.visibility = View.VISIBLE
                binding.visualizerSwitch2ImageDetail.visibility = View.GONE
                binding.visualizerSwitch3ImageDetail.visibility = View.GONE
                binding.visualizerSwitchOFFImageDetail.visibility = View.GONE
                barVisualizer.visibility = View.VISIBLE
                lineVisualizer.visibility = View.GONE
                squareBarVisualizer.visibility = View.GONE
                barVisualizer.apply {
                    isEnabled
                    setColor(requireContext().getColor(R.color.white))
                    setDensity(15F)
                    setPlayer(mediaPlayer!!.audioSessionId)
                }

            }

            binding.stopImageDetail.setOnClickListener {
                binding.stopImageDetail.visibility = View.GONE
                binding.playImageDetail.visibility = View.VISIBLE
                mediaPlayer!!.pause()
            }
            binding.skipNextImageDetail.setOnClickListener {
                findNavController().navigate(
                    DetailFragmentDirections.actionDetailFragmentSelf(currentStation.nextStation,openingFav = openingFav)
                    //<<< openingFav Argument wird benötigt um von der RadioClass zu unterscheiden >>>
                )
            }
            binding.skipPreviousImageDetail.setOnClickListener {
                findNavController().navigate(
                    DetailFragmentDirections.actionDetailFragmentSelf(currentStation.previousStation, openingFav = openingFav)
                )
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

            //Das öffnen und schließen der Informationen in der Detailansicht (Dialog Fenster)
            binding.informationImageDetail.setOnClickListener {
                binding.informationDialogDetail.visibility = View.VISIBLE
                binding.okButtonDialog.setOnClickListener {
                    binding.informationDialogDetail.visibility = View.GONE
                }
            }
            binding.homeImageDetail.setOnClickListener {
                findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToHomeFragment())
            }

            // Damit die VolumeSeekbar die Laustärke regulieren kann definieren wir hier,
            // die Maximale und die aktuelle Lautstärke.
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            binding.volumeSeekBar.max = maxVolume
            binding.volumeSeekBar.progress = curVolume

            binding.volumeSeekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
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
//            setDensity(15F)
//                    setStrokeWidth(1)
            setPlayer(mediaPlayer!!.audioSessionId)
        }
    }
}

//TODO Durch Blidschirmtimeout verursachten Soundstop fixen. Lösung suchen!


