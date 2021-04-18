//
// Created by kollins on 19/09/20.
//

#ifndef PROJECTSOFIA_GENERICCPU_H
#define PROJECTSOFIA_GENERICCPU_H

#include "../../../CommonCore.h"
#include "UCModule.h"

class GenericCPU : public UCModule {

    public:
        virtual ~GenericCPU() {};

    protected:
        spc32 pc;
};


#endif //PROJECTSOFIA_GENERICCPU_H
