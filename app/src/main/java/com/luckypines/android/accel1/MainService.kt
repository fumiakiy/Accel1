package com.luckypines.android.accel1

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
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

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var binder = MainServiceBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
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
}
