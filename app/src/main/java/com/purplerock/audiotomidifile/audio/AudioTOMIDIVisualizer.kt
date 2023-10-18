package com.purplerock.audiotomidifile.audio

import NoteData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AudioTOMIDIVisualizer : ViewModel(), AudioVisualizer {

    val note: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    override fun updateNoteValue(midiNote: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                var noteValue = ""
                noteValue = NoteData.midiNotesToLabel[midiNote] ?: "KO"
                note.postValue(noteValue)
            }
        }
    }

}