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

class SofiaNotifier {
    public:
        explicit SofiaNotifier(Listener **listeners, JavaVM *vm, JNIEnv *env);
        ~SofiaNotifier();

        void addNotification(int notificationID, const string& message = "");
    private :
        Listener **listeners;
        bool stopDispatcher;

        thread dispatcherThread;
        mutex notificationMutex;
        condition_variable notificationCv;
        list<pair<int, string>> notificationList;
        void dispatcher (JavaVM *vm, JNIEnv *env);
};

class SofiaCoreController {
    public:
        explicit SofiaCoreController(Listener **listeners, JavaVM *vm, JNIEnv *env);
        ~SofiaCoreController();

        void load(Device device, int fileDescriptor);
        void start();
        void stop();

    private:
        GenericDevice *device;
        SofiaNotifier *notifier;
};

#endif //PROJECTSOFIA_SOFIACORECONTROLLER_H
