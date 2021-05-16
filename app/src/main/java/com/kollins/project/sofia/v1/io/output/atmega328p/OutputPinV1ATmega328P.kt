package com.kollins.project.sofia.v1.io.output.atmega328p

import android.os.SystemClock
import android.util.Log
import com.kollins.project.sofia.defs.atmega328p.*
import com.kollins.project.sofia.interfaces.io.OutputInterface
import com.kollins.project.sofia.interfaces.io.OutputState
import kotlin.experimental.and

private const val METER_FILTER_SIZE = 2
private const val PIN_START_PORTD = 2
private const val PIN_START_PORTB = 14
private const val PIN_START_PORTC = 23

private const val OUTPUT_PIN_V1_ATMEGA328P_TAG: String = "OUT PIN V1 ATMEGA328P"

@ExperimentalUnsignedTypes
class OutputPinV1ATmega328P : OutputInterface {
    private var curOutput = PinMap.PINX
    private var portAddr: UByte = 0x00u
    private var ddrAddr: UByte = 0x00u
    private var outputBit = 0
    private var pinState = OutputState.TRI_STATE

    private var highTime: LongArray = LongArray(METER_FILTER_SIZE) { 0 }
    private var highTimeIndex = 0
    private var wavePeriod: LongArray = LongArray(METER_FILTER_SIZE) { 0 }
    private var wavePeriodIndex = 0
    private var measureHighTime: Long = 0
    private var lastRisingEdgeTimestamp: Long = 0


    override fun ioUpdate(change: String) {
        val splittedChange: List<String> = change.split(":")
        val register = splittedChange[0].toUByte()
        val value = splittedChange[1].toUByte()
        outRegisters[register] = value

        updateOutputList(register, value)
    }

    private fun updateOutputList(register: UByte, value: UByte) {
        when (register) {
            IoRegisters.DDRB.addr -> {
                addOutputPins(value, atmega328pPortBPins)
            }
            IoRegisters.DDRD.addr -> {
                addOutputPins(value, atmega328pPortDPins)
            }
            IoRegisters.DDRC.addr -> {
                addOutputPins(value, atmega328pPortCPins)
            }
        }
    }

    private fun addOutputPins(value: UByte, atmega328pPortPins: List<PinMap>) {
        var i = 0
        var mask = 0x01
        while (i < atmega328pPortPins.size) {
            outputList.remove(atmega328pPortPins[i])
            if ((value and mask.toUByte()) != 0.toUByte()) {
                outputList.add(0, atmega328pPortPins[i])
            }
            i++
            mask = (mask shl 1)
        }
        outputList = outputList.sortedBy { it.devicePin }.toMutableList()
    }

    override fun setPinIndex(index: Int) {
        curOutput = outputList[if (index > 0) index else 0]

        when (curOutput) {
            in atmega328pPortDPins -> {
                outputBit = curOutput.devicePin - PIN_START_PORTD
                if (curOutput.devicePin > 6) { outputBit -= 4 }
                portAddr = IoRegisters.PORTD.addr
                ddrAddr = IoRegisters.DDRD.addr
            }
            in atmega328pPortBPins -> {
                outputBit = curOutput.devicePin - PIN_START_PORTB
                portAddr = IoRegisters.PORTB.addr
                ddrAddr = IoRegisters.DDRB.addr
            }
            in atmega328pPortCPins -> {
                outputBit = curOutput.devicePin - PIN_START_PORTC
                portAddr = IoRegisters.PORTC.addr
                ddrAddr = IoRegisters.DDRC.addr
            }
            else -> {
                //None selected
                outputBit = 0
                portAddr = 0x00u
                ddrAddr = 0x00u
            }
        }

        //User has changed an output, reset meters
        wavePeriod = LongArray(METER_FILTER_SIZE) { 0 }
        highTime = LongArray(METER_FILTER_SIZE) { 0 }

    }

    override fun getPinIndex(): Int {
        val index = outputList.indexOf(curOutput)
        return if (index > 0) index else outputList.indexOf(PinMap.PINX)
    }

    override fun updatePin() {
        if (curOutput == PinMap.PINX) {
            //No pin selected
            pinState = OutputState.TRI_STATE
        } else {
            val portReg = outRegisters[portAddr] ?: 0x00u
            val mask = (0x01 shl outputBit).toUByte()

            val oldPinState = pinState
            pinState = if ((portReg and mask) == 0.toUByte()) {
                OutputState.LOW
            } else {
                OutputState.HIGH
            }

            if (oldPinState != pinState) {
                val timestamp = SystemClock.elapsedRealtimeNanos()
                if (pinState == OutputState.HIGH) {
                    highTime[highTimeIndex] = measureHighTime
                    wavePeriod[wavePeriodIndex] = timestamp - lastRisingEdgeTimestamp
                    lastRisingEdgeTimestamp = timestamp
                    measureHighTime = timestamp
                    highTimeIndex = (highTimeIndex + 1) % METER_FILTER_SIZE
                    wavePeriodIndex = (wavePeriodIndex + 1) % METER_FILTER_SIZE
                } else {
                    measureHighTime = timestamp - measureHighTime
                }
            }
        }
    }

    override fun getPinState(): OutputState {
        return pinState
    }

    override fun getPinNames(): List<String> {
        return outputList.map { it.boardName }
    }

    override fun clone(): OutputInterface {
        return OutputPinV1ATmega328P()
    }

    override fun updateSimulationSpeed() {
        val timestamp = SystemClock.elapsedRealtimeNanos()
        simulationSpeed[simulationSpeedArrayIndex] = timestamp - simulationSpeedTimestamp
        simulationSpeedTimestamp = timestamp
        simulationSpeedArrayIndex = (simulationSpeedArrayIndex + 1) % METER_FILTER_SIZE
    }

    override fun getFrequency(): Double {
        return simulationSpeed.average() / wavePeriod.average()
    }

    override fun getDutyCycle(): Double {
        return highTime.average() / wavePeriod.average()
    }

    companion object {
        var simulationSpeedTimestamp: Long = SystemClock.elapsedRealtimeNanos()
        var simulationSpeed: LongArray = LongArray(METER_FILTER_SIZE) { 0 }
        var simulationSpeedArrayIndex = 0

        var outRegisters = mutableMapOf<UByte, UByte>(
            IoRegisters.DDRB.addr to 0x00u,
            IoRegisters.PORTB.addr to 0x00u,
            IoRegisters.DDRC.addr to 0x00u,
            IoRegisters.PORTC.addr to 0x00u,
            IoRegisters.DDRD.addr to 0x00u,
            IoRegisters.PORTD.addr to 0x00u
        )

        var outputList = mutableListOf(PinMap.PINX)
    }
}