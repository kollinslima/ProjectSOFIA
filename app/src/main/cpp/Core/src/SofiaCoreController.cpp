//
// Created by kollins on 07/09/20.
//

#include <unistd.h>
#include "../include/SofiaCoreController.h"
#include "../include/devices/DeviceFactory.h"
#include "../include/devices/GenericDevice.h"

#define SOFIA_CORE_TAG "SOFIA CORE CONTROLLER"

SofiaCoreController::SofiaCoreController(Device device, int fd) {
    listeners = nullptr;
    isRunning = false;
    notificationList.clear();

    this->device = DeviceFactory::createDevice(device, this);
    this->device->loadFile(fd);
}
SofiaCoreController::~SofiaCoreController() {
    stop();
    delete device;
}

void SofiaCoreController::setListeners(Listener **listeners) {
    this->listeners = listeners;
}

void SofiaCoreController::start(JavaVM *vm, JNIEnv *env) {
    isRunning = true;
    dispatcherThread = thread(&SofiaCoreController::dispatcher, this, vm, env);
    device->run();
}
void SofiaCoreController::stop() {
    isRunning = false;
    dispatcherThread.join();
    notificationList.clear();
}

void SofiaCoreController::addNotification(int notificationID, const string& message) {
    lock_guard<mutex> notificationGuard(notificationMutex);
    notificationList.emplace_back(notificationID, message);
    notificationCv.notify_one();
}


void SofiaCoreController::dispatcher(JavaVM *vm, JNIEnv *env) {
    vm->AttachCurrentThread(&env, nullptr);
    pair<int, string> notification;
    while (true) {
        unique_lock<mutex> notificationGuard(notificationMutex);
        notificationCv.wait(notificationGuard,
                                  [this] { return !(notificationList.empty() && isRunning); });

        if (!isRunning) break;

        notification = notificationList.front();
        notificationList.pop_front();

        notificationGuard.unlock();
        listeners[notification.first](env, notification.second.c_str());
    }
    vm->DetachCurrentThread();
}

bool SofiaCoreController::isDeviceRunning() {
    return isRunning;
}

