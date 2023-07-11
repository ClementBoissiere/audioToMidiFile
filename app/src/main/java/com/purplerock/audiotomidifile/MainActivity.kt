package com.purplerock.audiotomidifile

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.purplerock.audiotomidifile.audio.AudioRecorder
import com.purplerock.audiotomidifile.handler.PermissionHandler.Permissions.addPermissions
import com.purplerock.audiotomidifile.ui.theme.AudioToMIDIFIleTheme

class MainActivity : ComponentActivity() {
    private val audioRecorder by lazy {
        AudioRecorder(applicationContext, this)
    }

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


    @Composable
    fun AudioToMIDIFilePreview() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MusicButton()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MusicButton() {
        var isLoading = audioRecorder.recordingState.value
        AudioToMIDIFIleTheme {
            Row() {
                Text(text = "Votre note :")
                Text(text = "${audioRecorder.note}")
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
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
                                .size(140.dp)
                                .padding(PaddingValues(bottom = 10.dp))
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
                                .size(140.dp)
                                .padding(PaddingValues(bottom = 10.dp))
                        )
                    }
                }
            }
        }
    }


    private fun launchAudioRecorder() {
        audioRecorder.launchRecord()
    }

    private fun stopAudioRecorder() {
        audioRecorder.stopRecording()
    }
}
