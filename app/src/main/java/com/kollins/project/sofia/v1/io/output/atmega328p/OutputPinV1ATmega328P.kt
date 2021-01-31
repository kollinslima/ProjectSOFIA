package com.kollins.project.sofia.v1.io.output.atmega328p

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

class OutputPinV1ATmega328P : OutputInterface {
    private var index = defaultIndex
    private var portAddr = OutputAddr.PORTB.addr
    private var ddrAddr = OutputAddr.DDRB.addr
    private var outputBit = 6
    private var pinState = OutputState.TRI_STATE
    private var haveMeter = false

    override fun outputUpdate(change:String) {
        val splittedChange: List<String> = change.split(":")
        outRegisters[splittedChange[0].toByte()] = splittedChange[1].toByte()
    }

    override fun setOutputIndex(position: Int) {
        index = position
        when (index) {
            in 0..7 -> {
                outputBit = index
                portAddr = OutputAddr.PORTD.addr
                ddrAddr = OutputAddr.DDRD.addr
            }
            in 8..13 -> {
                outputBit = position - 8
                portAddr = OutputAddr.PORTB.addr
                ddrAddr = OutputAddr.DDRB.addr
            }
            else -> {
                outputBit = position - 14
                portAddr = OutputAddr.PORTC.addr
                ddrAddr = OutputAddr.DDRC.addr
            }
        }
    }

    override fun getOutputIndex(): Int {
        return index
    }

    override fun updatePinState() {
        val ddrReg = outRegisters[ddrAddr] ?: 0x00
        val portReg = outRegisters[portAddr] ?: 0x00
        val mask = (0x01 shl index).toByte()

        if ((ddrReg and mask) == 0.toByte()) {
            //Pin is configured as Input
            pinState = if ((portReg and mask) == 0.toByte()) {
                OutputState.TRI_STATE
            } else {
                if (pullUpDisabled) {
                    OutputState.TRI_STATE
                } else {
                    OutputState.HIGH
                }
            }
        } else {
            //Pin is configured as Output
            pinState = if ((portReg and mask) == 0.toByte()) {
                OutputState.LOW
            } else {
                OutputState.HIGH
            }
        }
    }

    override fun getPinState(): OutputState {
        return pinState
    }

    override fun getPinNames(): List<String> {
        return pinNames
    }

    override fun clone(): OutputInterface {
        return OutputPinV1ATmega328P()
    }

    companion object {
        const val defaultIndex = 13
        var pullUpDisabled = false
        var outRegisters = mutableMapOf<Byte, Byte>(
            OutputAddr.DDRB.addr to 0x00,
            OutputAddr.PORTB.addr to 0x00,
            OutputAddr.DDRC.addr to 0x00,
            OutputAddr.PORTC.addr to 0x00,
            OutputAddr.DDRD.addr to 0x00,
            OutputAddr.PORTD.addr to 0x00
        )

        val pinNames = listOf(
            "Pin0",
            "Pin1",
            "Pin2",
            "Pin3",
            "Pin4",
            "Pin5",
            "Pin6",
            "Pin7",
            "Pin8",
            "Pin9",
            "Pin10",
            "Pin11",
            "Pin12",
            "Pin13",
            "A0",
            "A1",
            "A2",
            "A3",
            "A4",
            "A5"
        )
    }
}