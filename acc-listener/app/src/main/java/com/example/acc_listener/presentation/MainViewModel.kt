package com.example.acc_listener.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var nSamples by mutableStateOf(0)
    var text by mutableStateOf("Welcome")
    var status by mutableStateOf("None")

    fun updateNSamples(_nSamples: Int) {
        nSamples = _nSamples
    }
    fun updateText(_text: String) {
        text = _text
    }
    fun updateStatus(_status: String) {
        status = _status
    }
}