//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_TIMER1_ATMEGA328P_H
#define PROJECTSOFIA_TIMER1_ATMEGA328P_H

#include "Timer_ATMega328P.h"

class Timer1_ATMega328P : public Timer_ATMega328P {

public:
    Timer1_ATMega328P(GenericAVRDataMemory *dataMemory);
    virtual ~Timer1_ATMega328P() {}
    void run();
};

#endif //PROJECTSOFIA_TIMER1_ATMEGA328P_H
