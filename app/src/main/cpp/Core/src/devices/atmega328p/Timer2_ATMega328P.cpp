//
// Created by kollins on 18/04/21.
//

#include "../../../include/devices/atmega328p/Timer2_ATMega328P.h"

#define WGM_TCCRXA_MASK     0x06
#define WGM_TCCRXB_MASK     0x08

#define OC2A_MASK           0x08
#define OC2B_MASK           0x08

#define BOTTOM              0x00

Timer2_ATMega328P::Timer2_ATMega328P(GenericAVRDataMemory *dataMemory) :
        Timer_ATMega328P(dataMemory) {
    bottom =        BOTTOM;
    outARegAddr =   PORTD_ADDR;
    outBRegAddr =   PORTB_ADDR;
    ocxaMask =      OC2A_MASK;
    ocxbMask =      OC2B_MASK;
}

void Timer2_ATMega328P::run() {
    dataMemory->read(TCCR2B_ADDR, &tccrxbReg);
    if ((this->*clockSource[tccrxbReg & CS_MASK])()) {
        dataMemory->read(TCCR2A_ADDR, &tccrxaReg);
        (this->*mode[(tccrxbReg & WGM_TCCRXB_MASK) | ((tccrxaReg << 1) & WGM_TCCRXA_MASK)])();
    }
}

bool Timer2_ATMega328P::clockSource_011() {
    //Prescaler 32
    return !((++clockCount) & PRESCALER_32_MASK);
}

bool Timer2_ATMega328P::clockSource_100() {
    //Prescaler 64
    return !((++clockCount) & PRESCALER_64_MASK);
}

bool Timer2_ATMega328P::clockSource_101() {
    //Prescaler 128
    return !((++clockCount) & PRESCALER_128_MASK);
}

bool Timer2_ATMega328P::clockSource_110() {
    //Prescaler 256
    return !((++clockCount) & PRESCALER_256_MASK);
}

bool Timer2_ATMega328P::clockSource_111() {
    //Prescaler 1024
    return !((++clockCount) & PRESCALER_1024_MASK);
}

void Timer2_ATMega328P::normal() {
    interrFlags = 0x00;
    matchA = tccrxbReg & FOCXA_MASK;
    matchB = tccrxbReg & FOCXB_MASK;

    dataMemory->read(TCNT2_ADDR, &progress);
    dataMemory->read(OCR2A_ADDR, &ocrxa);
    dataMemory->read(OCR2B_ADDR, &ocrxb);

    Timer_ATMega328P::normal();

    dataMemory->write(TIFR2_ADDR, &interrFlags);
    dataMemory->write(TCNT2_ADDR, &progress);
}
