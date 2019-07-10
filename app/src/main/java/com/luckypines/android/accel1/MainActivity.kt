package com.luckypines.android.accel1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel get() = ViewModelProviders.of(this).get(MainViewModel::class.java)
    private val service: Intent get() = Intent(this, MainService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm.getData().observe(this, Observer<FloatArray> { values ->
            runOnUiThread {
                displayText.text = String.format("x: %.2f\ny:%.2f\nz:%.2f", values[0], values[1], values[2])
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
        bindService(service, vm, 0)
        sigText.text = String.format("Count: %d", vm.dataCount)
    }

    override fun onPause() {
        super.onPause()
        unbindService(vm)
    }

    private fun startForegroundService() {
        startService(service)
        bindService(service, vm, 0)
    }

    private fun stopForegroundService() {
        unbindService(vm)
        stopService(service)
    }

}
