//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICPROGRAMMEMORY_H
#define PROJECTSOFIA_GENERICPROGRAMMEMORY_H

#include "GenericMemory.h"

class GenericProgramMemory : public GenericMemory {

    public:
        virtual ~GenericProgramMemory() {}

        virtual bool loadInstruction(unsigned long pc, void *data) = 0;
        virtual bool write(unsigned long addr, void *data) = 0;
        virtual bool read(unsigned long addr, void *data) = 0;
        virtual unsigned long getSize() = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
