package com.purplerock.audiotomidifile.audio

import AudioAnalyzer
import be.hogent.tarsos.dsp.AudioEvent
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult
import com.leff.midi.MidiTrack
import com.purplerock.audiotomidifile.midi.MidiGeneratorService

class PitchDetectionHandlerImpl(
    private val visualizer: AudioVisualizer,
    private val midiTrack: MidiTrack,
    private val mgs: MidiGeneratorService,
    private val noteBuffer: LinkedHashMap<Long, Int>
) : PitchDetectionHandler {

    override fun handlePitch(result: PitchDetectionResult?, e: AudioEvent?) {
        val pitchInHz = result?.pitch ?: -1.0f
        if (result?.isPitched!!) {
            val midiNoteActual = AudioAnalyzer.convertFrequencyToMidiNoteNumber(pitchInHz)
            if (result.probability > 0.90f) {
                visualizer.updateNoteValue(mgs.resultsNote.last())
                addNoteBuffer(midiNoteActual)
                writeMIDINote(midiNoteActual)
            }
        } else {
            visualizer.updateNoteValue(mgs.resultsNote.last())
            addNoteBuffer(0)
            writeMIDINote(0)
        }
    }

    private fun writeMIDINote(midiNoteActual: Int) {
        if (midiNoteActual != mgs.resultsNote.last()) {
            if (noteBuffer.values.stream().allMatch { value -> value == midiNoteActual }) {
                // on écrit la note qui vient de se terminer
                mgs.resultsNote.add(midiNoteActual)
                mgs.writeMidiNote(midiTrack, noteBuffer.keys.first())
            }
        }
    }

    private fun addNoteBuffer(midiNoteActual: Int) {
        if (noteBuffer.size == 3) {
            val firstElement = noteBuffer.keys.first()
            noteBuffer.remove(firstElement)
        }
        noteBuffer.put(System.currentTimeMillis(), midiNoteActual)
    }
}