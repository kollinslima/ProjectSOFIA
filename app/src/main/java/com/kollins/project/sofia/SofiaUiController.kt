package com.kollins.project.sofia

import android.util.Log
import com.kollins.project.sofia.interfaces.ui.UiInterface

enum class RequestCodes {
    READ_EXTERNAL_REQUEST_CODE,
    IMPORT_FILE_REQUEST_CODE
}

enum class Device {
    ATMEGA328P
}

private const val SOFIA_UI_CONTROLLER_TAG: String = "SOFIA UI CONTROLLER"

class SofiaUiController(mainUi: UiInterface) {

    private var ui = mainUi

    //////////////////// LISTENERS ///////////////////////////
    fun timeUpdate() {
        ui.timeUpdate()
    }

    fun loadCoreSuccess() {
        Log.d(SOFIA_UI_CONTROLLER_TAG, "File load successful !")
        startCore()
    }

    fun loadCoreChecksumError() {
        ui.loadCoreChecksumError()
    }

    fun loadCoreFileOpenFail() {
        ui.loadCoreFileOpenFail()
    }

    fun loadCoreInvalidFile() {
        ui.loadCoreInvalidFile()
    }

    fun outputChanged (change:String) {
        ui.ioUpdate(change)
    }

    //////////////////// NATIVE FUNCTIONS ///////////////////////////
    external fun startCore()
    external fun stopCore()
    external fun loadCore(s: Device, fd: Int)
    external fun disposeCore()

    companion object {
        const val TARGET_DEVICE_EXTRA: String = "TARGET_DEVICE"
        init {
            System.loadLibrary("sofiacore")
        }
    }

}