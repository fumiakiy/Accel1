package com.luckypines.android.accel1

import android.app.*
import android.app.PendingIntent.FLAG_NO_CREATE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.abs

interface OnValueChangedListener {
    fun onValueChanged(values: FloatArray)
}

class MainServiceBinder: Binder() {

    private var listener: OnValueChangedListener? = null

    fun setOnValueChangedListener(listener: OnValueChangedListener?) {
        this.listener = listener
    }

    internal fun setData(values: FloatArray) {
        this.listener?.onValueChanged(values.copyOf())
    }
}

class MainService: Service() {

    private val CHANNEL_ID = "CHANNEL_ID"
    private val SERVICE_ID = 91

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var binder = MainServiceBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorListener)
    }

    val sensorListener = object: SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            Log.d("hoge", "$p1")
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val values = event.values
                if (abs(values[0]) > 0.1 || abs(values[1]) > 0.1 || abs(values[2]) > 0.1) {
                    binder.setData(values)
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
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
