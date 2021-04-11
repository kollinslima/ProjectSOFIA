//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICAVRMEMORY_H
#define PROJECTSOFIA_GENERICAVRMEMORY_H

#include "../../generic/GenericMemory.h"

#define I_FLAG_MASK     0x80
#define T_FLAG_MASK     0x40
#define H_FLAG_MASK     0x20
#define S_FLAG_MASK     0x10
#define V_FLAG_MASK     0x08
#define N_FLAG_MASK     0x04
#define Z_FLAG_MASK     0x02
#define C_FLAG_MASK     0x01

class GenericAVRDataMemory : public GenericMemory {

public:
    virtual ~GenericAVRDataMemory() {}

    virtual bool write(smemaddr16 addr, void *data) = 0;

    virtual bool read(smemaddr16 addr, void *data) = 0;

    virtual smemaddr16 getSize() = 0;

    virtual smemaddr16 getSREGAddress() = 0;

    virtual smemaddr16 getSPLAddress() = 0;

    virtual smemaddr16 getSPHAddress() = 0;

    //TODO: Maybe this can be on parent class
    virtual bool checkInterruption(spc32 *interAddr) = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
