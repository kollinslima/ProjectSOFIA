//
// Created by kollins on 18/04/21.
//

#include "../../../include/devices/atmega328p/Timer1_ATMega328P.h"

#define T1_MASK             0x20
#define WGM_TCCRXA_MASK     0x03
#define WGM_TCCRXB_MASK     0x0C

#define OC1A_MASK           0x02
#define OC1B_MASK           0x04

#define BOTTOM              0x0000

Timer1_ATMega328P::Timer1_ATMega328P(GenericAVRDataMemory *dataMemory) :
        Timer_ATMega328P(dataMemory) {
    dataMemory->read(PIND_ADDR, &oldPIND);

    bottom =        BOTTOM;
    outARegAddr =   PORTB_ADDR;
    outBRegAddr =   PORTB_ADDR;
    ocxaMask =      OC1A_MASK;
    ocxbMask =      OC1B_MASK;
}

void Timer1_ATMega328P::run() {
    dataMemory->read(TCCR0B_ADDR, &tccrxbReg);
    if ((this->*clockSource[tccrxbReg & CS_MASK])()) {
        dataMemory->read(TCCR0A_ADDR, &tccrxaReg);
        (this->*mode[((tccrxbReg>>1) & WGM_TCCRXB_MASK) | (tccrxaReg & WGM_TCCRXA_MASK)])();
    }
}

bool Timer1_ATMega328P::clockSource_011() {
    //Prescaler 64
    return !((++clockCount) & PRESCALER_64_MASK);
}

bool Timer1_ATMega328P::clockSource_100() {
    //Prescaler 256
    return !((++clockCount) & PRESCALER_256_MASK);
}

bool Timer1_ATMega328P::clockSource_101() {
    //Prescaler 1024
    return !((++clockCount) & PRESCALER_1024_MASK);
}

bool Timer1_ATMega328P::clockSource_110() {
    bool ret = false;
    sbyte newPIND;
    dataMemory->read(PIND_ADDR, &newPIND);
    if ((oldPIND & (~newPIND)) & T1_MASK) {
        ret = true;
    }
    oldPIND = newPIND;
    return ret;
}

bool Timer1_ATMega328P::clockSource_111() {
    bool ret = false;
    sbyte newPIND;
    dataMemory->read(PIND_ADDR, &newPIND);
    if (((~oldPIND) & newPIND) & T1_MASK) {
        ret = true;
    }
    oldPIND = newPIND;
    return ret;
}

void Timer1_ATMega328P::normal() {
    interrFlags = 0x00;
    matchA = tccrxbReg & FOCXA_MASK;
    matchB = tccrxbReg & FOCXB_MASK;

    dataMemory->read(TCNT1H_ADDR, &progress);
    progress = progress<<8;
    dataMemory->read(TCNT1L_ADDR, &progress);

    dataMemory->read(OCR1AH_ADDR, &ocrxa);
    ocrxa = ocrxa<<8;
    dataMemory->read(OCR1AL_ADDR, &ocrxa);

    dataMemory->read(OCR1BH_ADDR, &ocrxb);
    ocrxb = ocrxb<<8;
    dataMemory->read(OCR1BL_ADDR, &ocrxb);

    Timer_ATMega328P::normal();

    dataMemory->write(TIFR1_ADDR, &interrFlags);
    dataMemory->write(TCNT1L_ADDR, &progress);
    progress = progress>>8;
    dataMemory->write(TCNT1H_ADDR, &progress);
}
