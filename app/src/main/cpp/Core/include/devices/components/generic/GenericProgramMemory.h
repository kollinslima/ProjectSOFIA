//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICPROGRAMMEMORY_H
#define PROJECTSOFIA_GENERICPROGRAMMEMORY_H

#include "GenericMemory.h"

class GenericProgramMemory : public GenericMemory {

    public:
        virtual ~GenericProgramMemory() {}

        virtual bool loadInstruction(spc pc, void *data) = 0;
        virtual bool write(smemaddr addr, void *data) = 0;
        virtual bool read(smemaddr addr, void *data) = 0;
        virtual smemaddr getSize() = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
