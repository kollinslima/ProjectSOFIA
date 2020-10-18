//
// Created by kollins on 07/09/20.
//

#ifndef PROJECTSOFIA_SOFIACORECONTROLLER_H
#define PROJECTSOFIA_SOFIACORECONTROLLER_H

#include "../include/CommonCore.h"

#include <thread>
#include <mutex>
#include <condition_variable>
#include <list>
using namespace std;

class GenericDevice;

class SofiaCoreController {
    public:
        SofiaCoreController(Listener **listeners, JavaVM *vm, JNIEnv *env);
        ~SofiaCoreController();

//        void setListeners(Listener **listeners);

        void load(Device device, int fileDescriptor);
        void start();
        void stop();

//        bool isDeviceRunning();

        void addNotification(int notificationID, const string& message = "");

    private:

        GenericDevice *device;

        bool stopDispatcher;
        Listener **listeners;

        thread dispatcherThread;
        mutex notificationMutex;
        condition_variable notificationCv;
        list<pair<int, string>> notificationList;
        void dispatcher (JavaVM *vm, JNIEnv *env);
};


#endif //PROJECTSOFIA_SOFIACORECONTROLLER_H
