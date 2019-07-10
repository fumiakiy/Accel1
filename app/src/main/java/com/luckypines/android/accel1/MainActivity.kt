package com.luckypines.android.accel1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val vm = ViewModelProviders.of(this).get(MainViewModel::class.java)
        vm.getData().observe(this, Observer<FloatArray> { values ->
                runOnUiThread {
                    displayText.text = String.format("x: %.2f\ny:%.2f\nz:%.2f", values[0], values[1], values[2])
                }
            })
        Intent(this, MainService::class.java).also { intent ->
            bindService(intent, vm, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        sigText.text = String.format("Count: %d",
            ViewModelProviders.of(this).get(MainViewModel::class.java).getCount())
    }
}
