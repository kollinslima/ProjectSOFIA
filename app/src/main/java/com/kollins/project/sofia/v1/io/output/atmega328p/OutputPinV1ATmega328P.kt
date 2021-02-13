package com.kollins.project.sofia.v1.io.output.atmega328p

import android.os.SystemClock
import android.util.Log
import com.kollins.project.sofia.defs.atmega328p.PinMap
import com.kollins.project.sofia.defs.atmega328p.atmega328pPortBPins
import com.kollins.project.sofia.defs.atmega328p.atmega328pPortCPins
import com.kollins.project.sofia.defs.atmega328p.atmega328pPortDPins
import com.kollins.project.sofia.interfaces.io.OutputInterface
import com.kollins.project.sofia.interfaces.io.OutputState
import kotlin.experimental.and

enum class OutputAddr(val addr: Byte) {
    DDRB(0x24),
    PORTB(0x25),
    DDRC(0x27),
    PORTC(0x28),
    DDRD(0x2A),
    PORTD(0x2B)
}

private const val METER_FILTER_SIZE = 2
private const val PIN_START_PORTD = 2
private const val PIN_START_PORTB = 8
private const val PIN_START_PORTC = 23

class OutputPinV1ATmega328P : OutputInterface {
    private var curOutput = PinMap.PINX
    private var portAddr: Byte = 0x00
    private var ddrAddr: Byte = 0x00
    private var outputBit = 0
    private var pinState = OutputState.TRI_STATE

    private var highTime: LongArray = LongArray(METER_FILTER_SIZE) { 0 }
    private var highTimeIndex = 0
    private var wavePeriod: LongArray = LongArray(METER_FILTER_SIZE) { 0 }
    private var wavePeriodIndex = 0
    private var measureHighTime: Long = 0
    private var lastRisingEdgeTimestamp: Long = 0


    override fun outputUpdate(change: String) {
        val splittedChange: List<String> = change.split(":")
        val register = splittedChange[0].toByte()
        val value = splittedChange[1].toByte()
        outRegisters[register] = value

        updateOutputList(register, value)
    }

    private fun updateOutputList(register: Byte, value: Byte) {
        when (register) {
            OutputAddr.DDRB.addr -> {
                addOutputPins(value, atmega328pPortBPins)
            }
            OutputAddr.DDRD.addr -> {
                addOutputPins(value, atmega328pPortDPins)
            }
            OutputAddr.DDRC.addr -> {
                addOutputPins(value, atmega328pPortCPins)
            }
        }
    }

    private fun addOutputPins(value: Byte, atmega328pPortPins: List<PinMap>) {
        var i = 0
        var mask = 0x01
        while (i < atmega328pPortPins.size) {
            if ((value and mask.toByte()) == 0.toByte()) {
                outputList.remove(atmega328pPortPins[i])
            } else {
                outputList.add(0, atmega328pPortPins[i])
            }
            i++
            mask = (mask shl 1)
        }
    }

    override fun setOutputIndex(position: Int) {
        curOutput = outputList[if (position > 0) position else 0]

        when (curOutput) {
            in atmega328pPortDPins -> {
                outputBit = curOutput.devicePin - PIN_START_PORTD
                portAddr = OutputAddr.PORTD.addr
                ddrAddr = OutputAddr.DDRD.addr
            }
            in atmega328pPortBPins -> {
                outputBit = curOutput.devicePin - PIN_START_PORTB
                portAddr = OutputAddr.PORTB.addr
                ddrAddr = OutputAddr.DDRB.addr
            }
            in atmega328pPortCPins -> {
                outputBit = curOutput.devicePin - PIN_START_PORTC
                portAddr = OutputAddr.PORTC.addr
                ddrAddr = OutputAddr.DDRC.addr
            }
            else -> {
                //None selected
                outputBit = 0
                portAddr = 0x00
                ddrAddr = 0x00
            }
        }

        //User has changed an output, reset meters
        wavePeriod = LongArray(METER_FILTER_SIZE) { 0 }
        highTime = LongArray(METER_FILTER_SIZE) { 0 }

    }

    override fun getOutputIndex(): Int {
        val index = outputList.indexOf(curOutput)
        return if (index > 0) index else 0
    }

    override fun updatePin() {
        if (curOutput == PinMap.PINX) {
            //No pin selected
            pinState = OutputState.TRI_STATE
        } else {
            val portReg = outRegisters[portAddr] ?: 0x00
            val mask = (0x01 shl outputBit).toByte()

            pinState = if ((portReg and mask) == 0.toByte()) {
                OutputState.LOW
            } else {
                OutputState.HIGH
            }

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

        var outRegisters = mutableMapOf<Byte, Byte>(
            OutputAddr.DDRB.addr to 0x00,
            OutputAddr.PORTB.addr to 0x00,
            OutputAddr.DDRC.addr to 0x00,
            OutputAddr.PORTC.addr to 0x00,
            OutputAddr.DDRD.addr to 0x00,
            OutputAddr.PORTD.addr to 0x00
        )

        val outputList = mutableListOf(PinMap.PINX)
    }
}