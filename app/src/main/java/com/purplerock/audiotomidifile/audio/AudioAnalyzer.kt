class AudioAnalyzer {
    companion object Analyzer {
        fun convertFrequencyToMidiNoteNumber(frequency: Float): Int {
            val closestFrequency = NoteData.frequeciesToMIDI.keys.minByOrNull {
                Math.abs(it - frequency)
            } ?: return 0 // Si le tableau est vide, retourne une chaîne vide

            return NoteData.frequeciesToMIDI[closestFrequency] ?: 0
        }
    }
}
