import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor

class AudioAnalyzer {
    companion object Analyzer {
        fun convertFrequencyToMidiNoteNumber(frequency: Float): String? {
            var resultNote: String? = null
            Log.d("ANALYZER","Je passe dans le convertisseur")
            val existsNote = NoteData.notes.find { note -> note.frequency == frequency}
            if (existsNote != null) {
                resultNote = existsNote.name
            } else {
                return resultNote
            }
            return resultNote
        }
    }
}
