//
// Created by kollins on 12/09/20.
//

#include "../../include/devices/GenericDevice.h"

#define SOFIA_GENERIC_DEVICE_TAG "SOFIA GENERIC DEVICE"

GenericDevice::GenericDevice() {
    simulatedTime = 0;
}

GenericDevice::~GenericDevice() {
}

list<pair<int, string>> *GenericDevice::getNotifications() {
    notificationList.emplace_back(TIME_UPDATE_LISTENER, to_string(simulatedTime));
    return &notificationList;
}

void GenericDevice::addNotification(int notificationID, const string &message) {
    notificationList.emplace_back(notificationID, message);
}
