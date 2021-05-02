//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_TIMER1_ATMEGA328P_H
#define PROJECTSOFIA_TIMER1_ATMEGA328P_H

#include "Timer_ATMega328P.h"

class Timer1_ATMega328P : public Timer_ATMega328P {

public:
    Timer1_ATMega328P(DataMemory_ATMega328P& dataMemory);
    virtual ~Timer1_ATMega328P() {}
    void run();

private:
    sbyte oldPIND;
    sword16 writeHigh, readHigh;
    bool clockSource_011();  //Prescaler 64
    bool clockSource_100();  //Prescaler 256
    bool clockSource_101();  //Prescaler 1024
    bool clockSource_110();  //External falling edge T1
    bool clockSource_111();  //External rising edge T1

    void normal();
    void pwmPhaseCorrect1();
    void pwmPhaseCorrect2();
};

#endif //PROJECTSOFIA_TIMER1_ATMEGA328P_H
