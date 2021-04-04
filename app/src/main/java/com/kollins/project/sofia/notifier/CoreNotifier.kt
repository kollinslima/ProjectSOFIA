package com.kollins.project.sofia.notifier

interface CoreNotifier {
    fun signalInput(pin:Int, voltage:Float)
}