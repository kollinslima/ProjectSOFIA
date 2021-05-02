//
// Created by kollins on 18/04/21.
//

#ifndef PROJECTSOFIA_GENERICTIMER_H
#define PROJECTSOFIA_GENERICTIMER_H

#include "../../../CommonCore.h"
#include "UCModule.h"

class GenericTimer : public UCModule {

public:
    virtual ~GenericTimer() {};

protected:
    sword16 progress;
};

#endif //PROJECTSOFIA_GENERICTIMER_H
