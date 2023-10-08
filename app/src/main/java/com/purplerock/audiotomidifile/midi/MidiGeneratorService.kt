package com.purplerock.audiotomidifile.midi

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.leff.midi.MidiFile
import com.leff.midi.MidiTrack
import com.leff.midi.event.NoteOff
import com.leff.midi.event.NoteOn
import com.leff.midi.event.meta.Tempo
import com.leff.midi.event.meta.TimeSignature
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDate


class MidiGeneratorService : Service() {

    var noteTimeSignature = ArrayList<Long>()
    var noteTickSignature = ArrayList<Long>()
    var resultsNote = ArrayList<Int>()

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

    fun setupNoteTrack(): MidiTrack {
        val tempoTrack = MidiTrack()

        // 2. Add events to the tracks
        // Track 0 is the tempo map
        val ts = TimeSignature()
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION)

        //trouver un moyen de calculer le tempo plus tard
        val tempo = Tempo()
        tempo.bpm = 120f

        tempoTrack.insertEvent(ts)
        tempoTrack.insertEvent(tempo)

        return tempoTrack
    }

    fun setupMidiFile(tempoTrack: MidiTrack, filesDir: File): MidiFile {
        // 3. Create a MidiFile with the tracks we created
        val tracks: MutableList<MidiTrack> = ArrayList()
        tracks.add(tempoTrack)

        val midi = MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks)

        val date: LocalDate = LocalDate.now()
        val index = (filesDir.list()?.size ?: 0) + 1
        // 4. Write the MIDI data to a file
        val output = File(filesDir, "file$date($index).mid")
        output.setWritable(true)
        output.setExecutable(true)
        output.setReadable(true)
        try {
            midi.writeToFile(output)
        } catch (e: IOException) {
            System.err.println(e)
        }

        return midi
    }

    fun writeMidiNote(
        noteTrack: MidiTrack,
        timeNoteBuffer: Long = 0
    ) {
        Log.d("MIDI WRITE", "NOTE QUI VIENT DETRE JOUE :${resultsNote.last()}")
        if (timeNoteBuffer == 0L) {
            this.noteTimeSignature.add(System.currentTimeMillis())
        } else {
            this.noteTimeSignature.add(timeNoteBuffer)
        }
        val lastNoteIndex = this.noteTimeSignature.size - 1
        val lastNoteDurationMillisecondLong =
            this.noteTimeSignature[lastNoteIndex] - this.noteTimeSignature[lastNoteIndex - 1]
        Log.d("MIDI WRITE", "SA DUREE: $lastNoteDurationMillisecondLong")
        val lastNoteDurationTick = calculateTickFromMilliseconds(lastNoteDurationMillisecondLong)

        this.noteTickSignature.add(lastNoteDurationTick + this.noteTickSignature.last())

        if (resultsNote.last() == 0) {
            noteTrack.insertEvent(
                NoteOn(
                    this.noteTickSignature[noteTickSignature.size - 2],
                    0,
                    resultsNote.last(),
                    0
                )
            )
            noteTrack.insertEvent(
                NoteOff(
                    this.noteTickSignature[noteTickSignature.size - 1],
                    0,
                    resultsNote.last(),
                    0
                )
            )
        } else {
            noteTrack.insertEvent(
                NoteOn(
                    this.noteTickSignature[noteTickSignature.size - 2],
                    0,
                    resultsNote.last(),
                    80
                )
            )
            noteTrack.insertEvent(
                NoteOff(
                    this.noteTickSignature[noteTickSignature.size - 1],
                    0,
                    resultsNote.last(),
                    0
                )
            )
        }
    }

    private fun calculateTickFromMilliseconds(durationMillis: Long): Long {
        //val ppq: Long = 480 // Exemple de résolution MIDI (PPQ)

        //val bpm: Long = 120 // Exemple de tempo en BPM

        //val quarterNoteDurationMillis = 60000 / bpm

        // Calcul du nombre de noires pour la durée donnée
        //val numberOfQuarterNotes = durationMillis / quarterNoteDurationMillis

        // Calcul du nombre de ticks pour ces noires
        //val ticks = numberOfQuarterNotes * ppq

        val nbtick = BigDecimal((durationMillis / 500.0) * 480.0)
        return nbtick.toLong()
    }

    fun setupRecord(): MidiTrack {
        val midiTrack = setupNoteTrack()
        this.noteTimeSignature.add(System.currentTimeMillis())
        this.noteTickSignature.add(0)
        this.resultsNote.add(0)

        return midiTrack
    }

    fun resetValue() {
        this.noteTimeSignature.clear()
        this.noteTickSignature.clear()
        this.resultsNote.clear()
    }
}