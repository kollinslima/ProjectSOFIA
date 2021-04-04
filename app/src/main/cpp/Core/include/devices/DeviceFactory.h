//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_DEVICEFACTORY_H
#define PROJECTSOFIA_DEVICEFACTORY_H

#include "../../include/devices/GenericDevice.h"
#include "../CommonCore.h"

class SofiaUiNotifier;

class DeviceFactory {
    public:
        static GenericDevice *createDevice(Device device, SofiaUiNotifier *notifier);
};


#endif //PROJECTSOFIA_DEVICEFACTORY_H
