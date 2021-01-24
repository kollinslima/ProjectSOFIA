package com.kollins.project.sofia.interfaces.ui

interface UiInterface {
    fun timeUpdate()
    fun loadCoreChecksumError()
    fun loadCoreFileOpenFail()
    fun loadCoreInvalidFile()
    fun ioUpdate(change:String)
}