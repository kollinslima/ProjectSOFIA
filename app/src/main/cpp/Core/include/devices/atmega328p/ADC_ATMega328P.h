//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_ADC_ATMEGA328P_H
#define PROJECTSOFIA_ADC_ATMEGA328P_H

#include "../components/avr/adc/GenericAVRADC.h"
#include "DataMemory_ATMega328P.h"

class ADC_ATMega328P : public GenericAVRADC {

public:
    ADC_ATMega328P(DataMemory_ATMega328P& dataMemory);
    virtual ~ADC_ATMega328P() {}

    void run();

private:
    DataMemory_ATMega328P& datMem;
};

#endif //PROJECTSOFIA_ADC_ATMEGA328P_H
