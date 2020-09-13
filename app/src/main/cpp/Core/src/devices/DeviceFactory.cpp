//
// Created by kollins on 12/09/20.
//

#include "../../include/devices/DeviceFactory.h"
#include "../../include/devices/atmega328p/ATMega328P.h"

static const char *SOFIA_DEVICE_FACTORY_TAG = "SOFIA DEVICE FACTORY";

GenericDevice * DeviceFactory::createDevice(Device device) {
    GenericDevice *ret = nullptr;
    switch (device) {
        case Device::ARDUINO_UNO:
            ret = new ATMega328P();
            break;
        default:
            LOGE(SOFIA_DEVICE_FACTORY_TAG, "Device unknown (how did I get here?)");
            break;
    }
    return ret;
}