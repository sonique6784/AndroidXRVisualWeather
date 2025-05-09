package fr.sonique.mygeminiapplication

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.sonique.mygeminiapplication.ui.theme.MyGeminiApplicationTheme
import android.graphics.Bitmap
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.EdgeOffset
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SpatialRoundedCornerShape
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.padding
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.width
import androidx.xr.scenecore.GltfModel
import kotlinx.coroutines.launch
import androidx.concurrent.futures.await
import androidx.xr.compose.platform.LocalSession
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.SpatialCapabilities
import androidx.xr.scenecore.scene
import androidx.xr.scenecore.*
import java.lang.Thread.sleep
import java.util.concurrent.Executors

const val PERMISSIONS_REQUEST_RECORD_AUDIO = 100
const val IS_CONTINUES_LISTEN = false
const val RESULTS_LIMIT = 1

// 3D Models
// Clouds:  https://sketchfab.com/3d-models/clouds-116f49c23c4347eba340d0f59b0601f7
//          https://sketchfab.com/3d-models/cloud-3a76eb255e3c4c0199bbfedb2b54342f
//          https://sketchfab.com/3d-models/rain-3-6021e79c421344af9667469aa472389b

// Sun:     https://sketchfab.com/3d-models/simple-sun-83dd341b8c4c4f83808f5821e89f056e

// Rain:    https://sketchfab.com/3d-models/rain-2-b6eda19a94794336859fd61e6b7d5ae2

// Ligthning: https://sketchfab.com/3d-models/rain-1-5f04d3b4c0e04142a4a5d3d7d0bd3d22

// Fog: https://sketchfab.com/3d-models/fog-168a728c79014cebb27cbec6e7c3bf96#download

// Snowy : https://sketchfab.com/3d-models/snow-37443e9855254c85871ab894de910f50

// find places: https://zoom.earth/storms/93a-2025/

class MainActivity : ComponentActivity() {

    private var selectedLanguage = "en"
    private val viewModel: WeatherViewModel by viewModels()

    private val weatherRecognitionListener = WeatherRecognitionListener(
        context = this,
        selectedLanguage = selectedLanguage,
        onResultCallback = {
        viewModel.requestWeather(it)
    })

    private val storm3d = ThreeDimensionalModelSetup(
        "models/lightning.glb",
        0.002f,
        floatArrayOf(0f, 0f, 0.1f),
        "Take 001"
    )

    private val cloudy = ThreeDimensionalModelSetup(
        "models/clouds2.glb",
        0.003f,
        floatArrayOf(0f, -0.2f, 0.1f),
        "Take 001"
    )

    private val raining = ThreeDimensionalModelSetup(
        "models/simple_rain.glb",
        0.002f,
        floatArrayOf(0f, 0f, 0.1f),
        "Take 001"
    )

    private val sunny = ThreeDimensionalModelSetup(
        "models/simple_sun.glb",
        0.40f,
        floatArrayOf(-1f, 1.5f, -0.1f)
    )


    private val foggy = ThreeDimensionalModelSetup(
        "models/fog.glb",
        0.03f,
        floatArrayOf(1f, -1.5f, 0.1f),
        "ani"
    )

    private val snowy = ThreeDimensionalModelSetup(
        "models/snow.glb",
        0.005f,
        floatArrayOf(0f, 0.55f, 0.2f),
        "CINEMA_4D_Main"
    )

