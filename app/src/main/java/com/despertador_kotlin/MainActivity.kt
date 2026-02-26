package com.despertador_kotlin

import android.app.KeyguardManager
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.despertador_kotlin.ui.theme.Despertador_kotlinTheme
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.material3.Button
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.ComposableInferredTarget
import android.content.ComponentName
import android.util.Log
import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.os.CountDownTimer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.delay
import android.content.SharedPreferences
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex

//import android.service.notification.NotificationListenerService
class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "POST_NOTIFICATIONS concedido")
        }
    }
    fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return flat != null && flat.contains(pkgName)
    }
    fun openNotificationListenerSettings() {
        Toast.makeText(this, "ATENCIÃ“N: Si el permiso estÃ¡ deshabilitado (en gris), ve a Ajustes > Aplicaciones > Despertador > MenÃº (3 puntos arriba a la derecha) > 'Permitir ajustes restringidos'.", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("enablingScreen01", "Notification text: 0")
        // check permiso1_start
        if (!isNotificationServiceEnabled()) {
            Log.e("NotificationListener", "Notification Listener Service is not enabled")
            // Show a message to the user asking them to enable the service
            openNotificationListenerSettings()
        }
        // check permiso1_end
        // inicializar_servicio_end
        NotificationListener.loadTime(this)
        // inicializar_servicio_start
        // pantalla_bloqueada_start
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        // pantalla_bloqueada_end
        Log.d("enablingScreen02", "Notification text: 0")
        // bateria_optimizacion_ignore_start
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                // La aplicaciÃ³n no tiene permiso para ignorar las optimizaciones de baterÃ­a
                // Solicitar al usuario que otorgue este permiso
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                // La aplicaciÃ³n ya tiene permiso para ignorar las optimizaciones de baterÃ­a
                // No es necesario hacer nada
            }
        }
        // bateria_optimizacion_ignore_end

        // Permiso de Notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Permiso de Mostrar sobre otras aplicaciones (Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        Log.d("enablingScreen03", "Notification text: 0")
        // permiso_pantalla_start
        // permiso_pantalla_end
        // layout_start
        setContentView(R.layout.activity_main)
        // layout_end

        super.onCreate(savedInstanceState)
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.alarm_sound)
        // Iniciar servicio start
        //val intent = Intent(this, NotificationListener::class.java)
        val componentName = ComponentName(this, NotificationListener::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.service.notification.NotificationListenerService.requestRebind(componentName)
        }

        // Iniciar servicio end

        //mediaPlayer.start()
        //mediaPlayer.stop()
        /*
        mediaPlayer.stop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
            */
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Despertador_kotlinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding().imePadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isRinging by remember { mutableStateOf(NotificationListener.isAudioPlaying()) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            isRinging = NotificationListener.isAudioPlaying()
                            delay(500)
                        }
                    }

                    if (isRinging) {
                        RingingScreen(this@MainActivity) { isRinging = false }
                    } else {
                        MainScreen(this@MainActivity)
                    }
                }

            }
        }
    }
}

