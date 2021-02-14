package com.kollins.project.sofia.interfaces.io

interface IoInterface {
    fun clone(): IoInterface
    fun getPinNames() : List<String>
    fun ioUpdate(change:String)
    fun setPinIndex(position: Int)
    fun getPinIndex() : Int
}