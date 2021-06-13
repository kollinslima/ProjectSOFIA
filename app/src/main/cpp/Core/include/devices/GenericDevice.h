//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICDEVICE_H
#define PROJECTSOFIA_GENERICDEVICE_H

#include <string>
#include <list>
#include "../CommonCore.h"
#include "meters/FreqDcMeter.h"

using namespace std;

class SofiaCoreController;

class GenericDevice : public FreqDcMeter {

public:
    GenericDevice();

    virtual ~GenericDevice();

    virtual void load(int fd) = 0;

    virtual void start() = 0;

    virtual void stop() = 0;

    virtual list<pair<int, string>> *getNotifications();

    virtual void measureFreqDc(smemaddr16 addr, sbyte before, sbyte after);
    virtual int getPinNumber(smemaddr16 addr, int position) = 0;

    virtual void signalInput(int pin, float voltage) = 0;

protected:
    void addNotification(int notificationID, const string &message = "");

    struct timespec simulatedTime;

private:
    list<pair<int, string>> notificationList;
};


#endif //PROJECTSOFIA_GENERICDEVICE_H
