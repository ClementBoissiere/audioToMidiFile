package com.purplerock.audiotomidifile.audio

import be.hogent.tarsos.dsp.AudioEvent
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult

class PitchDetectionHandlerImpl(private val visualizer: AudioVisualizer) : PitchDetectionHandler {
    override fun handlePitch(result: PitchDetectionResult?, e: AudioEvent?) {
        val pitchInHz = result?.pitch ?: -1.0f
        val noteValue = AudioAnalyzer.convertFrequencyToMidiNoteNumber(pitchInHz)
        visualizer.updateNoteValue(noteValue)
    }
}