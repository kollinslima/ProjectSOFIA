package com.kollins.project.sofia.interfaces.io

enum class OutputState {
    HIGH,
    LOW,
    TRI_STATE
}

interface OutputInterface {
    fun ioUpdate(addr: Byte, value: Byte)
    fun setOutputIndex(position: Int)
    fun getOutputIndex() : Int
    fun updatePinState()
    fun getPinState() : OutputState
    fun getPinNames() : List<String>
    fun clone(): OutputInterface
}