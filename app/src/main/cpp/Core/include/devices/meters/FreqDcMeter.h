//
// Created by kollins on 13/06/2021.
//

#ifndef PROJECTSOFIA_FREQDCMETER_H
#define PROJECTSOFIA_FREQDCMETER_H

#include "../../CommonCore.h"
#include "GenericMeter.h"

class FreqDcMeter : public GenericMeter {
public:
    virtual ~FreqDcMeter() {};
    virtual void measureFreqDc(smemaddr16 addr, sbyte before, sbyte after) = 0;

protected:
    typedef struct {
        struct timespec dcPeriod;
        struct timespec freqPeriod;
    } IoPeriods;

    IoPeriods *periodMeasure;
    struct timespec freqRefTime, dcRefTime;
};

#endif //PROJECTSOFIA_FREQDCMETER_H
