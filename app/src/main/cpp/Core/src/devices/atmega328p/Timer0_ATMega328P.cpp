//
// Created by kollins on 18/04/21.
//

#include "../../../include/devices/atmega328p/Timer0_ATMega328P.h"

#define T0_MASK             0x10
#define WGM_TCCRXA_MASK     0x06
#define WGM_TCCRXB_MASK     0x08

#define OC0A_MASK           0x40
#define OC0B_MASK           0x20

#define BOTTOM              0x00

Timer0_ATMega328P::Timer0_ATMega328P(GenericAVRDataMemory *dataMemory) :
        Timer_ATMega328P(dataMemory) {
    dataMemory->read(PIND_ADDR, &oldPIND);

    bottom =        BOTTOM;
    outARegAddr =   PORTD_ADDR;
    outBRegAddr =   PORTD_ADDR;
    ocxaMask =      OC0A_MASK;
    ocxbMask =      OC0B_MASK;
}

void Timer0_ATMega328P::run() {
    dataMemory->read(TCCR0B_ADDR, &tccrxbReg);
    if ((this->*clockSource[tccrxbReg & CS_MASK])()) {
        dataMemory->read(TCCR0A_ADDR, &tccrxaReg);
        (this->*mode[(tccrxbReg & WGM_TCCRXB_MASK) | ((tccrxaReg << 1) & WGM_TCCRXA_MASK)])();
    }
}

bool Timer0_ATMega328P::clockSource_011() {
    //Prescaler 64
    return !((++clockCount) & PRESCALER_64_MASK);
}

bool Timer0_ATMega328P::clockSource_100() {
    //Prescaler 256
    return !((++clockCount) & PRESCALER_256_MASK);
}

bool Timer0_ATMega328P::clockSource_101() {
    //Prescaler 1024
    return !((++clockCount) & PRESCALER_1024_MASK);
}

bool Timer0_ATMega328P::clockSource_110() {
    bool ret = false;
    sbyte newPIND;
    dataMemory->read(PIND_ADDR, &newPIND);
    if ((oldPIND & (~newPIND)) & T0_MASK) {
        ret = true;
    }
    oldPIND = newPIND;
    return ret;
}

bool Timer0_ATMega328P::clockSource_111() {
    bool ret = false;
    sbyte newPIND;
    dataMemory->read(PIND_ADDR, &newPIND);
    if (((~oldPIND) & newPIND) & T0_MASK) {
        ret = true;
    }
    oldPIND = newPIND;
    return ret;
}

void Timer0_ATMega328P::normal() {
    interrFlags = 0x00;
    matchA = tccrxbReg & FOCXA_MASK;
    matchB = tccrxbReg & FOCXB_MASK;

    dataMemory->read(TCNT0_ADDR, &progress);
    dataMemory->read(OCR0A_ADDR, &ocrxa);
    dataMemory->read(OCR0B_ADDR, &ocrxb);

    Timer_ATMega328P::normal();

    dataMemory->write(TIFR0_ADDR, &interrFlags);
    dataMemory->write(TCNT0_ADDR, &progress);
}
