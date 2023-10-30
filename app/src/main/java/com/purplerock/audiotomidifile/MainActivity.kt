package com.purplerock.audiotomidifile

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.purplerock.audiotomidifile.audio.AudioTOMIDIVisualizer
import com.purplerock.audiotomidifile.audio.AudioThreadService
import com.purplerock.audiotomidifile.handler.PermissionHandler.Permissions.addPermissions
import com.purplerock.audiotomidifile.model.NoteEnum
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
        val scrollState = rememberScrollState()
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
                    .horizontalScroll(scrollState)
                    .constrainAs(notePanelBox) {
                        start.linkTo(pianoRollBox.absoluteRight)
                    }) {
                DisplayNote(screenHeight)
            }
            Box(modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.1f)
                .constrainAs(buttonBox) {
                    start.linkTo(notePanelBox.absoluteRight)
                }) {
                DisplayButtons()
            }
        }
    }

    @Preview
    @Composable
    private fun DisplayButtons() {
        var isModalVisible by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxSize()
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            RecordButton()
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            ClearButton()
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            ListButton()
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            InfoButton(onInfoButtonClick = {
                isModalVisible = true
            })

            // Modale d'information (Dialog) conditionnelle
            if (isModalVisible) {
                AlertDialog(
                    onDismissRequest = {
                        // Lorsque l'utilisateur ferme la modale, masquer la modale
                        isModalVisible = false
                    },
                    title = { Text("Informations") },
                    text = { Text("Hello. Important things to know : Not displayed can have a little difference with generated MIDI file in term of note length (not about the pitch). Also, default BPM of MIDI FILE is 120 BPM. Have fun!. Finally you can find your file in your phone, in data files in your phone (data/data/com/purplerock/audiotomidiFile") },
                    confirmButton = {
                        Button(
                            onClick = {
                                isModalVisible = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

    @Preview
    @Composable
    fun RecordButton() {
        val isLoading = this.recordingState.value

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

    @Composable
    fun InfoButton(
        onInfoButtonClick: () -> Unit
    ) {
        Button(
            onClick = {
                onInfoButtonClick()
            },
            content = {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = ImageVector.vectorResource(id = R.drawable.outline_info_24),
                    contentDescription = "Record"
                )
            },
            modifier = Modifier
                .size(75.dp)
                .padding(PaddingValues(bottom = 5.dp))
        )
    }

    @Preview
    @Composable
    fun ClearButton() {
        Button(
            onClick = {
                stopAudioRecorder() //TODO:: CLEAN LA ZONE DES NOTES
            },
            content = {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_clear_24),
                    contentDescription = "Clean"
                )
            },
            colors = ButtonDefaults.buttonColors(),
            modifier = Modifier
                .size(75.dp)
                .padding(PaddingValues(bottom = 5.dp))
        )
    }

    @Preview
    @Composable
    fun ListButton() {
        Button(
            onClick = {
                stopAudioRecorder()
            },
            content = {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_view_list_24),
                    contentDescription = "List"
                )
            },
            colors = ButtonDefaults.buttonColors(),
            modifier = Modifier
                .size(75.dp)
                .padding(PaddingValues(bottom = 5.dp))
        )
    }

    @Composable
    private fun PianoRoll(screenWidth: Dp, screenHeight: Dp) {
        //l'écran est à l'horizontale donc on inverse width et height
        val noteWidth = screenWidth / 10
        val noteHeight = screenHeight / 12
        val blackNoteIndex = setOf(1, 3, 5, 8, 10) //index des notes #
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


    @OptIn(ExperimentalTextApi::class)
    @Composable
    private fun DisplayNote(screenHeight: Dp) {

        val textMeasurer = rememberTextMeasurer()
        val pixelOffsetY = with(LocalDensity.current) {
            (screenHeight / 12) * density
        }
        var lastNote by remember { mutableStateOf(NoteEnum.KO) }
        var actualNote by remember { mutableStateOf("") }
        var lastNoteDropLast by remember { mutableStateOf("") }
        val listNote = remember { mutableStateListOf<NoteEnum>() }
        val listNoteLabel = remember { mutableStateListOf<String>() }
        val width by remember { mutableStateOf(750.dp) }
        this.audioTOMIDIVisualizer.note.observe(this) { newValue ->
            // Mettre à jour l'UI lorsque le LiveData change
            listNoteLabel.add(newValue)
            listNote.add(NoteEnum.valueOf(newValue.dropLast(1)))
            actualNote = newValue
            lastNote = listNote[listNote.size - 1]
            lastNoteDropLast = lastNote.name.dropLast(1)

            /*            if ((((listNote.size + 1) * 5) / densityScreen) > width.value) {
                            width += 100.dp
                            Log.d("UI CALCUL", ((((listNote.size + 1) * 5) / densityScreen).toString()))
                            Log.d("UI", "width$width")
                        }*/
        }
        Canvas(
            Modifier
                .width(width)
                .fillMaxHeight()
                .background(Color(0xC8FAEEE6))
        ) {
            listNote.forEachIndexed() { index, note ->
                if (NoteEnum.KO !== note) {
                    drawRect(
                        color = Color(0xE8E98438),
                        topLeft = Offset(index * 5f, pixelOffsetY.value * note.ordinal),
                        size = Size(5f, pixelOffsetY.value)
                    )
                }
                if (index > 0 && listNote[index] != listNote[index - 1]) {
                    val measuredText = textMeasurer.measure(
                        AnnotatedString(listNoteLabel[index - 1]),
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontSize = 13.sp)
                    )
                    drawText(
                        measuredText,
                        color = Color(0xE8C9712F),
                        topLeft = Offset(
                            (index + 1) * 5f,
                            pixelOffsetY.value * listNote[index - 1].ordinal
                        )
                    )
                }
            }
        }
    }


    private fun launchAudioRecorder() {
        this.recordingState.value = true
        this.timer = System.currentTimeMillis()
        AudioThreadService.startAudioProcessing(this.audioTOMIDIVisualizer)
    }

    private fun stopAudioRecorder() {
        this.recordingState.value = false
        this.timer = 0
        AudioThreadService.stopAudioProcessing(applicationContext.filesDir)
    }
}
