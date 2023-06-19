package com.example.radiosharp.ui

import android.Manifest
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedImageDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.radiosharp.model.IRadio

/**
 *
 */
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
        //"fromRadio" Falls wir aus der RadioClass kommen
        fun fromRadio(radio: IRadio?) {
            if (radio != null) {
                stationuuid = radio.stationuuid
                country = radio.country
                name = radio.name
                radioUrl = radio.radioUrl
                favicon = radio.favicon
                tags = radio.tags
                nextStation = radio.nextStation
                previousStation = radio.previousStation
            }

        }
    }

    // Hier erstellen wir ein leeres Objekt um es später zu füllen.
    private var currentStation: Radio = Radio()

    private lateinit var audioManager: AudioManager
    private lateinit var lineVisualizer: LineVisualizer
    private lateinit var barVisualizer: BarVisualizer
    private lateinit var squareBarVisualizer: SquareBarVisualizer
    private lateinit var circleBarVisualizer: CircleBarVisualizer
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: WakeLock
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiLock: WifiLock

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

    //============================================================================= * * * *
    //    - DetailFragment: "Skip & Previous Funktion, Argumentenübergabe für Navigation -
    //             - erstellen des MediaPlayers, Visualizers & der SeekBar" -
    //============================================================================= * * * *

    // TODO: App im Hintergrund laufen lassen, um Timeout problem zu beheben. (Lifecycle)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Das instanziieren des "powerManagers" dient dazu den mediaPlayer nicht in den ruhestand zu versetzten
        powerManager = requireActivity().getSystemService(POWER_SERVICE) as PowerManager

        // Mit "wakelock" schließen wir den aktuellen Zustand mit einer funktion des PowerManagers zu.
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"radiosharp:wakelockv1")

        // Das instanziieren des WifiManager dient dazu der WIFI Verbindung nicht in den Ruhestand zu versetzten
        // und eine Kontinuirliche Verbindungsaktivität zu gewährleisten.
        wifiManager = requireActivity().getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Mit "wifiLock" schließen wir den voreingestellten Zustand "WIFI_MODE_FULL_HIGH_PERF" ab.
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "radiosharp:wifilockv1")

        // Das definieren des Audio Managers um den Sound in der SeekBar zu regulieren
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Um die Argumete zu übergeben, speichern wir den Station-key in der variable serverid
        val serverid = requireArguments().getString("stationuuid")

        //Wir haben ein Boolean Argument erstellt um die Klassen voneinander schließen zu können woher das Radio kommt.
        //"FavClass oder RadioClass" -> OpeningFav Gibt an ob wir aus der Favoritenliste kommen.
        //Favoriten müssen aus der FavoritenTabelle geladen werden, nicht bedingungslos aus der "RadioClass"
        //  ---->   Grund: eine neue Suche kann einen Favoriten aus der RadioClass löschen
        val openingFav = requireArguments().getBoolean("openingFav")

        //Wenn wir aus dem Fragment kommen unterscheiden wir durch die "if" Abfrage in der Variable "radios":
        // "FavClass" und "RadioClass" voneinander, bzw. wählen eines aus.
        // Dadurch das wir "FavClass" und "RadioClass" die zusammendfassen wird es automatisch zum ---> Interface "IRadio"
        val radios = (if (openingFav) { viewModel.favRadios } else { viewModel.allRadios }).value!!

        // mit .indexOfFirst suchen wir den aktuellen Index durch die stationuuid.
        // Nachfolger wird hier berechnet.
        val currentRadioIndex = radios.indexOfFirst{ it.stationuuid == serverid } // .indexOfFirst erkennt wo er den Index findet,
        // er returned den aktuellen Index
        //Hier berechnen wir in der Playlist das nächste Radio.
        val nextStationIndex = if (currentRadioIndex == radios.size - 1) {
            // Der Wert des letzten Elements
            // beim letzten "next" drücken fängt wieder vom Anfang der Liste an.
            0
        } else {
            currentRadioIndex + 1
        }
        // Der Wert des Ersten Elements
        // beim ersten Element "previous" drücken fängt wieder vom Ende der Liste an.
        val previousStationIndex = if (currentRadioIndex == 0) {
            radios.lastIndex // Binäre Minus
        } else {
            currentRadioIndex - 1
        }
        //mit .fromRadio geben wir der currentStation der Oberklasse-"Radio" die aktuelle Unterklasse und den jeweiligen Index mit.
        currentStation.fromRadio(radios[currentRadioIndex])
        currentStation.nextStation = radios[nextStationIndex].stationuuid
        currentStation.previousStation = radios[previousStationIndex].stationuuid

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

        val onInfoListener = MediaPlayer.OnInfoListener { mp, what, extra ->
            when(what) {
                MediaPlayer.MEDIA_INFO_STARTED_AS_NEXT -> {
                    Log.d("MediaPlayerOnInfo","Nächster Stream gestartet")
                }
            }
            return@OnInfoListener true
        }

        mediaPlayer!!.setDataSource(requireContext(), uri.toUri())
        mediaPlayer!!.setWakeMode(requireContext(),PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer!!.prepareAsync()// .prepareAsync() bereitet die Mediendatei asynchron vor, was bedeutet,
        // dass das Abspielen bereits beginnen kann, während die Datei noch geladen wird.

        //OnPreparedListener wird ausgelöst um das Abspielen tatsächlich zu starten.
        mediaPlayer!!.setOnPreparedListener {
            binding.progressBarDetail.visibility = View.GONE
            binding.playImageDetail.visibility = View.VISIBLE

            binding.playImageDetail.setOnClickListener {
                wifiLock.acquire()
                Log.d("WIFILOCK","WIFILOCK ${wifiLock.isHeld}")
                wakeLock.acquire(60*60*1000L /*60 minutes*/)
                Log.d("WAKELOCK","WAKELOCK: ${wakeLock.isHeld}")
                play()

                //.setOnCompletionListener muss nach Wiedergabe des Mediaplayers verwendet werden
                // um zur Überwachung des Abschlusses eines Medienbezogenen Inhaltes zu registrieren.
//                mediaPlayer!!.setOnCompletionListener{
//                    mediaPlayer!!.release()
//                }
//                mediaPlayer!!.setScreenOnWhilePlaying(true) // .setScreenOnWhilePlaying kann nur
//                mit SurfaceView aktiviert werden ansonsten hat es keine Auswirkungen auf den mediaplayer!!
                binding.playImageDetail.visibility = View.GONE
                binding.stopImageDetail.visibility = View.VISIBLE
            }
        }


        // TODO: Audio Encodierung bezüglich des Error Stream -Timout -Problems
//        val mimeType = "audio/mp4a-latm"
//        val codec = MediaCodec.createEncoderByType(mimeType)
//        val testBitrate = 128000
//
//        val extractor = MediaExtractor()
//        extractor.setDataSource(uri)
//
//        val trackFormat = currentStation.currentIndex?.let { extractor.getTrackFormat(it) }
//        val sampleRate = trackFormat?.getInteger(MediaFormat.KEY_SAMPLE_RATE)
//        val channelCount = trackFormat?.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
//        val mediaFormat = MediaFormat.createAudioFormat(mimeType, sampleRate?:0, channelCount?:0)
//
//        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE ,testBitrate)
//        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
//        codec.configure(mediaFormat,null,null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//        codec.start()
//
//        val inputBuffers = codec.inputBuffers
//        val timeoutUS = 10000000 // 10 Sekunden
//        val inputBufferIndex = codec.dequeueInputBuffer(timeoutUS.toLong())
//        val inputBuffer = inputBuffers[inputBufferIndex]
//        inputBuffer.clear()
////        inputBuffer.put()
//
//        mediaPlayer!!.setOnInfoListener(onInfoListener)

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
//            stopPlaying()
            wifiLock.release()
            wakeLock.release()
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
        // Implementierung der remove & add Funktionen an dem Favoriten Symbol
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

    // Um Abstürze beim drücken vom Stop des Tracks zu beseitigen definieren wir hier eine Funktion
    // die den Mediaplayer neu Ladet, stoppt & weiterspielen lässt wenn es nicht "null" ist.
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

    private fun play(){
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
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
    // durch "ToggleFav" kontrollieren wir die Sichtbarkeit des Favoriten Elementes.
    fun toggleFav(on: Boolean) {
        if (on) {
            binding.favOnImageDetail.visibility = View.VISIBLE
            binding.favOffImageDetail.visibility = View.GONE
        } else {
            binding.favOffImageDetail.visibility = View.VISIBLE
            binding.favOnImageDetail.visibility = View.GONE
        }
    }

}



//TODO Durch Blidschirmtimeout verursachten Soundstop fixen. Lösung suchen!

//===============================================================================================================
// -------------- eine weitere Methode zur Lösung für die Funktion "skip & privious" --------------
//===============================================================================================================

//
//        if (openingFav) {
//            //Wir kommen aus der Favoritenliste
//
//            val favorites = viewModel.favRadios.value!!
//            // Mit currentIndex -> "indexOfFirst" wollen wir das nächste Element auslesen.
//            val currentIndex = favorites.indexOfFirst { it.stationuuid == serverid }
//
//            //setzen die jetzige Station auf den jeweiligen Favoriten
//            currentStation.fromRadio(favorites[currentIndex])
//
//            //Berechnen den nächsten Index der "stationuuid"
//
//        } else {
//
//            val searchResults = viewModel.allRadios.value!!
//            // Mit currentIndex -> "indexOfFirst" wollen wir das nächste Element auslesen.
//            val currentIndex = searchResults.indexOfFirst { it.stationuuid == serverid }
//
//            //setzen die jetzige Station auf den jeweiligen Favoriten
//            currentStation.fromRadio(searchResults[currentIndex])
//
//            //Berechnen den nächsten Index der "stationuuid"
//            val nextStationIndex = if (currentIndex == searchResults.size - 1) { // Der Wert des letzten Elemnts
//                // beim letzten "next" drücken fängt wieder vom Anfang der Liste an.
//                0
//            } else {
//                currentIndex + 1
//            }

//            // Lösung mit Modulo Operator -->
////            val nextStationIndex = (currentIndex + 1) % favorites.size // Der Modulo-Operator (%),
//            // auch Modulo-Funktion genannt,
//            // berechnet den Rest einer Division von zwei Zahlen.
//            // Der Ausdruck a % b gibt den Rest zurück, wenn man a durch b teilt.
//
//            val previousStationIndex = if (currentIndex == 0) { // Der Wert des Ersten Elements
//                // beim ersten Element "previous" drücken fängt wieder vom Ende der Liste an.
//                searchResults.size - 1
//            } else {
//                currentIndex - 1
//            }
//
//            currentStation.nextStation = searchResults[nextStationIndex].stationuuid
//            currentStation.previousStation = searchResults[previousStationIndex].stationuuid
//        }


