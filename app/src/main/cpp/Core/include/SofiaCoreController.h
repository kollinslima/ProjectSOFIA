//
// Created by kollins on 07/09/20.
//

#ifndef PROJECTSOFIA_SOFIACORECONTROLLER_H
#define PROJECTSOFIA_SOFIACORECONTROLLER_H

#include "../include/CommonCore.h"

#include <thread>
#include <semaphore.h>
#include <list>
using namespace std;

class GenericDevice;

class SofiaUiNotifier {
    public:
        explicit SofiaUiNotifier(Listener **listeners, JavaVM *vm, JNIEnv *env);
        ~SofiaUiNotifier();

        void setNotificationPeriod(int usPeriod);
        void setDevice(GenericDevice *device);
        void addNotification(int notificationID, const string& message = "");
    private :
        Listener **listeners;
        bool runDispatcher;

        int usNotPeriod;
        GenericDevice *device;
        thread dispatcherThread;
        sem_t waitDeviceSem;
        void dispatcher (JavaVM *vm, JNIEnv *env);
};

class SofiaCoreController {
    public:
        explicit SofiaCoreController(Listener **listeners, JavaVM *vm, JNIEnv *env);
        ~SofiaCoreController();

        void load(Device device, int fileDescriptor);
        void start();
        void stop();

        void setNotificationPeriod(int usPeriod);

        void signalInput(int pin, float voltage);

    private:
        GenericDevice *device;
        SofiaUiNotifier *notifier;
};

#endif //PROJECTSOFIA_SOFIACORECONTROLLER_H
