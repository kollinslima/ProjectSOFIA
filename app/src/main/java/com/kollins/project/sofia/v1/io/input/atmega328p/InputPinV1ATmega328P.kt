package com.kollins.project.sofia.v1.io.input.atmega328p

import com.kollins.project.sofia.interfaces.io.InputInterface
import com.kollins.project.sofia.interfaces.io.InputMode
import com.kollins.project.sofia.interfaces.io.InputType

//ATMega328P can go from -0.5V to 5.5V, but let's stick with the basics for now
private const val MAX_VOLTAGE_INPUT = 5.00f
//private const val MIN_VOLTAGE_INPUT = 0.00

//Map arduino names to ATMega328P pins
enum class InputMap(val boardName: String, val devicePin: String) {
    PIN0("Pin0", "2"),
    PIN1("Pin1", "3"),
    PIN2("Pin2", "4"),
    PIN3("Pin3", "5"),
    PIN4("Pin4", "6"),
    PIN5("Pin5", "11"),
    PIN6("Pin6", "12"),
    PIN7("Pin7", "13"),
    PIN8("Pin8", "14"),
    PIN9("Pin9", "15"),
    PIN10("Pin10", "16"),
    PIN11("Pin11", "17"),
    PIN12("Pin12", "18"),
    PIN13("Pin13", "19"),
    A0("A0", "23"),
    A1("A1", "24"),
    A2("A2", "25"),
    A3("A3", "26"),
    A4("A4", "27"),
    A5("A5", "28"),
    PINX("PinX", "-1")
}

class InputPinV1ATmega328P : InputInterface {

    private var inputIndex = InputMap.PINX.ordinal
    private var modeIndex = InputMode.PULL_DOWN.ordinal
    private var type = InputType.DIGITAL

    override fun clone(): InputInterface {
        return InputPinV1ATmega328P()
    }

    override fun setInputType(type: InputType) {
        this.type = type
    }

    override fun getInputType(): InputType {
        return type
    }

    override fun getInputIndex(): Int {
        return inputIndex
    }

    override fun setInputIndex(index: Int) {
        inputIndex = index
    }

    override fun getInputModeIndex(): Int {
        return modeIndex
    }

    override fun setInputModeIndex(index: Int) {
        modeIndex = index
    }

    override fun getVoltage(percent: Int): Float {
        return (percent * MAX_VOLTAGE_INPUT)/100
    }

    companion object {
        val pinNames = InputMap.values()
    }
}