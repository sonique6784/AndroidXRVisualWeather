package fr.sonique.mygeminiapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import androidx.xr.compose.platform.LocalSpatialCapabilities


@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel = viewModel(),
    onStartListening: () -> Unit = {},
    onStopListening: () -> Unit = {},
    onSendTextPrompt: (text: String) -> Unit = {},
    onXRFullScreen: () -> Unit = {}
) {
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf("") }
    var listening by rememberSaveable { mutableStateOf(false) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by weatherViewModel.uiState.collectAsState()

    Box {
        // Check if XR is available on the device
        if (LocalHasXrSpatialFeature.current) {
            // TODO move to bottom right corner
            FullSpaceModeIconButton({
                onXRFullScreen()
            }, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.screen_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    weatherViewModel.registerTapOnImage()
                }
        )

        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // show prompt + go button
            if (uiState is UiState.DebugMode) {
                TextField(
                    value = prompt,
                    label = { Text(stringResource(R.string.label_prompt)) },
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            RecordButton(listening, onClick = {
                if (listening) {
                    onStopListening()
                } else {
                    onStartListening()
                }
                listening = !listening
            })
        }

        if (uiState is UiState.DebugMode) {
            Button(
                onClick = {
                    // - "is it sunny in Nancy?"
                    // - "What is the weather in Paris?"
                    // - "How's the weather in Sydney?
                    // - "What's the weather like in Byron Bay?"
                    onSendTextPrompt(prompt)
                },
                enabled = prompt.isNotEmpty(),
                modifier = Modifier
            ) {
                Text(text = stringResource(R.string.action_go))
            }
        }


        if (uiState is UiState.Loading) {
            CircularProgressIndicator()
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            if (uiState is UiState.Error) {
                textColor = MaterialTheme.colorScheme.error
                result = (uiState as UiState.Error).errorMessage
            } else if (uiState is UiState.Success) {
                textColor = MaterialTheme.colorScheme.onSurface
                result = (uiState as UiState.Success).outputText
            }

            if (!LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                (uiState as? UiState.SuccessBitmap)?.let {
                    Image(
                        it.outputBitmap.asImageBitmap(),
                        contentDescription = "Image generated",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Text(
                text = result,
                textAlign = TextAlign.Start,
                color = textColor,
                modifier = Modifier
                    .padding(16.dp)
            )
        }

    }
}


@Composable
fun RecordButton(isRecording: Boolean, onClick: () -> Unit) {
    val colors = if(isRecording) {
        ButtonDefaults.buttonColors(containerColor = Color.Red)
    } else {
        ButtonDefaults.buttonColors()
    }

    Button(onClick, colors = colors) {
        Row {
            Icon(
                if (isRecording) {
                    Icons.Rounded.MicOff
                } else {
                    Icons.Default.Mic
                },
                contentDescription = "Mic"
            )
            Text(
                if (isRecording) {
                    "Stop"
                } else {
                    "Start"
                }
            )
        }
    }
}

@Composable
fun FullSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_full_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_full_space_mode)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun WeatherScreenPreview() {
    Column {
        RecordButton(true, {})
        RecordButton(false, {})
    }
}