package com.kollins.project.sofia.interfaces.io

enum class InputType {
    ANALOG,
    DIGITAL
}

//For digital input only
enum class InputMode (var text:String){
    PUSH_GND("Push-GND"),
    PUSH_VCC("Push-VCC"),
    PULL_UP("Pull-UP"),
    PULL_DOWN("Pull-DOWN"),
    TOGGLE("Toggle")
}

interface InputInterface {
    fun clone(): InputInterface
    fun setInputType(type:InputType)
    fun getInputType(): InputType
    fun getInputIndex(): Int
    fun setInputIndex(index:Int)
    fun getInputModeIndex(): Int
    fun setInputModeIndex(index:Int)

}