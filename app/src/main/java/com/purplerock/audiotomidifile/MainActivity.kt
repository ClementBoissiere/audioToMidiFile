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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.purplerock.audiotomidifile.audio.AudioTOMIDIVisualizer
import com.purplerock.audiotomidifile.audio.AudioThreadService
import com.purplerock.audiotomidifile.handler.PermissionHandler.Permissions.addPermissions
import com.purplerock.audiotomidifile.ui.theme.AudioToMIDIFIleTheme


class MainActivity : ComponentActivity() {


    private var timer: Long = 0
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
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        ConstraintLayout(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxSize()
        ) {
            val (pianoRollBox, notePanelBox, buttonBox) = createRefs()

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.1f)
                    .constrainAs(pianoRollBox) {}) {
                PianoRoll(screenWidth, screenHeight)
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f)
                    .constrainAs(notePanelBox) {
                        start.linkTo(pianoRollBox.absoluteRight)
                    }) {
                AffichageNote(screenHeight)
            }
            Box(modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.1f)
                .constrainAs(buttonBox) {
                    start.linkTo(notePanelBox.absoluteRight)
                }) {
                RecordButton()
            }
        }
    }

    @Composable
    private fun PianoRoll(screenWidth: Dp, screenHeight: Dp) {
        //l'écran est à l'horizontale donc on inverse width et height
        val noteWidth = screenWidth / 10
        val noteHeight = screenHeight / 12
        val blackNoteIndex = setOf(1, 3, 6, 8, 10) //index des notes #
        Column {
            setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).forEachIndexed { index, _ ->
                //Log.d("UI", "JE SUIS LE PIANO ROLL")
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
    fun RecordButton() {
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
    private fun AffichageNote(screenHeight: Dp) {
        val height = screenHeight / 12

        val listNote = remember { mutableStateListOf<String>() }
        this.audioTOMIDIVisualizer.note.observe(this) { newValue ->
            // Mettre à jour l'UI lorsque le LiveData change
            listNote.add(newValue)
            Log.d("UI", "JE SUIS UNE NOUVELLE NOTE")
            Log.d("UI", listNote.size.toString())
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .animateContentSize()
        ) {
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    Log.d("UI", "JE SUIS LE LAZYROW")
                    DrawNote(note, "C")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "C#")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "D")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "D#")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "E")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "F")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "F#")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "G")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "G#")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "A")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "A#")
                }
            }
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                items(listNote) { note ->
                    DrawNote(note, "B")
                }
            }
        }
    }

    @Composable
    private fun DrawNote(note: String, noteColumn: String) {
        Log.d("UI", "JE DRAW")
        if (noteColumn == note.dropLast(1)) {
            Box(
                modifier = Modifier
                    .width(calculateWidth())
                    .fillMaxHeight()
                    .background(Color(0xE4F4A836))
            )
        } else {
            Box(
                modifier = Modifier
                    .width(calculateWidth())
                    .fillMaxHeight()
                    .background(Color(0xF44336))
            )
        }
    }

    private fun calculateWidth(): Dp {
        val nbMilli = System.currentTimeMillis() - this.timer
        Log.d("UI", "nbMilli : $nbMilli")
        val size = nbMilli / 999999999999f
        Log.d("UI", "SIZE : $size")
        return size.dp
    }


    private fun launchAudioRecorder() {
        this.recordingState.value = true
        this.timer = System.currentTimeMillis();
        AudioThreadService.startAudioProcessing(this.audioTOMIDIVisualizer)
    }

    private fun stopAudioRecorder() {
        this.recordingState.value = false
        this.timer = 0;
        AudioThreadService.stopAudioProcessing(applicationContext.filesDir)
    }
}
