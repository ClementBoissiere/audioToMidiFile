package com.purplerock.audiotomidifile.audio

import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher
import be.hogent.tarsos.dsp.pitch.PitchProcessor

object AudioThreadService {

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
        audioThread = Thread(dispatcher, "Audio Thread")
        audioThread.start()
    }

    fun stopAudioProcessing() {
        dispatcher.stop()
        audioThread.join()
    }
}