//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICDEVICE_H
#define PROJECTSOFIA_GENERICDEVICE_H

#include <string>
#include "../CommonCore.h"

using namespace std;

class SofiaCoreController;

class GenericDevice {

    public:
        virtual ~GenericDevice() {};

        virtual void load(int fd)=0;
        virtual void start()=0;
        virtual void stop()=0;
};


#endif //PROJECTSOFIA_GENERICDEVICE_H
