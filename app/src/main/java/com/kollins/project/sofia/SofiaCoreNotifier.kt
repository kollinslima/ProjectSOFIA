package com.kollins.project.sofia

import android.util.Log
import com.kollins.project.sofia.notifier.CoreNotifier

private const val SOFIA_CORE_NOTIFIER_TAG: String = "SOFIA CORE NOTIFIER"

class SofiaCoreNotifier(private val suc:SofiaUiController):CoreNotifier {
    override fun signalInput(pin: Int, voltage: Float) {
        Log.d(SOFIA_CORE_NOTIFIER_TAG, "Send signal to device: $pin:$voltage")
        suc.signalInput(pin,voltage)
    }
}