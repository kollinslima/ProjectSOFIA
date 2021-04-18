//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_TIMER_ATMEGA328P_H
#define PROJECTSOFIA_TIMER_ATMEGA328P_H

#include "../components/avr/timer/GenericAVRTimer.h"
#include "../components/avr/memory/GenericAVRDataMemory.h"

class Timer_ATMega328P : public GenericAVRTimer {

public:
    Timer_ATMega328P(GenericAVRDataMemory *dataMemory);
    virtual ~Timer_ATMega328P() {}

private:
    GenericAVRDataMemory *dataMemory;
};

#endif //PROJECTSOFIA_TIMER_ATMEGA328P_H
