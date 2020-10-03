//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICMEMORY_H
#define PROJECTSOFIA_GENERICMEMORY_H

#include "../../../CommonCore.h"

class GenericMemory {

    public:
        virtual ~GenericMemory() {}

        virtual bool write(unsigned long addr, void *data) = 0;
        virtual bool read(unsigned long addr, void *data) = 0;
        virtual unsigned long getSize() = 0;

    protected:
        unsigned long size;
        sbyte *buffer;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
