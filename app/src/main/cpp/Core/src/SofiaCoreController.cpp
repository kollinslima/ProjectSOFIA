//
// Created by kollins on 07/09/20.
//

#include <unistd.h>
#include "../include/SofiaCoreController.h"
#include "../include/devices/DeviceFactory.h"
#include "../include/devices/GenericDevice.h"

#define SOFIA_CORE_TAG "SOFIA CORE CONTROLLER"

SofiaCoreController::SofiaCoreController(Listener **listeners, JavaVM *vm, JNIEnv *env) {
    device = nullptr;
    this->notifier = new SofiaNotifier(listeners, vm, env);
}
SofiaCoreController::~SofiaCoreController() {
    if (device) {
        device->stop();
        delete device;
        device = nullptr;
    }
    delete this->notifier;
}

void SofiaCoreController::load(Device device, int fileDescriptor) {
    if (this->device) {
        delete this->device;
        this->device = nullptr;
    }
    this->device = DeviceFactory::createDevice(device, this->notifier);
    this->device->load(fileDescriptor);
}
void SofiaCoreController::start() {
    if (device) {
        device->start();
    }
}
void SofiaCoreController::stop() {
    if (device) {
        device->stop();
    }
}

//--------- NOTIFIER -----------
SofiaNotifier::SofiaNotifier(Listener **listeners, JavaVM *vm, JNIEnv *env) {
    this->listeners = listeners;
    notificationList.clear();
    stopDispatcher = false;
    dispatcherThread = thread(&SofiaNotifier::dispatcher, this, vm, env);
}

SofiaNotifier::~SofiaNotifier() {
    stopDispatcher = true;
    notificationCv.notify_all();
    dispatcherThread.join();
    notificationList.clear();
}

void SofiaNotifier::addNotification(int notificationID, const string& message) {
    lock_guard<mutex> notificationGuard(notificationMutex);
    notificationList.emplace_back(notificationID, message);
    notificationCv.notify_one();
}

void SofiaNotifier::dispatcher(JavaVM *vm, JNIEnv *env) {
    vm->AttachCurrentThread(&env, nullptr);
    pair<int, string> notification;
    while (true) {
        unique_lock<mutex> notificationGuard(notificationMutex);
        notificationCv.wait(notificationGuard,
                                  [this] { return !(notificationList.empty() && !stopDispatcher); });

        if (stopDispatcher) break;

        notification = notificationList.front();
        notificationList.pop_front();

        notificationGuard.unlock();
        listeners[notification.first](env, notification.second.c_str());
    }
    vm->DetachCurrentThread();
}

