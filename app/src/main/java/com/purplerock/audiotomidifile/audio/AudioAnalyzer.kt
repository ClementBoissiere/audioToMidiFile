import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor

class AudioAnalyzer {
    companion object Analyzer {
        fun convertFrequencyToMidiNoteNumber(frequency: Float): String {
            val closestFrequency = NoteData.frequenciesToNotes.keys.minByOrNull {
                Math.abs(it - frequency)
            } ?: return "" // Si le tableau est vide, retourne une chaîne vide

            Log.d( "AUDIO ANALYZER  : ", NoteData.frequenciesToNotes[closestFrequency] ?: "")
            return NoteData.frequenciesToNotes[closestFrequency] ?: ""
        }
    }
}
