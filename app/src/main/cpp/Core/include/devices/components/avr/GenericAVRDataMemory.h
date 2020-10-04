//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICAVRMEMORY_H
#define PROJECTSOFIA_GENERICAVRMEMORY_H

#include "../generic/GenericMemory.h"

class GenericAVRDataMemory : public GenericMemory {

public:
    virtual ~GenericAVRDataMemory() {}

    virtual bool write(smemaddr addr, void *data) = 0;

    virtual bool read(smemaddr addr, void *data) = 0;

    virtual smemaddr getSize() = 0;

    virtual smemaddr getSREGAddres() = 0;

    virtual smemaddr getSPLAddres() = 0;

    virtual smemaddr getSPHAddres() = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
