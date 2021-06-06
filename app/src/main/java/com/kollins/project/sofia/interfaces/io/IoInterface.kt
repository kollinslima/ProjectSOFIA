package com.kollins.project.sofia.interfaces.io

interface IoInterface {
    fun clone(): IoInterface
    fun getPinNames() : List<String>
    fun ioChange(change:String) : Boolean
    fun ioConfig(config:String) : Boolean
    fun setPinIndex(index: Int)
    fun getPinIndex() : Int
}