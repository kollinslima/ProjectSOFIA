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

    bool write(smemaddr addr, void *data) override;

    bool read(smemaddr addr, void *data) override;

    smemaddr getSize() override;

    smemaddr getSREGAddres() override;

    smemaddr getSPLAddres() override;

    smemaddr getSPHAddres() override;
};


#endif //PROJECTSOFIA_DATAMEMORY_ATMEGA328P_H
