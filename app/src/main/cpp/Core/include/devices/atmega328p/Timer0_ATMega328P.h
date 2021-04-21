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

private:
    sbyte oldPIND;
    bool clockSource_011();  //Prescaler 64
    bool clockSource_100();  //Prescaler 256
    bool clockSource_101();  //Prescaler 1024
    bool clockSource_110();  //External falling edge T0
    bool clockSource_111();  //External rising edge T0

    void normal();
};

#endif //PROJECTSOFIA_TIMER0_ATMEGA328P_H
