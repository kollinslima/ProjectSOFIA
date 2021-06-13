//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_ATMEGA328P_H
#define PROJECTSOFIA_ATMEGA328P_H

#include <thread>
#include "../GenericDevice.h"
#include "ProgramMemory_ATMega328P.h"
#include "DataMemory_ATMega328P.h"
#include "ADC_ATMega328P.h"
#include "Timer_ATMega328P.h"
#include "../components/avr/cpu/AVRCPU.h"

#define NUM_MODULES 5

using namespace std;

class SofiaUiNotifier;

class ATMega328P : public GenericDevice {

public:
    ATMega328P();
    ~ATMega328P();

    void load(int fd) override;
    void start() override;
    void stop() override;

    int getPinNumber(smemaddr16 addr, int position) override;

    void signalInput(int pin, float voltage) override;

    list<pair<int, string>> *getNotifications() override;

private:
    bool isRunning;
    unsigned int clockFreqHz;
    unsigned int clockPerNs;

    float vcc;
    float minInputHight;
    float maxInputLow;

    ProgramMemory_ATMega328P programMemory;
    DataMemory_ATMega328P dataMemory;
    ADC_ATMega328P adc;

    UCModule *modules[NUM_MODULES];
    unsigned int syncCounter[NUM_MODULES];
    thread scheduler[NUM_MODULES];
    thread syncThread;

    bool isDigitalInput(int pin);
    bool isAnalogInput(int pin);
    bool getLogicState(int pin, float voltage);

    void moduleThread(int index);
    void syncronizationThread();
};


#endif //PROJECTSOFIA_ATMEGA328P_H
