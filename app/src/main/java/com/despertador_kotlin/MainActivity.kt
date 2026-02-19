package com.despertador_kotlin

import android.app.KeyguardManager
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.ComposableInferredTarget
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.util.Log
import android.app.Activity
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

//import android.service.notification.NotificationListenerService
class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1
    }
    private val enableAdminLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // El usuario ha concedido los permisos de administración de dispositivos
            // Puedes cambiar el tiempo de suspensión de la pantalla aquí
        } else {
            // El usuario ha rechazado los permisos de administración de dispositivos
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
                // La aplicación no tiene permiso para ignorar las optimizaciones de batería
                // Solicitar al usuario que otorgue este permiso
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                // La aplicación ya tiene permiso para ignorar las optimizaciones de batería
                // No es necesario hacer nada
            }
        }
        // bateria_optimizacion_ignore_end
        Log.d("enablingScreen03", "Notification text: 0")
        // permiso_pantalla_start
        // test_start
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
        */
        // test_end
        /*
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        Log.d("enablingScreen04", "Notification text: 0")
        //val componentName = ComponentName(this, DevicePolicyManager::class.java)
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        Log.d("enablingScreen05", "Notification text: 0")
        if (devicePolicyManager.isAdminActive(componentName)) {
            if (true) {
                Log.d("enablingScreen1", "Notification text: 0")
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 120000) // 1 minuto
                Log.d("enablingScreen2", "Notification text: 0")
            } else {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 0) // nunca
            }
        } else {
            Log.d("enablingScreen06", "Notification text: 0")
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Necesitamos permiso para administrar tu dispositivo")
            startActivity(intent)
            Log.d("enablingScreen07", "Notification text: 0")
        }

        */
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
        setContent {
            Despertador_kotlinTheme {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        //verticalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting("esta es una alarma por notificaciones", Modifier.padding(bottom = 4.dp))
                        MyApp(this@MainActivity, Modifier.padding(bottom = 4.dp))
                        var hour1 by remember { mutableStateOf("") }
                        var minute1 by remember { mutableStateOf("") }
                        TimeInput(hour1, { hour1 = it }, minute1, { minute1 = it }, Modifier.padding(bottom = 4.dp))

                        var hour2 by remember { mutableStateOf("") }
                        var minute2 by remember { mutableStateOf("") }
                        TimeInput(hour2, { hour2 = it }, minute2, { minute2 = it }, Modifier.padding(bottom = 4.dp))
                        LaunchedEffect(hour1, minute1, hour2, minute2) {

                            val hour1Int = hour1.toIntOrNull() ?: -1
                            val minute1Int = minute1.toIntOrNull() ?: -1
                            val hour2Int = hour2.toIntOrNull() ?: -1
                            val minute2Int = minute2.toIntOrNull() ?: -1
                            NotificationListener.setTime(this@MainActivity, hour1Int, minute1Int, hour2Int, minute2Int)
                        }
                        CustomCheckboxDeshabilitarAlarma(Modifier.padding(bottom = 4.dp))
                    }
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hola, $name!",
        modifier = modifier
    )
}
@Composable
fun MyApp(context: Context, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Button(onClick = {
            /* Aquí va el código que se ejecutará al presionar el botón */
            NotificationListener.stopAudio()
            setVolumeToMin(context)
        }) {
            Text("Apagar")
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
@Composable
fun CustomCheckboxDeshabilitarAlarma(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val checked = remember { mutableStateOf(sharedPref.getBoolean("NoHacerSonar", false)) }
    val showDialog = remember { mutableStateOf(false) }

    val timer = remember {
        object: CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Nada que hacer aquí
            }

            override fun onFinish() {
                setScreenState(context, false)
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Confirmación") },
            text = { Text("¿Estás seguro de que quieres habilitar (Hara que no suene la alarma)?") },
            confirmButton = {
                Button(
                    onClick = {
                        checked.value = true
                        sharedPref.edit().putBoolean("NoHacerSonar", true).apply()
                        showDialog.value = false
                    }
                ) {
                    Text("Sí")
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
                    Text("No")
                }
            }
        )
    }

    Column(modifier = modifier) {
        Checkbox(
            checked = checked.value,
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    showDialog.value = true
                } else {
                    checked.value = false
                    sharedPref.edit().putBoolean("NoHacerSonar", false).apply()
                    timer.start()
                }
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Magenta,
                uncheckedColor = Color.Gray
            )
        )
        Text("La alarma ${if (checked.value) "No Sonara" else "Sonara"}")
    }
}

fun setVolumeToMin(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInput(
    hourValue: String,
    onHourValueChange: (String) -> Unit,
    minuteValue: String,
    onMinuteValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TextField(
            value = hourValue,
            onValueChange = { newHour ->
                if (newHour.isBlank() || (newHour.toIntOrNull() in 0..23)) {
                    onHourValueChange(newHour)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Hour") }
        )

        TextField(
            value = minuteValue,
            onValueChange = { newMinute ->
                if (newMinute.isBlank() || (newMinute.toIntOrNull() in 0..59)) {
                    onMinuteValueChange(newMinute)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Minute") }
        )
    }
}
