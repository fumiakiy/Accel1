package com.luckypines.android.accel1

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel get() = ViewModelProviders.of(this).get(MainViewModel::class.java)
    private val service: Intent get() = Intent(this, MainService::class.java)
    private var serviceIsBound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm.getData().observe(this, Observer<FloatArray> { values ->
            runOnUiThread {
                displayText.text = String.format("x: %.2f\ny:%.2f\nz:%.2f", values[0], values[1], values[2])
            }
        })
        vm.getLocations().observe(this, Observer<List<Location>> {
            val l = it.lastOrNull() ?: return@Observer
            runOnUiThread {
                locationText.text = String.format("%.4f, %.4f", l.latitude, l.longitude)
            }
        })
        displayText.setOnClickListener {
            startForegroundService()
        }
        sigText.setOnClickListener {
            stopForegroundService()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!serviceIsBound) {
            bindService(service, vm, 0)
            serviceIsBound = true
        }
        sigText.text = String.format("Count: %d", vm.dataCount)
    }

    override fun onPause() {
        super.onPause()
        if (serviceIsBound) {
            unbindService(vm)
            serviceIsBound = false
        }
    }

    private fun startForegroundService() {
        startService(service)
        if (!serviceIsBound) {
            bindService(service, vm, 0)
            serviceIsBound = true
        }
    }

    private fun stopForegroundService() {
        if (serviceIsBound) {
            unbindService(vm)
            serviceIsBound = false
        }
        stopService(service)
    }

}
