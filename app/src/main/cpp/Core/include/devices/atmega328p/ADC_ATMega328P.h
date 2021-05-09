//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_ADC_ATMEGA328P_H
#define PROJECTSOFIA_ADC_ATMEGA328P_H

#include "../components/avr/adc/GenericAVRADC.h"
#include "DataMemory_ATMega328P.h"

#define N_ADC_INPUTS 16

class ADC_ATMega328P : public GenericAVRADC {

public:
    ADC_ATMega328P(DataMemory_ATMega328P& dataMemory);
    virtual ~ADC_ATMega328P() {}

    void run();
    void setAnalogInput(int pin, float voltage);

private:
    DataMemory_ATMega328P& datMem;
    sword16 inputMux[N_ADC_INPUTS];
    sword16 converted;

    void convert();
};

#endif //PROJECTSOFIA_ADC_ATMEGA328P_H
