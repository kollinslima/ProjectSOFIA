//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_GENERICADC_H
#define PROJECTSOFIA_GENERICADC_H

#include "../../../CommonCore.h"
#include "UCModule.h"

class GenericADC : public UCModule {

public:
    virtual ~GenericADC() {};

    virtual void setAnalogInput(int pin, float voltage) = 0;

protected:
    int adcResolution;
    int voltageReference;
};

#endif //PROJECTSOFIA_GENERICADC_H
