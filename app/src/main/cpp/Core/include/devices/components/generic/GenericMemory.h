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

        virtual bool write(smemaddr addr, void *data) = 0;
        virtual bool read(smemaddr addr, void *data) = 0;
        virtual smemaddr getSize() = 0;

    protected:
        smemaddr size;
        sbyte *buffer;
};


#endif //PROJECTSOFIA_GENERICMEMORY_H
