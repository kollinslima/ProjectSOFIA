//
// Created by kollins on 12/09/20.
//

#ifndef PROJECTSOFIA_GENERICDEVICE_H
#define PROJECTSOFIA_GENERICDEVICE_H

#include <string>
#include <list>
#include "../CommonCore.h"

using namespace std;

class SofiaCoreController;

class GenericDevice {

    public:
        GenericDevice();
        virtual ~GenericDevice();

        virtual void load(int fd)=0;
        virtual void start()=0;
        virtual void stop()=0;
        virtual list<pair<int, string>> *getNotifications();

        virtual void signalInput(int pin, float voltage)=0;

    protected:
        void addNotification(int notificationID, const string& message = "");
        unsigned long simulatedTime;

    private:
        list<pair<int, string>> notificationList;
};


#endif //PROJECTSOFIA_GENERICDEVICE_H
