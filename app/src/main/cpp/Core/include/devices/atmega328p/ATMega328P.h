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
#include "../components/avr/AVRCPU.h"

#define NUM_MODULES 1

using namespace std;

class SofiaCoreController;

class ATMega328P : public GenericDevice {

public:
    ATMega328P(SofiaCoreController *scc);

    ~ATMega328P();

    bool loadFile(int fd) override;

    void run() override;

private:
    SofiaCoreController *scc;

    unsigned int clockFreq;

    ProgramMemory_ATMega328P programMemory;
    DataMemory_ATMega328P dataMemory;
    AVRCPU cpu = AVRCPU(&programMemory, &dataMemory);

    int syncCounter[NUM_MODULES];
    thread scheduler[NUM_MODULES];
    thread syncThread;

    void cpuThread();
//    void stubThread(int index);
    void syncronizationThread();
};


#endif //PROJECTSOFIA_ATMEGA328P_H
