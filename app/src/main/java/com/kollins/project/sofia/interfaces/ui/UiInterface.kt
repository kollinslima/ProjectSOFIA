package com.kollins.project.sofia.interfaces.ui

interface UiInterface {
    fun timeUpdate(time:String)
    fun loadSuccess()
    fun loadCoreChecksumError()
    fun loadCoreFileOpenFail()
    fun loadCoreInvalidFile()
    fun outputChange(change:String)
    fun outputConfig(config:String)
    fun inputConfig(config:String)
}