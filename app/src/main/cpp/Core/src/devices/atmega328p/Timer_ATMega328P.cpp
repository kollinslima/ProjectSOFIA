//
// Created by kollins on 18/04/21.
//

#include "../../../include/devices/atmega328p/Timer_ATMega328P.h"

#define NORM_TOG_CPR_MATCH      0x01
#define NORM_CLR_CPR_MATCH      0x02
#define NORM_SET_CPR_MATCH      0x03

#define OVERFLOW_INTERRUPT      0x01
#define CPR_MATH_A_INTERRUPT    0x02
#define CPR_MATH_B_INTERRUPT    0x03

Timer_ATMega328P::Timer_ATMega328P(GenericAVRDataMemory *dataMemory) {
    this->dataMemory = dataMemory;
    clockCount = 0;

    tccrxaReg = 0x00;
    tccrxbReg = 0x00;
    outReg = 0x00;

    progress = 0x0000;
    ocrxa = 0x0000;
    ocrxb = 0x0000;
    matchA = false;
    matchB = false;

    interrFlags = 0x00;

    setupClockSourceDecoder();
    setupOperationMode();
}

void Timer_ATMega328P::setupClockSourceDecoder() {
    clockSource[0] = &Timer_ATMega328P::clockSource_000;
    clockSource[1] = &Timer_ATMega328P::clockSource_001;
    clockSource[2] = &Timer_ATMega328P::clockSource_010;
    clockSource[3] = &Timer_ATMega328P::clockSource_011;
    clockSource[4] = &Timer_ATMega328P::clockSource_100;
    clockSource[5] = &Timer_ATMega328P::clockSource_101;
    clockSource[6] = &Timer_ATMega328P::clockSource_110;
    clockSource[7] = &Timer_ATMega328P::clockSource_111;
}

void Timer_ATMega328P::setupOperationMode() {
    mode[0] = &Timer_ATMega328P::normal;
    mode[1] = &Timer_ATMega328P::pwmPhaseCorrect1;
    mode[2] = &Timer_ATMega328P::pwmPhaseCorrect2;
    mode[3] = &Timer_ATMega328P::pwmPhaseCorrect3;
    mode[4] = &Timer_ATMega328P::ctc1;
    mode[5] = &Timer_ATMega328P::fastPWM1;
    mode[6] = &Timer_ATMega328P::fastPWM2;
    mode[7] = &Timer_ATMega328P::fastPWM3;
    mode[8] = &Timer_ATMega328P::pwmPhaseAndFreqCorrect1;
    mode[9] = &Timer_ATMega328P::pwmPhaseAndFreqCorrect2;
    mode[10] = &Timer_ATMega328P::pwmPhaseCorrect4;
    mode[11] = &Timer_ATMega328P::pwmPhaseCorrect5;
    mode[12] = &Timer_ATMega328P::ctc2;
    mode[13] = &Timer_ATMega328P::reserved;
    mode[14] = &Timer_ATMega328P::fastPWM4;
    mode[15] = &Timer_ATMega328P::fastPWM5;
}

bool Timer_ATMega328P::clockSource_000() {
    //No clock
    return false;
}

bool Timer_ATMega328P::clockSource_001() {
    //No Prescaler
    return true;
}

bool Timer_ATMega328P::clockSource_010() {
    //Prescaler 8
    return !((++clockCount) & PRESCALER_8_MASK);
}

void Timer_ATMega328P::normal() {
    progress++;

    if (progress == bottom) {
        interrFlags |= OVERFLOW_INTERRUPT;
    }
    if (!matchA && progress == ocrxa) {
        interrFlags |= CPR_MATH_A_INTERRUPT;
        matchA = true;
    }
    if (!matchB && progress == ocrxb) {
        interrFlags |= CPR_MATH_B_INTERRUPT;
        matchB = true;
    }
    if (matchA) {
        switch (tccrxaReg&COMXA_MASK) {
            case NORM_TOG_CPR_MATCH:
                dataMemory->read(outARegAddr, &outReg);
                outReg = (outReg&ocxaMask)?outReg&(~ocxaMask):outReg|ocxaMask;
                dataMemory->write(outARegAddr, &outReg);
                break;
            case NORM_CLR_CPR_MATCH:
                dataMemory->read(outARegAddr, &outReg);
                outReg = outReg&(~ocxaMask);
                dataMemory->write(outARegAddr, &outReg);
                break;
            case NORM_SET_CPR_MATCH:
                dataMemory->read(outARegAddr, &outReg);
                outReg = outReg|ocxaMask;
                dataMemory->write(outARegAddr, &outReg);
                break;
        }
    }
    if (matchB) {
        switch (tccrxaReg&COMXB_MASK) {
            case NORM_TOG_CPR_MATCH:
                dataMemory->read(outBRegAddr, &outReg);
                outReg = (outReg&ocxbMask)?outReg&(~ocxbMask):outReg|ocxbMask;
                dataMemory->write(outBRegAddr, &outReg);
                break;
            case NORM_CLR_CPR_MATCH:
                dataMemory->read(outBRegAddr, &outReg);
                outReg = outReg&(~ocxbMask);
                dataMemory->write(outBRegAddr, &outReg);
                break;
            case NORM_SET_CPR_MATCH:
                dataMemory->read(outBRegAddr, &outReg);
                outReg = outReg|ocxbMask;
                dataMemory->write(outBRegAddr, &outReg);
                break;
        }
    }
}

void Timer_ATMega328P::pwmPhaseCorrect1() {

}

void Timer_ATMega328P::pwmPhaseCorrect2() {

}

void Timer_ATMega328P::pwmPhaseCorrect3() {

}

void Timer_ATMega328P::ctc1() {

}

void Timer_ATMega328P::fastPWM1() {

}

void Timer_ATMega328P::fastPWM2() {

}

void Timer_ATMega328P::fastPWM3() {

}

void Timer_ATMega328P::pwmPhaseAndFreqCorrect1() {

}

void Timer_ATMega328P::pwmPhaseAndFreqCorrect2() {

}

void Timer_ATMega328P::pwmPhaseCorrect4() {

}

void Timer_ATMega328P::pwmPhaseCorrect5() {

}

void Timer_ATMega328P::ctc2() {

}

void Timer_ATMega328P::reserved() {

}

void Timer_ATMega328P::fastPWM4() {

}

void Timer_ATMega328P::fastPWM5() {

}
