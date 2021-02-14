package com.kollins.project.sofia.defs.atmega328p

enum class IoRegisters(val addr: Byte) {
    DDRB(0x24),
    PORTB(0x25),
    DDRC(0x27),
    PORTC(0x28),
    DDRD(0x2A),
    PORTD(0x2B)
}