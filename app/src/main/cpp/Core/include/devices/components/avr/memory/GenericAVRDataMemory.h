//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICAVRMEMORY_H
#define PROJECTSOFIA_GENERICAVRMEMORY_H

#include "../../generic/GenericMemory.h"

class GenericAVRDataMemory : public GenericMemory {

public:
    virtual ~GenericAVRDataMemory() {}

    virtual bool write(smemaddr16 addr, void *data) = 0;

    virtual bool read(smemaddr16 addr, void *data) = 0;

    virtual smemaddr16 getSize() = 0;

    virtual smemaddr16 getSREGAddres() = 0;

    virtual smemaddr16 getSPLAddres() = 0;

    virtual smemaddr16 getSPHAddres() = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
