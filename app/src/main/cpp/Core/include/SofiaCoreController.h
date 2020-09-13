//
// Created by kollins on 07/09/20.
//

#ifndef PROJECTSOFIA_SOFIACORECONTROLLER_H
#define PROJECTSOFIA_SOFIACORECONTROLLER_H

#include "../include/CommonCore.h"
#include "devices/GenericDevice.h"

#include <thread>
using namespace std;

class SofiaCoreController {
    public:
        SofiaCoreController(Device device, int filePath);
        ~SofiaCoreController();

        void setListeners(Listener **listeners);

        void start(JavaVM *vm, JNIEnv *env);
        void stop();

    private:

        GenericDevice *device;

        void scheduler (JavaVM *vm, JNIEnv *env);

        thread *schedulerThread;
        Listener **listeners;
};


#endif //PROJECTSOFIA_SOFIACORECONTROLLER_H
