package com.purplerock.audiotomidifile

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purplerock.audiotomidifile.audio.AudioTOMIDIVisualizer
import com.purplerock.audiotomidifile.audio.AudioThreadService
import com.purplerock.audiotomidifile.handler.PermissionHandler.Permissions.addPermissions
import com.purplerock.audiotomidifile.ui.theme.AudioToMIDIFIleTheme
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

class MainActivity : ComponentActivity() {


    private val audioTOMIDIVisualizer: AudioTOMIDIVisualizer by viewModels()

    var recordingState = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO))
        setContent {
            AudioToMIDIFIleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AudioToMIDIFilePreview()
                }
            }
        }
    }

    @Preview
    @Composable
    fun AudioToMIDIFilePreview() {
        Column(modifier = Modifier.fillMaxSize().fillMaxHeight()) {
            Box(modifier = Modifier.fillMaxSize().fillMaxHeight()) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(Color.Red)
                ) // 10% de l'écran à gauche)
                {
                    PianoRoll()
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth() // 90% de l'écran à gauche
                        .background(Color.Yellow)
                ) {
                    //AffichageNote()
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight()
                    .background(Color.Green)
            ) {
                MusicButton()
            }
        }
    }

    @Preview
    @Composable
    private fun PianoRoll() {
        //l'écran est à l'horizontale donc on inverse width et height
        val noteWidth = LocalConfiguration.current.screenWidthDp.dp / 12
        val noteHeight = LocalConfiguration.current.screenHeightDp.dp / 12
        val blackNoteIndex = listOf(1, 3, 6, 8, 10) //index des notes #
        LazyColumn {
            itemsIndexed(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)) { index, _ ->
                Log.d("UI", "JE SUIS LE PIANO ROLL")
                val backgroundColor = if (blackNoteIndex.contains(index)) {
                    Color.Black
                } else {
                    Color.White
                }

                Box(
                    modifier = Modifier
                        .width(noteWidth)
                        .height(noteHeight)
                        .border(border = BorderStroke(1.dp, Color.Black))
                        .background(backgroundColor)
                )
            }
        }
    }

    @Preview
    @Composable
    fun MusicButton() {
        val isLoading = this.recordingState.value

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Column(verticalArrangement = Arrangement.Center) {
                if (isLoading) {
                    Button(
                        onClick = {
                            stopAudioRecorder()
                        },
                        content = {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_mic_24),
                                contentDescription = "Record"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier
                            .size(75.dp)
                            .padding(PaddingValues(bottom = 5.dp))
                    )
                } else {
                    Button(
                        onClick = {
                            launchAudioRecorder()
                        },
                        content = {
                            Icon(
                                modifier = Modifier.fillMaxSize(),
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_mic_none_24),
                                contentDescription = "Record"
                            )
                        },
                        modifier = Modifier
                            .size(75.dp)
                            .padding(PaddingValues(bottom = 5.dp))
                    )
                }
            }
        }
    }

    @Composable
    private fun AffichageNote() {
        val isLoading = this.recordingState.value
        val scrollState = rememberScrollState()
        var progress by remember { mutableStateOf(0.0f) }
        var noteLabel by remember {
            mutableStateOf("")
        }

        this.audioTOMIDIVisualizer.note.observe(this) { newValue ->
            // Mettre à jour l'UI lorsque le LiveData change
            noteLabel = newValue
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color(0x90F44336))
                .fillMaxWidth()
                .animateContentSize()
        )
    }


    private fun launchAudioRecorder() {
        this.recordingState.value = true
        AudioThreadService.startAudioProcessing(this.audioTOMIDIVisualizer)
    }

    private fun stopAudioRecorder() {
        this.recordingState.value = false
        AudioThreadService.stopAudioProcessing(applicationContext.filesDir)
    }
}
