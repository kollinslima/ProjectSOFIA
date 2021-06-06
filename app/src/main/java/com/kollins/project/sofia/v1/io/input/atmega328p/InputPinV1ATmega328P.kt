package com.kollins.project.sofia.v1.io.input.atmega328p

import com.kollins.project.sofia.defs.atmega328p.*
import com.kollins.project.sofia.interfaces.io.InputInterface
import com.kollins.project.sofia.interfaces.io.InputMode
import com.kollins.project.sofia.interfaces.io.InputType
import com.kollins.project.sofia.notifier.CoreNotifier
import kotlin.experimental.and

//ATMega328P can go from -0.5V to 5.5V, but let's stick with the basics for now
private const val MAX_VOLTAGE_INPUT = 5.00f
//private const val MIN_VOLTAGE_INPUT = 0.00f

class InputPinV1ATmega328P(private val scn: CoreNotifier) : InputInterface {

    private var curInput = PinMap.PINX
    private var modeIndex = InputMode.PULL_DOWN.ordinal
    private var type = InputType.DIGITAL

    override fun clone(): InputInterface {
        return InputPinV1ATmega328P(scn)
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

    override fun setPinIndex(index: Int) {
        curInput = inputList[if (index > 0) index else 0]
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

    override fun notifySignalInput(voltage: Float) {
        scn.signalInput(curInput.devicePin, voltage)
    }

    override fun ioChange(change: String) : Boolean{
        return false;
    }

    override fun ioConfig(config: String) : Boolean{
        val splittedConfig: List<String> = config.split(":")
        val register = splittedConfig[0].toUByte()
        val value = splittedConfig[1].toUByte()
        if (inputRegisters[register] != value) {
            inputRegisters[register] = value
            updateInputList(register, value)
            return true;
        }
        return false;
    }

    private fun updateInputList(register: UByte, value: UByte) {
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

    private fun addInputPins(value: UByte, atmega328pPortPins: List<PinMap>) {
        var i = 0
        var mask = 0x01
        while (i < atmega328pPortPins.size) {
            inputList.remove(atmega328pPortPins[i])
            if ((value and mask.toUByte()) == 0.toUByte()) {
                inputList.add(0, atmega328pPortPins[i])
            }
            i++
            mask = (mask shl 1)
        }
        inputList = inputList.sortedBy { it.devicePin }
            .toMutableList()
    }

    companion object {
        var inputRegisters = mutableMapOf<UByte, UByte>(
            IoRegisters.DDRB.addr to 0x00u,
            IoRegisters.DDRC.addr to 0x00u,
            IoRegisters.DDRD.addr to 0x00u
        )

        var inputList = atmega328pPinList.toMutableList()
    }
}