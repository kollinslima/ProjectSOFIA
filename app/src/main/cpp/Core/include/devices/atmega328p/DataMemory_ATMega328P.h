//
// Created by kollins on 27/09/20.
//

#ifndef PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
#define PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H


#include "../components/avr/GenericAVRDataMemory.h"

class DataMemory_ATMega328P : public GenericAVRDataMemory {

public:
    DataMemory_ATMega328P();

    ~DataMemory_ATMega328P();

    bool write(smemaddr16 addr, void *data) override;

    bool read(smemaddr16 addr, void *data) override;

    smemaddr16 getSize() override;

    smemaddr16 getSREGAddres() override;

    smemaddr16 getSPLAddres() override;

    smemaddr16 getSPHAddres() override;

private:
    void setupDataMemory();
};


#endif //PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
