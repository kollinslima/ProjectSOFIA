//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICAVRMEMORY_H
#define PROJECTSOFIA_GENERICAVRMEMORY_H

#include "../generic/GenericMemory.h"

class GenericAVRDataMemory : public GenericMemory {

    public:
        virtual ~GenericAVRDataMemory() {}

        virtual bool write(unsigned long addr, void *data) = 0;
        virtual bool read(unsigned long addr, void *data) = 0;
        virtual unsigned long getSize() = 0;

        virtual unsigned long getSREGAddres() = 0;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
