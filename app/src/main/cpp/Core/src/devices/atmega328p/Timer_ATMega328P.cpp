//
// Created by kollins on 18/04/21.
//

#include "../../../include/devices/atmega328p/Timer_ATMega328P.h"

#define NON_PWM_TOG_CPR_MATCH_A                     0x40
#define NON_PWM_CLR_CPR_MATCH_A                     0x80
#define NON_PWM_SET_CPR_MATCH_A                     0xC0

#define NON_PWM_TOG_CPR_MATCH_B                     0x10
#define NON_PWM_CLR_CPR_MATCH_B                     0x20
#define NON_PWM_SET_CPR_MATCH_B                     0x30

#define PHASE_FREQ_PWM_TOG_MATCH_A                  0x40
#define PHASE_FREQ_PWM_CLR_UP_SET_DOWN_MATCH_A      0x80
#define PHASE_FREQ_PWM_SET_UP_CLR_DOWN_MATCH_A      0xC0

#define PHASE_FREQ_PWM_CLR_UP_SET_DOWN_MATCH_B      0x20
#define PHASE_FREQ_PWM_SET_UP_CLR_DOWN_MATCH_B      0x30

#define FAST_PWM_CLR_SET_BOTTOM_MATCH_A             0x80
#define FAST_PWM_SET_CLR_BOTTOM_MATCH_A             0xC0

#define FAST_PWM_CLR_SET_BOTTOM_MATCH_B             0x20
#define FAST_PWM_SET_CLR_BOTTOM_MATCH_B             0x30

#define OVERFLOW_INTERRUPT                          0x01
#define CPR_MATH_A_INTERRUPT                        0x02
#define CPR_MATH_B_INTERRUPT                        0x03

Timer_ATMega328P::Timer_ATMega328P(DataMemory_ATMega328P &dataMemory) :
        datMem(dataMemory) {

    clockCount = 0;

    tccrxaReg = 0x00;
    tccrxbReg = 0x00;

    progress = 0x0000;
    ocrxa = 0x0000;
    ocrxb = 0x0000;
    icrx = 0x0000;
    matchA = false;
    matchB = false;
    upCount = true;

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

void Timer_ATMega328P::run() {
    tccrxbReg = datMem.buffer[tccrxBAddr];
    if ((this->*clockSource[tccrxbReg & CS_MASK])()) {
        tccrxaReg = datMem.buffer[tccrxAAddr];
        prepare(); operate(); writeBack();
    }
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

void Timer_ATMega328P::normalCtc(sword16 top) {
    if (progress == endOfScale) {
        interrFlags |= OVERFLOW_INTERRUPT;
        progress = bottom;
    } else if (progress == top) {
        progress = bottom;
    } else {
        progress++;
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
        switch (tccrxaReg & COMXA_MASK) {
            case NON_PWM_TOG_CPR_MATCH_A:
                datMem.buffer[outARegAddr] = (datMem.buffer[outARegAddr] & ocxaMask) ?
                                             datMem.buffer[outARegAddr] & (~ocxaMask) :
                                             datMem.buffer[outARegAddr] | ocxaMask;
                break;
            case NON_PWM_CLR_CPR_MATCH_A:
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] & (~ocxaMask);;
                break;
            case NON_PWM_SET_CPR_MATCH_A:
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] | ocxaMask;
                break;
        }
    }
    if (matchB) {
        switch (tccrxaReg & COMXB_MASK) {
            case NON_PWM_TOG_CPR_MATCH_B:
                datMem.buffer[outBRegAddr] = (datMem.buffer[outBRegAddr] & ocxbMask) ?
                                             datMem.buffer[outBRegAddr] & (~ocxbMask) :
                                             datMem.buffer[outBRegAddr] | ocxbMask;
                break;
            case NON_PWM_CLR_CPR_MATCH_B:
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] & (~ocxbMask);
                break;
            case NON_PWM_SET_CPR_MATCH_B:
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] | ocxbMask;
                break;
        }
    }
}

