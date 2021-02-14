package com.kollins.project.sofia.interfaces.ui

interface UiInterface {
    fun timeUpdate()
    fun loadSuccess()
    fun loadCoreChecksumError()
    fun loadCoreFileOpenFail()
    fun loadCoreInvalidFile()
    fun outputUpdate(change:String)
    fun inputUpdate(change:String)
}