package com.luckypines.android.accel1

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel(), ServiceConnection, OnValueChangedListener {

    private var binder: MainServiceBinder? = null
    private val data = MutableLiveData<FloatArray>()
    private var count: Int = 0
    private var bound: Boolean = false

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        this.binder = binder as MainServiceBinder
        this.binder?.setOnValueChangedListener(this)
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

    override fun onCleared() {
        super.onCleared()
        Log.d(">>>>>>", "CLEARED???")
    }
}