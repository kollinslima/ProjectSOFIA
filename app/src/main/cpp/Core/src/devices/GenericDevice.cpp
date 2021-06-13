//
// Created by kollins on 12/09/20.
//

#include "../../include/devices/GenericDevice.h"

#define SOFIA_GENERIC_DEVICE_TAG "SOFIA GENERIC DEVICE"

GenericDevice::GenericDevice() {
    simulatedTime.tv_sec = 0;
    simulatedTime.tv_nsec = 0;

    freqRefTime.tv_sec = 0;
    freqRefTime.tv_nsec = 0;

    dcRefTime.tv_sec = 0;
    dcRefTime.tv_nsec = 0;
}

GenericDevice::~GenericDevice() {
}

list<pair<int, string>> *GenericDevice::getNotifications() {
    notificationList.emplace_back(TIME_UPDATE_LISTENER, to_string(simulatedTime.tv_sec));
    return &notificationList;
}

void GenericDevice::addNotification(int notificationID, const string &message) {
    notificationList.emplace_back(notificationID, message);
}

void GenericDevice::measureFreqDc(smemaddr16 addr, sbyte before, sbyte after) {
    sbyte mask = 0x01;
    sbyte changePos = before ^after;
    int idx;
    for (int i = 0; i < 8; ++i, mask = mask << 1) {
        if (changePos & mask) {
            idx = getPinNumber(addr, i);
            if (after & mask) {
                //RISE
                dcRefTime = simulatedTime;
            } else {
                //FALL
                //Update Frequency period
                periodMeasure[idx].freqPeriod.tv_sec =
                        simulatedTime.tv_sec - freqRefTime.tv_sec;
                periodMeasure[idx].freqPeriod.tv_nsec =
                        simulatedTime.tv_nsec - freqRefTime.tv_nsec;

                //Update DC period
                periodMeasure[idx].dcPeriod.tv_sec =
                        simulatedTime.tv_sec - dcRefTime.tv_sec;
                periodMeasure[idx].dcPeriod.tv_nsec =
                        simulatedTime.tv_nsec - dcRefTime.tv_nsec;

                freqRefTime = simulatedTime;
            }
        }
    }
}
