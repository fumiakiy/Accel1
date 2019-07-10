package com.luckypines.android.accel1

import android.content.ComponentName
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel(), ServiceConnection, OnValueChangedListener, OnLocationChangedListener {

    private var binder: MainServiceBinder? = null
    private val data = MutableLiveData<FloatArray>()
    private val locations = MutableLiveData<List<Location>>()
    private var count: Int = 0
    private var bound: Boolean = false

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        this.binder = binder as MainServiceBinder
        this.binder?.also {
            it.setOnValueChangedListener(this)
            it.setOnLocationChangedListener(this)
        }
        this.bound = true
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        this.binder?.setOnValueChangedListener(null)
        this.bound = false
    }

    override fun onValueChanged(values: FloatArray) {
        this.data.postValue(values)
        this.count++
    }

    fun getData(): LiveData<FloatArray> {
        return data
    }

    val dataCount: Int get() = count

    override fun onLocationChanged(locations: List<Location>) {
        this.locations.postValue(locations)
    }
    fun getLocations(): LiveData<List<Location>> {
        return locations
    }
}