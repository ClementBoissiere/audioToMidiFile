package com.purplerock.audiotomidifile.audio

import androidx.lifecycle.MutableLiveData

object AudioTOMIDIVisualizer {

    val note: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun updateNoteValue(newValue: String) {
        note.value = newValue
    }
}