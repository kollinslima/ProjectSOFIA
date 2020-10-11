//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICMEMORY_H
#define PROJECTSOFIA_GENERICMEMORY_H

#include "../../../CommonCore.h"

#define SAFE_ADDR(ADDR, LIMIT) (ADDR&LIMIT)

class GenericMemory {

    public:
        virtual ~GenericMemory() {}

        virtual bool write(smemaddr16 addr, void *data) = 0;
        virtual bool read(smemaddr16 addr, void *data) = 0;
        virtual smemaddr16 getSize() = 0;

    protected:
        smemaddr16 size;
        sbyte *buffer;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