void Timer_ATMega328P::pwmDualSlope(sword16 top, bool ocxaToggleEnable) {
    progress += upCount ? 1 : -1;

    if (!matchA && progress == ocrxa) {
        interrFlags |= CPR_MATH_A_INTERRUPT;
        matchA = true;
    }
    if (!matchB && progress == ocrxb) {
        interrFlags |= CPR_MATH_B_INTERRUPT;
        matchB = true;
    }


    switch (tccrxaReg & COMXA_MASK) {
        case PHASE_FREQ_PWM_TOG_MATCH_A:
            if (ocxaToggleEnable && matchA){
                datMem.buffer[outARegAddr] = (datMem.buffer[outARegAddr] & ocxaMask) ?
                                             datMem.buffer[outARegAddr] & (~ocxaMask) :
                                             datMem.buffer[outARegAddr] | ocxaMask;
            }
            break;
        case PHASE_FREQ_PWM_CLR_UP_SET_DOWN_MATCH_A:
            if (ocrxa == endOfScale) {
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] | ocxaMask;
            } else if (ocrxa == bottom) {
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] & (~ocxaMask);
            } else if (matchA) {
                datMem.buffer[outARegAddr] = upCount ? datMem.buffer[outARegAddr] & (~ocxaMask) :
                                             datMem.buffer[outARegAddr] | ocxaMask;
            }
            break;
        case PHASE_FREQ_PWM_SET_UP_CLR_DOWN_MATCH_A:
            if (ocrxa == endOfScale) {
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] & (~ocxaMask);
            } else if (ocrxa == bottom) {
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] | ocxaMask;
            } else if (matchA) {
                datMem.buffer[outARegAddr] = upCount ? datMem.buffer[outARegAddr] | ocxaMask :
                                             datMem.buffer[outARegAddr] & (~ocxaMask);
            }
            break;
    }

    switch (tccrxaReg & COMXB_MASK) {
        case PHASE_FREQ_PWM_CLR_UP_SET_DOWN_MATCH_B:
            if (ocrxb == endOfScale) {
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] | ocxbMask;
            } else if (ocrxb == bottom) {
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] & (~ocxbMask);
            } else if (matchA) {
                datMem.buffer[outBRegAddr] = upCount ? datMem.buffer[outBRegAddr] & (~ocxbMask) :
                                             datMem.buffer[outBRegAddr] | ocxbMask;
            }
            break;
        case PHASE_FREQ_PWM_SET_UP_CLR_DOWN_MATCH_B:
            if (ocrxb == endOfScale) {
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] & (~ocxbMask);
            } else if (ocrxb == bottom) {
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] | ocxbMask;
            } else if (matchA) {
                datMem.buffer[outBRegAddr] = upCount ? datMem.buffer[outBRegAddr] | ocxbMask :
                                             datMem.buffer[outBRegAddr] & (~ocxbMask);
            }
            break;
    }

    if (progress == bottom) {
        interrFlags |= OVERFLOW_INTERRUPT;
        upCount = true;
    } else if (progress == top) {
        upCount = false;
    }
}

void Timer_ATMega328P::pwmSingleSlope(sword16 top) {
    if (progress == top) {
        interrFlags |= OVERFLOW_INTERRUPT;
        progress = bottom;
    } else {
        progress++;
    }

    if (!matchA && progress == ocrxa) {
        interrFlags |= CPR_MATH_A_INTERRUPT;
        matchA = true;
    }
    if (!matchB && progress == ocrxb) {
        interrFlags |= CPR_MATH_B_INTERRUPT;
        matchB = true;
    }

    switch (tccrxaReg & COMXA_MASK) {
        case FAST_PWM_CLR_SET_BOTTOM_MATCH_A:
            if (ocrxa == endOfScale) {
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] | ocxaMask;
            } else if (matchA) {
                datMem.buffer[outARegAddr] = (progress == bottom) ?
                                             datMem.buffer[outARegAddr] & (~ocxaMask) :
                                             datMem.buffer[outARegAddr] | ocxaMask;
            }
            break;
        case FAST_PWM_SET_CLR_BOTTOM_MATCH_A:
            if (ocrxa == endOfScale) {
                datMem.buffer[outARegAddr] = datMem.buffer[outARegAddr] & (~ocxaMask);
            } else if (matchA) {
                datMem.buffer[outARegAddr] = (progress == bottom) ?
                                             datMem.buffer[outARegAddr] | ocxaMask :
                                             datMem.buffer[outARegAddr] & (~ocxaMask);
            }
            break;
    }

    switch (tccrxaReg & COMXB_MASK) {
        case FAST_PWM_CLR_SET_BOTTOM_MATCH_B:
            if (ocrxb == endOfScale) {
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] | ocxbMask;
            } else if (matchA) {
                datMem.buffer[outBRegAddr] = (progress == bottom) ?
                                             datMem.buffer[outBRegAddr] & (~ocxbMask) :
                                             datMem.buffer[outBRegAddr] | ocxbMask;
            }
            break;
        case FAST_PWM_SET_CLR_BOTTOM_MATCH_B:
            if (ocrxb == endOfScale) {
                datMem.buffer[outBRegAddr] = datMem.buffer[outBRegAddr] & (~ocxbMask);
            } else if (matchA) {
                datMem.buffer[outBRegAddr] = (progress == bottom) ?
                                             datMem.buffer[outBRegAddr] | ocxbMask :
                                             datMem.buffer[outBRegAddr] & (~ocxbMask);
            }
            break;
    }
}

void Timer_ATMega328P::normal() {
    normalCtc(endOfScale);
}

void Timer_ATMega328P::pwmPhaseCorrect1() {
    pwmDualSlope(endOfScale);
}

void Timer_ATMega328P::pwmPhaseCorrect2() {
    pwmDualSlope(endOfScale);
}

void Timer_ATMega328P::pwmPhaseCorrect3() {
    pwmDualSlope(endOfScale);
}

void Timer_ATMega328P::ctc1() {
    normalCtc(ocrxa);
}

void Timer_ATMega328P::fastPWM1() {
    pwmSingleSlope(endOfScale);
}

void Timer_ATMega328P::fastPWM2() {
    pwmSingleSlope(endOfScale);
}

void Timer_ATMega328P::fastPWM3() {
    pwmSingleSlope(endOfScale);
}

void Timer_ATMega328P::pwmPhaseAndFreqCorrect1() {
    pwmDualSlope(icrx);
}

void Timer_ATMega328P::pwmPhaseAndFreqCorrect2() {
    pwmDualSlope(ocrxa, true);
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
