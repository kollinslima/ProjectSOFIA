//
// Created by kollins on 27/09/20.
//

#ifndef PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
#define PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H


#include "../components/avr/memory/GenericAVRDataMemory.h"
#include "../../../include/SofiaCoreController.h"
#include <string>

using namespace std;

class DataMemory_ATMega328P : public GenericAVRDataMemory {

public:
    DataMemory_ATMega328P(SofiaNotifier *notifier);

    ~DataMemory_ATMega328P();

    bool write(smemaddr16 addr, void *data) override;

    bool read(smemaddr16 addr, void *data) override;

    smemaddr16 getSize() override;

    smemaddr16 getSREGAddres() override;

    smemaddr16 getSPLAddres() override;

    smemaddr16 getSPHAddres() override;

private:
    void setupDataMemory();
    SofiaNotifier *notifier;
};


#endif //PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
