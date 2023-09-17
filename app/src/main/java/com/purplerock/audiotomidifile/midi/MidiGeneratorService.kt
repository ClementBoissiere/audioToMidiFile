package com.purplerock.audiotomidifile.midi

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.leff.midi.MidiFile
import com.leff.midi.MidiTrack
import com.leff.midi.event.meta.Tempo
import com.leff.midi.event.meta.TimeSignature
import java.io.File
import java.io.IOException


class MidiGeneratorService: Service() {
    companion object {
        private var instance: MidiGeneratorService? = null

        fun getInstance(): MidiGeneratorService {
            if (instance == null) {
                instance = MidiGeneratorService()
            }
            return instance!!
        }
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun setupMidiFile() {
        val tempoTrack = MidiTrack()
        val noteTrack = MidiTrack()

        // 2. Add events to the tracks
        // Track 0 is the tempo map
        val ts = TimeSignature()
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION)

        val tempo = Tempo()
        tempo.bpm = 228f

        tempoTrack.insertEvent(ts)
        tempoTrack.insertEvent(tempo)

        // 3. Create a MidiFile with the tracks we created
        val tracks: MutableList<MidiTrack> = ArrayList()
        tracks.add(tempoTrack)
        tracks.add(noteTrack)

        val midi = MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks)

        // 4. Write the MIDI data to a file
        val output = File("file.mid")
        try {
            midi.writeToFile(output)
        } catch (e: IOException) {
            System.err.println(e)
        }
    }
}