fun setScreenState(context: Context, shouldKeepScreenOn: Boolean) {
    val activity = context as? Activity
    if (activity != null) {
        if (shouldKeepScreenOn) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

fun getSavedKeywords(sharedPref: SharedPreferences): List<String> {
    val orderedStr = sharedPref.getString("keywords_ordered", null)
    if (orderedStr != null) {
        return if (orderedStr.isBlank()) emptyList() else orderedStr.split("|||")
    }
    val legacySet = sharedPref.getStringSet("keywords", setOf("prog1"))
    val list = legacySet?.toList() ?: listOf("prog1")
    sharedPref.edit().putString("keywords_ordered", list.joinToString("|||")).apply()
    return list
}

fun saveKeywords(sharedPref: SharedPreferences, list: List<String>) {
    sharedPref.edit()
        .putString("keywords_ordered", list.joinToString("|||"))
        .putStringSet("keywords", list.toSet())
        .apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context) {
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    
    var hour1 by remember { mutableStateOf(sharedPref.getInt("hour1", -1).let { if (it == -1) "" else it.toString() }) }
    var minute1 by remember { mutableStateOf(sharedPref.getInt("minute1", -1).let { if (it == -1) "" else it.toString() }) }
    var hour2 by remember { mutableStateOf(sharedPref.getInt("hour2", -1).let { if (it == -1) "" else it.toString() }) }
    var minute2 by remember { mutableStateOf(sharedPref.getInt("minute2", -1).let { if (it == -1) "" else it.toString() }) }

    // Estado para las palabras clave
    var keywords by remember {
        mutableStateOf(getSavedKeywords(sharedPref))
    }
    var disabledKeywords by remember {
        mutableStateOf(sharedPref.getStringSet("disabled_keywords", setOf())?.toSet() ?: setOf())
    }
    var newKeyword by remember { mutableStateOf("") }

    LaunchedEffect(hour1, minute1, hour2, minute2) {
        val hour1Int = hour1.toIntOrNull() ?: -1
        val minute1Int = minute1.toIntOrNull() ?: -1
        val hour2Int = hour2.toIntOrNull() ?: -1
        val minute2Int = minute2.toIntOrNull() ?: -1
        NotificationListener.setTime(context, hour1Int, minute1Int, hour2Int, minute2Int)
    }

    val density = LocalDensity.current
    val spacingPx = with(density) { 8.dp.toPx() }
    var itemHeight by remember { mutableStateOf(0f) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Despertador por Notificaciones",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "La alarma sonará automáticamente cuando recibas una notificación que contenga la palabra clave configurada.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Configuración de Horario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Define un rango de horas (en formato 24h) en las cuales deseas que la alarma opere.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    TimeInputRow("Hora de Inicio", hour1, { hour1 = it }, minute1, { minute1 = it })
                    TimeInputRow("Hora de Fin", hour2, { hour2 = it }, minute2, { minute2 = it })
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Palabras Clave de Alerta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("La alarma sonará si la notificación contiene alguna de estas palabras o frases:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Input para agregar nuevas palabras
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newKeyword,
                            onValueChange = { newKeyword = it },
                            label = { Text("Nueva palabra/frase") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newKeyword.isNotBlank() && !keywords.contains(newKeyword.trim())) {
                                    val updatedList = keywords + newKeyword.trim()
                                    keywords = updatedList
                                    saveKeywords(sharedPref, updatedList)
                                    newKeyword = ""
                                }
                            }
                        ) {
                            Text("Añadir")
                        }
                    }

                    // Lista de palabras activas
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (keywords.isEmpty()) {
                            Text("No hay palabras configuradas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        } else {
                            keywords.forEachIndexed { index, keyword ->
                                val isEnabled = !disabledKeywords.contains(keyword)
                                val isDragging = draggedIndex == index
                                val zIndex = if (isDragging) 1f else 0f
                                val translationY = if (isDragging) dragOffset else 0f
                                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(zIndex)
                                        .graphicsLayer { this.translationY = translationY }
                                        .shadow(elevation, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .onGloballyPositioned {
                                            if (itemHeight == 0f) {
                                                itemHeight = it.size.height.toFloat() + spacingPx
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Arrastrar para reordenar",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .pointerInput(index, itemHeight) {
                                                    detectVerticalDragGestures(
                                                        onDragStart = { draggedIndex = index; dragOffset = 0f },
                                                        onDragEnd = { draggedIndex = null; dragOffset = 0f },
                                                        onDragCancel = { draggedIndex = null; dragOffset = 0f },
                                                        onVerticalDrag = { change, dragAmount ->
                                                            change.consume()
                                                            dragOffset += dragAmount
                                                            if (itemHeight > 0) {
                                                                val steps = (dragOffset / itemHeight).toInt()
                                                                val fraction = (dragOffset / itemHeight) - steps
                                                                var targetIndex = index + steps
                                                                if (fraction > 0.5f) targetIndex += 1
                                                                else if (fraction < -0.5f) targetIndex -= 1
                                                                targetIndex = targetIndex.coerceIn(0, keywords.lastIndex)

                                                                if (targetIndex != index) {
                                                                    val newList = keywords.toMutableList()
                                                                    val item = newList.removeAt(index)
                                                                    newList.add(targetIndex, item)
                                                                    keywords = newList
                                                                    saveKeywords(sharedPref, newList)
                                                                    draggedIndex = targetIndex
                                                                    dragOffset -= (targetIndex - index) * itemHeight
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                        )
                                        Switch(
                                            checked = isEnabled,
                                            onCheckedChange = { checked ->
                                                val updatedDisabled = if (checked) {
                                                    disabledKeywords - keyword
                                                } else {
                                                    disabledKeywords + keyword
                                                }
                                                disabledKeywords = updatedDisabled
                                                sharedPref.edit().putStringSet("disabled_keywords", updatedDisabled).apply()
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = keyword,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            val updatedList = keywords - keyword
                                            keywords = updatedList
                                            saveKeywords(sharedPref, updatedList)

                                            val updatedDisabled = disabledKeywords - keyword
                                            disabledKeywords = updatedDisabled
                                            sharedPref.edit().putStringSet("disabled_keywords", updatedDisabled).apply()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Borrar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text("Control de Alarma", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomCheckboxDeshabilitarAlarma()
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Composable
fun CustomCheckboxDeshabilitarAlarma(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val checked = remember { mutableStateOf(sharedPref.getBoolean("NoHacerSonar", false)) }
    val showDialog = remember { mutableStateOf(false) }

    val timer = remember {
        object: CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Nada que hacer aquÃ­
            }

            override fun onFinish() {
                setScreenState(context, false)
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("ConfirmaciÃ³n") },
            text = { Text("¿Estás seguro de que quieres silenciar la alarma? No sonará aunque recibas notificaciones.") },
            confirmButton = {
                Button(
                    onClick = {
                        checked.value = true
                        sharedPref.edit().putBoolean("NoHacerSonar", true).apply()
                        showDialog.value = false
                    }
                ) {
                    Text("Sí, silenciar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        checked.value = false
                        sharedPref.edit().putBoolean("NoHacerSonar", false).apply()
                        showDialog.value = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (!checked.value) {
                    showDialog.value = true
                } else {
                    checked.value = false
                    sharedPref.edit().putBoolean("NoHacerSonar", false).apply()
                    timer.start()
                }
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked.value,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Silenciar Alarma", 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (checked.value) "La alarma NO sonará" else "La alarma está ACTIVA y sonará",
                style = MaterialTheme.typography.bodySmall,
                color = if (checked.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun setVolumeToMin(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputRow(
    label: String,
    hourValue: String,
    onHourValueChange: (String) -> Unit,
    minuteValue: String,
    onMinuteValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hourValue,
                onValueChange = { newHour ->
                    if (newHour.isBlank() || (newHour.length <= 2 && (newHour.toIntOrNull() in 0..23))) {
                        onHourValueChange(newHour)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Hora") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Text(":", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = minuteValue,
                onValueChange = { newMinute ->
                    if (newMinute.isBlank() || (newMinute.length <= 2 && (newMinute.toIntOrNull() in 0..59))) {
                        onMinuteValueChange(newMinute)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Minuto") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@Composable
fun RingingScreen(context: Context, onStopRinging: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡ALARMA SONANDO!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = {
                NotificationListener.stopAudio()
                setVolumeToMin(context)
                Toast.makeText(context, "Alarma apagada", Toast.LENGTH_SHORT).show()
                onStopRinging()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("APAGAR ALARMA", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
        }
    }
}
