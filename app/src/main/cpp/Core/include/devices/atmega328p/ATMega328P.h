//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_ATMEGA328P_H
#define PROJECTSOFIA_ATMEGA328P_H

#include <thread>
#include <mutex>
#include <condition_variable>
#include "../GenericDevice.h"
#include "ProgramMemory_ATMega328P.h"
#include "DataMemory_ATMega328P.h"
#include "../components/avr/cpu/AVRCPU.h"

#define NUM_MODULES 1

using namespace std;

class SofiaUiNotifier;

class ATMega328P : public GenericDevice {

public:
    ATMega328P(SofiaUiNotifier *notifier);

    ~ATMega328P();

    void load(int fd) override;
    void start() override;
    void stop() override;

    void signalInput(int pin, float voltage) override;

private:
    SofiaUiNotifier *notifier;

    bool isRunning;
    unsigned int clockFreq;

    float vcc;
    float minInputHight;
    float maxInputLow;

    ProgramMemory_ATMega328P *programMemory;
    DataMemory_ATMega328P *dataMemory;
    AVRCPU *cpu;

    unsigned int syncCounter[NUM_MODULES];
    thread scheduler[NUM_MODULES];
    thread syncThread;

    bool isDigitalInput(int pin);
    bool isAnalogInput(int pin);
    bool getLogicState(int pin, float voltage);

    void cpuThread();
//    void stubThread(int index);
    void syncronizationThread();
};


#endif //PROJECTSOFIA_ATMEGA328P_H
