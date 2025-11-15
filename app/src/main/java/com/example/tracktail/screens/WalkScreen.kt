package com.example.tracktail.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.tracktail.data.models.Walk
import com.example.tracktail.data.models.WalkPoint
import com.example.tracktail.data.repositories.WalkRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt
import org.osmdroid.util.BoundingBox

enum class WalkState {
    IDLE, TRACKING, PAUSED
}

@Composable
fun WalkScreen() {
    val context = LocalContext.current
    val repository = remember { WalkRepository(context) }

    var showHistory by remember { mutableStateOf(false) }

    if (showHistory) {
        WalkHistoryScreen(
        )
        return
    }

    // --- STATES ---
    var walkState by remember { mutableStateOf(WalkState.IDLE) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var distance by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(0L) }
    var routePoints by remember { mutableStateOf(listOf<WalkPoint>()) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var walkStartTime by remember { mutableLongStateOf(0L) }

    // Light + Flashlight
    var lightLevel by remember { mutableFloatStateOf(100f) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var autoFlashlight by remember { mutableStateOf(true) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasLocationPermission = isGranted }

    // Light sensor
    DisposableEffect(walkState) {
        if (walkState == WalkState.TRACKING) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.getOrNull(0)

            val sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        lightLevel = it.values[0]

                        if (autoFlashlight && cameraId != null) {
                            val shouldBeOn = lightLevel < 10f
                            if (shouldBeOn != isFlashlightOn) {
                                try {
                                    cameraManager.setTorchMode(cameraId, shouldBeOn)
                                    isFlashlightOn = shouldBeOn
                                } catch (_: Exception) { }
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL
            )

            onDispose {
                sensorManager.unregisterListener(sensorListener)
                cameraId?.let {
                    try { cameraManager.setTorchMode(it, false) } catch (_: Exception) {}
                }
            }
        } else onDispose {}
    }

    // Timer
    LaunchedEffect(walkState) {
        if (walkState == WalkState.TRACKING) {
            while (true) {
                delay(1000)
                duration = System.currentTimeMillis() - walkStartTime
            }
        }
    }

    // Location tracking
    DisposableEffect(walkState, hasLocationPermission) {
        if (walkState == WalkState.TRACKING && hasLocationPermission) {
            val fused = LocationServices.getFusedLocationProviderClient(context)

            val req = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000L
            ).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    currentLocation = loc

                    val newPoint = WalkPoint(
                        loc.latitude, loc.longitude, System.currentTimeMillis()
                    )

                    if (routePoints.isNotEmpty()) {
                        val last = routePoints.last()
                        val arr = FloatArray(1)
                        Location.distanceBetween(
                            last.latitude, last.longitude,
                            newPoint.latitude, newPoint.longitude,
                            arr
                        )
                        distance += arr[0]
                    }

                    routePoints = routePoints + newPoint
                }
            }

            try {
                fused.requestLocationUpdates(req, callback, null)
            } catch (_: SecurityException) {}

            onDispose { fused.removeLocationUpdates(callback) }
        } else onDispose {}
    }

    // MAIN UI
    Box(modifier = Modifier.fillMaxSize()) {

        // --- MAPA ---
        MapViewComposable(
            currentLocation = currentLocation,
            routePoints = routePoints
        )

        // --- PANEL STEROWANIA NAD MAPĄ ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {

            ControlPanel(
                walkState = walkState,
                distance = distance,
                duration = duration,
                onStart = {
                    if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        walkStartTime = System.currentTimeMillis()
                        distance = 0f
                        duration = 0L
                        routePoints = emptyList()
                        walkState = WalkState.TRACKING
                    }
                },
                onPause = { walkState = WalkState.PAUSED },
                onResume = { walkState = WalkState.TRACKING },
                onStop = {
                    val walk = Walk(
                        id = Walk.generateId(),
                        petId = null,
                        startTime = walkStartTime,
                        endTime = System.currentTimeMillis(),
                        distance = distance,
                        duration = duration,
                        route = routePoints
                    )
                    repository.saveWalk(walk)

                    walkState = WalkState.IDLE
                    distance = 0f
                    duration = 0L
                    routePoints = emptyList()
                }
            )
        }

        // Historia — przycisk
        if (walkState == WalkState.IDLE) {
            Button(
                onClick = { showHistory = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text("📜 Historia")
            }
        }

        // --- KARTKA ŚWIATŁA ---
        if (walkState == WalkState.TRACKING) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                onClick = {
                    val cameraManager =
                        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val cameraId = cameraManager.cameraIdList[0]
                    autoFlashlight = false
                    isFlashlightOn = !isFlashlightOn
                    cameraManager.setTorchMode(cameraId, isFlashlightOn)
                }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (isFlashlightOn) "🔦 ON" else "🔦 OFF")
                    Text("${lightLevel.toInt()} lux")
                    if (!autoFlashlight)
                        Text("Manual", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
fun ControlPanel(
    walkState: WalkState,
    distance: Float,
    duration: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Statystyki
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("Dystans", "${(distance / 1000).roundToInt()} km")
                StatCard("Czas", formatDuration(duration))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Przyciski
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (walkState) {
                    WalkState.IDLE -> {
                        Button(modifier = Modifier.weight(1f), onClick = onStart) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Start")
                        }
                    }

                    WalkState.TRACKING -> {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onPause,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(Icons.Default.Pause, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Pauza")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onStop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Stop")
                        }
                    }

                    WalkState.PAUSED -> {
                        Button(modifier = Modifier.weight(1f), onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Wznów")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onStop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Zakończ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapViewComposable(
    currentLocation: Location?,
    routePoints: List<WalkPoint>
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(
                ctx,
                ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
            )

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0) // domyślny zoom
            }
        },
        update = { map ->
            map.overlays.clear()

            // === Rysowanie trasy ===
            if (routePoints.size > 1) {
                val polyline = Polyline().apply {
                    setPoints(routePoints.map { GeoPoint(it.latitude, it.longitude) })
                    outlinePaint.color = "#2E7D32".toColorInt()
                    outlinePaint.strokeWidth = 12f
                }
                map.overlays.add(polyline)

                // === Centrowanie mapy na środku trasy ===
                val bounds = polyline.bounds
                map.controller.setCenter(bounds.center)
                map.controller.setZoom(15.0.coerceAtMost(map.maxZoomLevel))
            }

            // === Bieżąca pozycja (opcjonalna) ===
            currentLocation?.let {
                val point = GeoPoint(it.latitude, it.longitude)
                val marker = Marker(map).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = context.getDrawable(android.R.drawable.presence_online)
                }
                map.overlays.add(marker)
                if (routePoints.isEmpty()) {
                    map.controller.animateTo(point)
                    map.controller.setZoom(17.0)
                }
            }

            map.invalidate()
        }
    )
}

// ---- POMOCNICZE ----

@Composable
fun StatCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
    }
}

fun formatDuration(millis: Long): String {
    val s = (millis / 1000) % 60
    val m = (millis / (1000 * 60)) % 60
    val h = millis / (1000 * 60 * 60)
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
