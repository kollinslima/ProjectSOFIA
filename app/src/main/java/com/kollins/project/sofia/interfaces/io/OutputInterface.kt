package com.kollins.project.sofia.interfaces.io

enum class OutputState {
    HIGH,
    LOW,
    TRI_STATE
}

interface OutputInterface : IoInterface{
    fun updatePin()
    fun getPinState() : OutputState
    override fun clone(): OutputInterface
}