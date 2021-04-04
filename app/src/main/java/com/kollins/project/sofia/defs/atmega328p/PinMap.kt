package com.kollins.project.sofia.defs.atmega328p

//Map Arduino names to ATMega328P pins
enum class PinMap(val boardName: String, val devicePin: Int) {
    PINX("None", -1),

    PIN0("Pin0", 2),
    PIN1("Pin1", 3),
    PIN2("Pin2", 4),
    PIN3("Pin3", 5),
    PIN4("Pin4", 6),
    PIN5("Pin5", 11),
    PIN6("Pin6", 12),
    PIN7("Pin7", 13),

    PIN8("Pin8", 14),
    PIN9("Pin9", 15),
    PIN10("Pin10", 16),
    PIN11("Pin11", 17),
    PIN12("Pin12", 18),
    PIN13("Pin13", 19),

    A0("A0", 23),
    A1("A1", 24),
    A2("A2", 25),
    A3("A3", 26),
    A4("A4", 27),
    A5("A5", 28),
}

val atmega328pPinList = PinMap.values()

val atmega328pPortDPins = listOf(
    PinMap.PIN0,
    PinMap.PIN1,
    PinMap.PIN2,
    PinMap.PIN3,
    PinMap.PIN4,
    PinMap.PIN5,
    PinMap.PIN6,
    PinMap.PIN7
)

val atmega328pPortBPins = listOf(
    PinMap.PIN8,
    PinMap.PIN9,
    PinMap.PIN10,
    PinMap.PIN11,
    PinMap.PIN12,
    PinMap.PIN13
)

val atmega328pPortCPins = listOf(
    PinMap.A0,
    PinMap.A1,
    PinMap.A2,
    PinMap.A3,
    PinMap.A4,
    PinMap.A5
)