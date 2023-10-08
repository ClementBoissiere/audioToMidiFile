package com.purplerock.audiotomidifile.audio

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher
import be.hogent.tarsos.dsp.pitch.PitchProcessor
import com.leff.midi.MidiTrack
import com.purplerock.audiotomidifile.midi.MidiGeneratorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object AudioThreadService : ViewModel() {

    private val sampleRate = 44100
    private val sampleRateFloat = 44100F
    private val bufferElement = 2048
    private lateinit var dispatcher: MicrophoneAudioDispatcher
    private var dateDebut = System.currentTimeMillis()
    private var dateFin = System.currentTimeMillis()
    private lateinit var midiTrack: MidiTrack

    fun startAudioProcessing(visualizer: AudioVisualizer) {
        dispatcher = MicrophoneAudioDispatcher(sampleRate, bufferElement, 0)
        midiTrack = midiService().setupRecord()
        val noteBuffer = LinkedHashMap<Long, Int>(3)
        val pdh = PitchDetectionHandlerImpl(visualizer, midiTrack, midiService(), noteBuffer)

        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
            sampleRateFloat,
            bufferElement,
            pdh
        )

        dispatcher.addAudioProcessor(pitchProcessor)
        viewModelScope.launch(Dispatchers.IO) {
            dateDebut = System.currentTimeMillis()
            dispatcher.run()
        }
    }

    fun stopAudioProcessing(filesDir: File) {
        dispatcher.stop()
        midiService().writeMidiNote(midiTrack)
        midiService().setupMidiFile(midiTrack, filesDir)
        midiService().resetValue()
        dateFin = System.currentTimeMillis()
        Log.d("FIN ANALYSE", "temps passé : " + (dateFin - dateDebut) / 1000.0)
    }

    fun midiService(): MidiGeneratorService {
        return MidiGeneratorService.getInstance()
    }
}