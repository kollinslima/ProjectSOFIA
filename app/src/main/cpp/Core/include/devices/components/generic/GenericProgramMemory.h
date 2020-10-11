//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICPROGRAMMEMORY_H
#define PROJECTSOFIA_GENERICPROGRAMMEMORY_H

#include "GenericMemory.h"

class GenericProgramMemory : public GenericMemory {

    public:
        virtual ~GenericProgramMemory() {}

        virtual bool loadInstruction(spc32 pc, void *data) = 0;
        virtual bool write(smemaddr16 addr, void *data) = 0;
        virtual bool read(smemaddr16 addr, void *data) = 0;
        virtual smemaddr16 getSize() = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
