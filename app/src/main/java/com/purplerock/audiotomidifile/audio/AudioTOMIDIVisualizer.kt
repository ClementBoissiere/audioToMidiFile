package com.purplerock.audiotomidifile.audio

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AudioTOMIDIVisualizer : ViewModel(), AudioVisualizer {

    private val scope = MainScope()

    val note: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    override fun updateNoteValue(noteValue: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                note.postValue(noteValue)
            }
        }
    }
}