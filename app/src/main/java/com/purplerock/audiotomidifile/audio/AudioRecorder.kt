package com.purplerock.audiotomidifile.audio

import AudioAnalyzer
import android.Manifest
import android.app.Activity
import android.content.Context
import android.media.MediaRecorder
import android.media.MicrophoneInfo
import android.os.Build
import android.util.Log
import android.util.Pair
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purplerock.audiotomidifile.handler.PermissionHandler
import kotlinx.coroutines.launch
import java.io.File


class AudioRecorder(
    private val context: Context,
    private val activity: Activity
    ) : ViewModel() {

    private var recorder: MediaRecorder? = null
    var recordingState = mutableStateOf(false)
    var note = mutableListOf<String?>()
    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    private fun startRecording() {
        if (!PermissionHandler.checkPermission(activity, Manifest.permission.RECORD_AUDIO)) {
            Log.e("RECORDER", "PAS DE PERMISSION")
            return;
        }
        viewModelScope.launch {
            createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                setOutputFile(File.createTempFile("test", "ogg"))
                prepare()
                start()
                Log.d("RECORDER","DEBUT")
                activeMicrophones.stream().forEachOrdered { infos -> infos.frequencyResponse.stream().forEachOrdered { frequency -> note.add(AudioAnalyzer.convertFrequencyToMidiNoteNumber(frequency.first))}}
                recordingState.value = true

                recorder = this
            }

        }
    }

    fun stopRecording() {
        recorder?.stop()
        recorder?.reset()
        recordingState.value = false
        Log.d("USER ACTION","STOP RECORD")
    }

    fun launchRecord() {
        if (!recordingState.value) {
            Log.d("USER ACTION","START RECORD")
            recordingState.value = true
            startRecording()
        }
    }

//    private fun startRecording() {
//        viewModelScope.launch {
//           delay(2000)
//            recordingState.value = false
//        }
//    }
}