package com.purplerock.audiotomidifile.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher
import be.hogent.tarsos.dsp.pitch.PitchProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AudioThreadService : ViewModel() {

    private val sampleRate = 44100
    private val sampleRateFloat = 44100F
    private val bufferElement = 1024
    private var dispatcher: MicrophoneAudioDispatcher =
        MicrophoneAudioDispatcher(sampleRate, bufferElement, 0)

    private lateinit var audioThread: Thread

    fun startAudioProcessing(visualizer: AudioVisualizer) {
        val pdh = PitchDetectionHandlerImpl(visualizer)

        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
            sampleRateFloat,
            bufferElement,
            pdh
        )

        dispatcher.addAudioProcessor(pitchProcessor)
        viewModelScope.launch(Dispatchers.IO) {
            dispatcher.run()
        }
    }

    fun stopAudioProcessing() {
        dispatcher.stop()
    }
}