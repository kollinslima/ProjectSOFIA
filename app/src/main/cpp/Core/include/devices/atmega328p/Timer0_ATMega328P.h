//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_TIMER0_ATMEGA328P_H
#define PROJECTSOFIA_TIMER0_ATMEGA328P_H

#include "Timer_ATMega328P.h"

class Timer0_ATMega328P : public Timer_ATMega328P {

public:
    Timer0_ATMega328P(GenericAVRDataMemory *dataMemory);
    virtual ~Timer0_ATMega328P() {}
    void run();
};

#endif //PROJECTSOFIA_TIMER0_ATMEGA328P_H
