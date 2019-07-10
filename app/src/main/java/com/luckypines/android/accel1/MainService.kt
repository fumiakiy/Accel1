package com.luckypines.android.accel1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import kotlin.math.abs

interface OnValueChangedListener {
    fun onValueChanged(values: FloatArray)
}

interface OnLocationChangedListener {
    fun onLocationChanged(locations: List<Location>)
}

class MainServiceBinder: Binder() {

    private var onValueChangedListener: OnValueChangedListener? = null
    private var onLocationChangedListener: OnLocationChangedListener? = null

    fun setOnValueChangedListener(listener: OnValueChangedListener?) {
        this.onValueChangedListener = listener
    }

    internal fun setData(values: FloatArray) {
        this.onValueChangedListener?.onValueChanged(values.copyOf())
    }

    fun setOnLocationChangedListener(listener: OnLocationChangedListener?) {
        this.onLocationChangedListener = listener
    }

    internal fun setLocations(locations: List<Location>) {
        this.onLocationChangedListener?.onLocationChanged(locations)
    }
}

class MainService: Service() {

    private val CHANNEL_ID = "CHANNEL_ID"
    private val SERVICE_ID = 91

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var locationClient: FusedLocationProviderClient? = null
    private var binder = MainServiceBinder()
    private var file: File? = null
    private var writer: OutputStreamWriter? = null

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create()?.apply {
            interval = 500
            fastestInterval = 100
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notification Title")
            .setContentText("Notification Message")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setTicker("Ticker Message")
            .build()
        startForeground(SERVICE_ID, notification)
        file = File.createTempFile("data.txt", null, cacheDir)
        writer = OutputStreamWriter(FileOutputStream(file))
        Log.d(">>>> file", file!!.absolutePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        writer?.close()
        locationClient?.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(sensorListener)
    }

    val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            Log.d("hoge", "$p1")
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val values = event.values
                if (abs(values[0]) > 0.1 || abs(values[1]) > 0.1 || abs(values[2]) > 0.1) {
                    binder.setData(values)
                    try {
                        val time = Date().time
                        writer?.write(String.format("V, %.04f, %.04f, %.04f, %d", values[0], values[1], values[2], time))
                    } catch (e: IOException) {
                        Log.e(">>>v", e.localizedMessage)
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, "MainChannel", importance).apply {
                description = "Main Channel"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            binder.setLocations(locationResult.locations)
            try {
                locationResult.locations.forEach { location ->
                    writer?.write(String.format("L, %.08f, %.08f, %d", location.latitude, location.longitude, location.time))
                }
            } catch (e: IOException) {
                Log.e(">>>l", e.localizedMessage)
            }
        }
    }
}
