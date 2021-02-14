package com.kollins.project.sofia.v1.io.input.atmega328p

import com.kollins.project.sofia.defs.atmega328p.*
import com.kollins.project.sofia.interfaces.io.InputInterface
import com.kollins.project.sofia.interfaces.io.InputMode
import com.kollins.project.sofia.interfaces.io.InputType
import kotlin.experimental.and

//ATMega328P can go from -0.5V to 5.5V, but let's stick with the basics for now
private const val MAX_VOLTAGE_INPUT = 5.00f
//private const val MIN_VOLTAGE_INPUT = 0.00

class InputPinV1ATmega328P : InputInterface {

    private var curInput = PinMap.PINX
    private var modeIndex = InputMode.PULL_DOWN.ordinal
    private var type = InputType.DIGITAL

    override fun clone(): InputInterface {
        return InputPinV1ATmega328P()
    }

    override fun getPinNames(): List<String> {
        return inputList.map { it.boardName }
    }

    override fun setInputType(type: InputType) {
        this.type = type
    }

    override fun getInputType(): InputType {
        return type
    }

    override fun getPinIndex(): Int {
        val index = inputList.indexOf(curInput)
        return if (index > 0) index else inputList.indexOf(PinMap.PINX)
    }

    override fun setPinIndex(position: Int) {
        curInput = inputList[if (position > 0) position else 0]
    }

    override fun getInputModeIndex(): Int {
        return modeIndex
    }

    override fun setInputModeIndex(index: Int) {
        modeIndex = index
    }

    override fun getVoltage(percent: Int): Float {
        return (percent * MAX_VOLTAGE_INPUT) / 100
    }

    override fun ioUpdate(change: String) {
        val splittedChange: List<String> = change.split(":")
        val register = splittedChange[0].toByte()
        val value = splittedChange[1].toByte()

        updateInputList(register, value)
    }

    private fun updateInputList(register: Byte, value: Byte) {
        when (register) {
            IoRegisters.DDRB.addr -> {
                addInputPins(value, atmega328pPortBPins)
            }
            IoRegisters.DDRD.addr -> {
                addInputPins(value, atmega328pPortDPins)
            }
            IoRegisters.DDRC.addr -> {
                addInputPins(value, atmega328pPortCPins)
            }
        }
    }

    private fun addInputPins(value: Byte, atmega328pPortPins: List<PinMap>) {
        var i = 0
        var mask = 0x01
        while (i < atmega328pPortPins.size) {
            inputList.remove(atmega328pPortPins[i])
            if ((value and mask.toByte()) == 0.toByte()) {
                inputList.add(0, atmega328pPortPins[i])
            }
            i++
            mask = (mask shl 1)
        }
        inputList = inputList.sortedBy { it.devicePin }
            .toMutableList()
    }

    companion object {
        var inputList = atmega328pPinList.toMutableList()
    }
}