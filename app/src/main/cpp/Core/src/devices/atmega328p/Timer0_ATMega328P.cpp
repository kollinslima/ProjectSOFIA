//
// Created by kollins on 18/04/21.
//

#include "../../../include/devices/atmega328p/Timer0_ATMega328P.h"

#define T0_MASK             0x10

#define OC0A_MASK           0x40
#define OC0B_MASK           0x20

#define TOP                 0xFF
#define BOTTOM              0x00

Timer0_ATMega328P::Timer0_ATMega328P(DataMemory_ATMega328P &dataMemory) :
        Timer_ATMega328P(dataMemory) {

    oldPIND = datMem.buffer[PIND_ADDR];

    endOfScale = TOP;
    top = endOfScale;
    bottom = BOTTOM;
    outARegAddr = PORTD_ADDR;
    outBRegAddr = PORTD_ADDR;
    tccrxAAddr = TCCR0A_ADDR;
    tccrxBAddr = TCCR0B_ADDR;
    ocxaMask = OC0A_MASK;
    ocxbMask = OC0B_MASK;
}

void Timer0_ATMega328P::prepare() {
    interrFlags = 0x00;
    matchA = tccrxbReg & FOCXA_MASK;
    matchB = tccrxbReg & FOCXB_MASK;

    progress = datMem.buffer[TCNT0_ADDR];
}

void Timer0_ATMega328P::operate() {
    (this->*mode[(tccrxbReg & WGM_TCCR0B_MASK) | ((tccrxaReg << 1) & WGM_TCCR0A_MASK)])();
}

void Timer0_ATMega328P::writeBack() {
    datMem.buffer[TIFR0_ADDR] = interrFlags;
    datMem.buffer[TCNT0_ADDR] = progress;
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
    sbyte newPIND = datMem.buffer[PIND_ADDR];
    if ((oldPIND & (~newPIND)) & T0_MASK) {
        ret = true;
    }
    oldPIND = newPIND;
    return ret;
}

bool Timer0_ATMega328P::clockSource_111() {
    bool ret = false;
    sbyte newPIND = datMem.buffer[PIND_ADDR];
    if (((~oldPIND) & newPIND) & T0_MASK) {
        ret = true;
    }
    oldPIND = newPIND;
    return ret;
}

void Timer0_ATMega328P::normal() {
    ocrxa = datMem.buffer[OCR0A_ADDR];
    ocrxb = datMem.buffer[OCR0B_ADDR];
    top = endOfScale;
    Timer_ATMega328P::normal();
}

void Timer0_ATMega328P::pwmPhaseCorrect2() {
    ocrxa = datMem.buffer[OCR0A_ADDR];
    ocrxb = datMem.buffer[OCR0B_ADDR];

    top = endOfScale;

    Timer_ATMega328P::pwmPhaseCorrect2();

    if (progress == top) {
        //Update double buffer on TOP
        datMem.buffer[OCR0A_ADDR] = datMem.doubleBuffer[DOUBLE_BUFFER_OCR0A];
        datMem.buffer[OCR0B_ADDR] = datMem.doubleBuffer[DOUBLE_BUFFER_OCR0B];
    }
}

void Timer0_ATMega328P::ctc1() {
    ocrxa = datMem.buffer[OCR0A_ADDR];
    ocrxb = datMem.buffer[OCR0B_ADDR];

    top = ocrxa;

    Timer_ATMega328P::normal();
}

void Timer0_ATMega328P::fastPWM2() {
    ocrxa = datMem.buffer[OCR0A_ADDR];
    ocrxb = datMem.buffer[OCR0B_ADDR];

    top = endOfScale;

    Timer_ATMega328P::fastPWM2();

    if (progress == BOTTOM) {
        //Update double buffer at the BOTTOM
        datMem.buffer[OCR0A_ADDR] = datMem.doubleBuffer[DOUBLE_BUFFER_OCR0A];
        datMem.buffer[OCR0B_ADDR] = datMem.doubleBuffer[DOUBLE_BUFFER_OCR0B];
    }
}

void Timer0_ATMega328P::pwmPhaseAndFreqCorrect1() {
    //Reserved for Timer 0
}

void Timer0_ATMega328P::pwmPhaseCorrect4() {
    ocrxa = datMem.buffer[OCR0A_ADDR];
    ocrxb = datMem.buffer[OCR0B_ADDR];

    top = ocrxa;

    Timer_ATMega328P::pwmPhaseCorrect4();

    if (progress == top) {
        //Update double buffer on TOP
        datMem.buffer[OCR0A_ADDR] = datMem.doubleBuffer[DOUBLE_BUFFER_OCR0A];
        datMem.buffer[OCR0B_ADDR] = datMem.doubleBuffer[DOUBLE_BUFFER_OCR0B];
    }
}
