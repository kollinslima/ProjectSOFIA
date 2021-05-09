package com.kollins.project.sofia.defs.atmega328p

@ExperimentalUnsignedTypes
enum class IoRegisters(val addr: UByte) {
    DDRB(0x24u),
    PORTB(0x25u),
    DDRC(0x27u),
    PORTC(0x28u),
    DDRD(0x2Au),
    PORTD(0x2Bu)
}