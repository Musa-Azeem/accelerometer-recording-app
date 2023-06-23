package com.example.delta_alarm.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var nSamples by mutableStateOf(0)
    var text by mutableStateOf("Welcome")

    fun updateNSamples(_nSamples: Int) {
        nSamples = _nSamples
    }
    fun updateText(_text: String) {
        text = _text
    }
}