//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_TIMER2_ATMEGA328P_H
#define PROJECTSOFIA_TIMER2_ATMEGA328P_H

#include "Timer_ATMega328P.h"

class Timer2_ATMega328P : public Timer_ATMega328P {

public:
    Timer2_ATMega328P(GenericAVRDataMemory *dataMemory);
    virtual ~Timer2_ATMega328P() {}
    void run();
};

#endif //PROJECTSOFIA_TIMER2_ATMEGA328P_H