    private val weather3d = hashMapOf<WeatherFor3dModel, ThreeDimensionalModelSetup>(
        WeatherFor3dModel.CLOUDY to cloudy,
        WeatherFor3dModel.SUNNY to sunny,
        WeatherFor3dModel.RAINING to raining,
        WeatherFor3dModel.STORM to storm3d,
        WeatherFor3dModel.FOGGY to foggy,
        WeatherFor3dModel.SNOWY to snowy
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGeminiApplicationTheme {
                val session = LocalSpatialConfiguration.current
                if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    Subspace {
                        MySpatialContent(onRequestHomeSpaceMode = { session.requestHomeSpaceMode() })
                    }
                } else {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        WeatherScreen(
                            weatherViewModel = viewModel,
                            onStartListening = {
                                weatherRecognitionListener.startListening()
                                //viewModel.sendJSONPrompt("What is the weather in paris")
                            },
                            onStopListening = {
                                weatherRecognitionListener.onStopListening()
                            },
                            onSendTextPrompt = { text ->
                                viewModel.requestWeather(text)
                            },
                            onXRFullScreen = {
                                session.requestFullSpaceMode()
                            }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is UiState.SuccessBitmap -> {
                            save(uiState.outputBitmap, uiState.name)
                        }
                        else -> {}
                    }
                }
            }
        }

        checkPermissions()
    }

    private val assetCache: HashMap<String, GltfModel> = HashMap()

    fun loadModelAsset(xrSession: Session?, modelName: String) {
        if(xrSession == null) {
            return
        }

        lifecycleScope.launch {
            //load the asset if it hasn't been loaded previously
            if (!assetCache.containsKey(modelName)) {
                try {
                    val gltfModel =
                        GltfModel.create(xrSession, modelName).await()
                    assetCache[modelName] = gltfModel

                } catch (e: Exception) {
                    Log.e(
                        "Android Weather",
                        "Failed to load model for $modelName: $e"
                    )
                }
            }
        }
    }


    @SuppressLint("RestrictedApi")
    @Composable
    fun MySpatialContent(onRequestHomeSpaceMode: () -> Unit) {

        // get XR Session
        val xrSession = LocalSession.current

        var showSecondPanel by remember { mutableStateOf(true) } // State to control visibility

        val uiState by viewModel.uiState.collectAsState()


        // import 3d model
//        xrSession?.let { s ->
//            if (xrSession.scene.spatialCapabilities
//                    .hasCapability(SpatialCapabilities.SPATIAL_CAPABILITY_3D_CONTENT)
//            ) {
//                assetCache["models/clouds.glb"]?.let { c ->
//                    val gltfEntity = GltfModelEntity.create(xrSession, c)
//
//                }
//
//                //gltfEntity.startAnimation(loop = true, animationName = "Walk")
//            }
//        }

        SpatialPanel(SubspaceModifier
            .width(1280.dp)
            .height(800.dp)
            .resizable()
            .movable()) {
            Surface {
                WeatherScreen(
                    weatherViewModel = viewModel,
                    onStartListening = {
                        weatherRecognitionListener.startListening()
                        //viewModel.sendJSONPrompt("What is the weather in paris")
                    },
                    onStopListening = {
                        weatherRecognitionListener.onStopListening()
                    },
                    onSendTextPrompt = { text ->
                        viewModel.requestWeather(text)
                    }
                )
            }
            Orbiter(
                position = OrbiterEdge.Bottom,
                offset = EdgeOffset.inner(offset = 20.dp),
                alignment = Alignment.End,
                shape = SpatialRoundedCornerShape(CornerSize(28.dp))
            ) {
                HomeSpaceModeIconButton(
                    onClick = onRequestHomeSpaceMode,
                    modifier = Modifier.size(56.dp)
                )
            }

            // You might want a button to toggle the second panel's visibility
            Orbiter(
                position = OrbiterEdge.Top,
                offset = EdgeOffset.inner(offset = 20.dp),
                alignment = Alignment.End,
                shape = SpatialRoundedCornerShape(CornerSize(28.dp))
            ) {
                FilledTonalIconButton(
                    onClick = { showSecondPanel = !showSecondPanel },
                    modifier = Modifier.size(56.dp)
                ) {

                    Icon(
                        // Use an appropriate icon for toggling visibility
                        painter = rememberVectorPainter(Icons.Default.Visibility), // Replace with your icon
                        contentDescription = "Toggle second panel"
                    )
                }
            }
        }

        // Second panel
        if (showSecondPanel && uiState is UiState.SuccessBitmap) { // Conditionally render the second panel
            SpatialPanel(
                SubspaceModifier
                    .width(800.dp) // Adjust size as needed
                    .height(800.dp) // Adjust size as needed
                    .movable() // Make the second panel movable
                    .resizable()
                    .offset(x = 800.dp, y = 0.dp, z = 200.dp)
                    .rotate(pitch = 0.5f, // _
                        yaw = -20f, // |
                        roll = 0f) // ()
                    .padding(left = 0.dp, right=0.dp, top=0.dp, bottom=0.dp) // Add some padding

            ) {
                Surface(modifier = Modifier.background(Color.Green)) {
                    (uiState as? UiState.SuccessBitmap)?.outputBitmap?.let {
                        Image(it.asImageBitmap(),
                            contentDescription = "Image generated",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
        if(uiState as? UiState.SuccessBitmap != null) {
            (uiState as UiState.SuccessBitmap).type?.let {
                render3d(it)
            }
        }
    }

    private fun save(bitmap: Bitmap?, filename: String?) {
        if(bitmap != null && filename != null) {
            SaveImageHelper.saveBitmap(context = this, bitmap = bitmap, fileName = filename)
        }
    }

    private fun checkPermissions() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.RECORD_AUDIO
            )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                /* activity = */ this,
                /* permissions = */ arrayOf(android.Manifest.permission.RECORD_AUDIO),
                /* requestCode = */ PERMISSIONS_REQUEST_RECORD_AUDIO
            )
            return
        }
    }

    var modelEntity: GltfModelEntity? = null

        @Composable
    fun render3d(modelType: WeatherFor3dModel) {
        val xrSession = LocalSession.current
//        val root = xrSession?.activity

//        val executor by lazy { Executors.newSingleThreadExecutor() }

        val modelInfo = weather3d[modelType] ?: sunny


        LaunchedEffect(Unit) {
            // Model loading
            val model = GltfModel.create(xrSession!!, modelInfo.model).await() //xrSession.createGltfResourceAsync("low_poly_tiktok_3d_logo.glb").await()
//            val modelEntity = assetCache["models/clouds.glb"]?.let { c ->
//            } ?: null

//            modelEntity?.getParent()?.removeAllComponents()
            modelEntity?.dispose()
            modelEntity = GltfModelEntity.create(xrSession, model)

            // Transformations
            val translation = Vector3(modelInfo.position[0], modelInfo.position[1], modelInfo.position[2])
            val orientation = Quaternion.fromEulerAngles(0f, 0f, 0f)
            val pose = Pose(translation, orientation)
            modelEntity?.setPose(pose)
            modelEntity?.setScale(modelInfo.scale)
            modelEntity?.startAnimation(loop = true, animationName = modelInfo.animation)


            // Model name
            // Scale
            // position
            // animation name


//            val interactable = InteractableComponent.create(xrSession, executor) { ie ->
//            // Setting an Interactable Component
//                when (ie.action) {
//                    InputEvent.ACTION_HOVER_ENTER -> {
//                        modelEntity?.setScale(scale * 1.2f)
//                        for (i in 0..360) {
//                            val rotation = Quaternion.fromEulerAngles(0f, i.toFloat(), 0f)
//                            modelEntity?.setPose(Pose(translation, rotation))
//                            sleep(5)
//                        }
//                        println(modelEntity?.getScale())
//                    }
//                    InputEvent.ACTION_HOVER_EXIT -> {
//                        modelEntity?.setScale(scale)
//                        println(modelEntity?.getScale())
//                    }
//                    else -> {}
//                }
//            }
//            modelEntity?.addComponent(interactable)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                weatherRecognitionListener.startListening()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        weatherRecognitionListener.resetSpeechRecognizer()
        if (IS_CONTINUES_LISTEN) {
            weatherRecognitionListener.startListening()
        }
    }

    override fun onPause() {
        weatherRecognitionListener.onEndOfSpeech()
        super.onPause()
    }

    override fun onStop() {
        Log.d("speech", "stop")
        weatherRecognitionListener.onStopListening()
        super.onStop()
    }

    @Composable
    fun HomeSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
        FilledTonalIconButton(onClick = onClick, modifier = modifier) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home_space_mode_switch),
                contentDescription = stringResource(R.string.switch_to_home_space_mode)
            )
        }
    }
}