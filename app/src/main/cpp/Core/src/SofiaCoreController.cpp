//
// Created by kollins on 07/09/20.
//

#include <unistd.h>
#include "../include/SofiaCoreController.h"
#include "../include/devices/DeviceFactory.h"

static const char *SOFIA_CORE_TAG = "SOFIA CORE CONTROLLER";

SofiaCoreController::SofiaCoreController(Device device, int fd) {
    this->listeners = nullptr;
    this->schedulerThread = nullptr;

    this->device = DeviceFactory::createDevice(device);
    this->device->loadFile(fd);
}
SofiaCoreController::~SofiaCoreController() {
    if (this->schedulerThread) {
        this->schedulerThread->join();
        delete this->schedulerThread;
        this->schedulerThread = nullptr;
    }
    delete this->device;
}

void SofiaCoreController::setListeners(Listener **listeners) {
    this->listeners = listeners;
}

void SofiaCoreController::start(JavaVM *vm, JNIEnv *env) {
    this->schedulerThread = new thread(&SofiaCoreController::scheduler, this, vm, env);
}
void SofiaCoreController::stop() {

}

void SofiaCoreController::scheduler(JavaVM *vm, JNIEnv *env) {
    int seconds = 0;
    vm->AttachCurrentThread(&env, nullptr);
    while (true) {
        for (int i = 0; i < 16000000; ++i);
        this->listeners[TIME_UPDATE_LISTENER](env, nullptr);
    }
    vm->DetachCurrentThread();
}
