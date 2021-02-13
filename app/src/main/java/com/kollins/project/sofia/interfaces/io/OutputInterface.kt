package com.kollins.project.sofia.interfaces.io

enum class OutputState {
    HIGH,
    LOW,
    TRI_STATE
}

interface OutputInterface {
    fun outputUpdate(change:String)
    fun setOutputIndex(position: Int)
    fun getOutputIndex() : Int
    fun updatePin()
    fun getPinState() : OutputState
    fun getFrequency() : Double
    fun getDutyCycle() : Double
    fun getPinNames() : List<String>
    fun clone(): OutputInterface
    fun updateSimulationSpeed()
}