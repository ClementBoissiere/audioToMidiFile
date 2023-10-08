package com.purplerock.audiotomidifile.audio

import AudioAnalyzer
import android.util.Log
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

    /* private val noteBuffer = CircularBuffer(5);
    override fun handlePitch(result: PitchDetectionResult?, e: AudioEvent?) {
        val pitchInHz = result?.pitch ?: -1.0f
        Log.d(
            "PITCH EVENT",
            "heure : " +  System.currentTimeMillis()
                .toString() + " | isPitched:  " + result?.isPitched + " | result: " + result?.pitch + " | proba: " + result?.probability
        )
        if(result?.isPitched!!) {
            val midiNoteActual = AudioAnalyzer.convertFrequencyToMidiNoteNumber(pitchInHz)
            if (result.probability > 0.90f) {
                noteBuffer.add(Pair(midiNoteActual, System.currentTimeMillis()))
                if (midiNoteActual != mgs.resultsNote.last()) {
                    if (noteBuffer.checkForKey(midiNoteActual)) {
                        // on écrit la note qui vient de se terminer
                        mgs.resultsNote.add(midiNoteActual)
                        mgs.writeMidiNote(midiTrack, noteBuffer.get(0).second)
                    }
                }
            }
        } else {
            noteBuffer.add(Pair(0, System.currentTimeMillis()))
            if (0 != mgs.resultsNote.last() && 0 != mgs.resultsNote.last()) {
                if (noteBuffer.checkForKey(0)) {
                    // on écrit la note qui vient de se terminer
                    mgs.resultsNote.add(0)
                    mgs.writeMidiNote(midiTrack, noteBuffer.get(0).second)
                }
            }
        }

        visualizer.updateNoteValue(mgs.resultsNote.last())
    }
    */

    override fun handlePitch(result: PitchDetectionResult?, e: AudioEvent?) {
        val pitchInHz = result?.pitch ?: -1.0f
        Log.d(
            "PITCH EVENT",
            "heure : " + System.currentTimeMillis()
                .toString() + " | isPitched:  " + result?.isPitched + " | result: " + result?.pitch + " | proba: " + result?.probability + " | BufferSize: " + noteBuffer.size
        )
        if (result?.isPitched!!) {
            val midiNoteActual = AudioAnalyzer.convertFrequencyToMidiNoteNumber(pitchInHz)
            if (result.probability > 0.90f) {
                addNoteBuffer(midiNoteActual)
                writeMIDINote(midiNoteActual)
            }
        } else {
            addNoteBuffer(0)
            writeMIDINote(0)
        }

        visualizer.updateNoteValue(mgs.resultsNote.last())
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