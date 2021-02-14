package com.kollins.project.sofia.interfaces.io

enum class OutputState {
    HIGH,
    LOW,
    TRI_STATE
}

interface OutputInterface : IoInterface{
    fun updatePin()
    fun getPinState() : OutputState
    fun getFrequency() : Double
    fun getDutyCycle() : Double
    fun updateSimulationSpeed()
    override fun clone(): OutputInterface
}