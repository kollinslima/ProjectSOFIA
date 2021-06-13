//
// Created by kollins on 07/09/20.
//

#include <unistd.h>
#include "../include/SofiaCoreController.h"
#include "../include/devices/DeviceFactory.h"
#include "../include/devices/GenericDevice.h"

#define SOFIA_CORE_TAG "SOFIA CORE CONTROLLER"
#define SOFIA_UI_NOTIFICATION_TAG "SOFIA UI NOTIFICATION"

SofiaCoreController::SofiaCoreController(Listener **listeners, JavaVM *vm, JNIEnv *env) {
    device = nullptr;
    this->notifier = new SofiaUiNotifier(listeners, vm, env);
}
SofiaCoreController::~SofiaCoreController() {
    delete this->notifier;

    if (device != nullptr) {
        device->stop();
        delete device;
        device = nullptr;
    }
}

void SofiaCoreController::load(Device device, int fileDescriptor) {
    if (this->device != nullptr) {
        delete this->device;
        this->device = nullptr;
    }
    this->device = DeviceFactory::createDevice(device);
    this->device->load(fileDescriptor);
    notifier->setDevice(this->device);
}
void SofiaCoreController::start() {
    if (device != nullptr) {
        device->start();
    }
}
void SofiaCoreController::stop() {
    if (device != nullptr) {
        device->stop();
    }
}

void SofiaCoreController::signalInput(int pin, float voltage) {
    if (device != nullptr) {
        LOGI(SOFIA_CORE_TAG, "Signal received: %d:%f", pin, voltage);
        device->signalInput(pin, voltage);
    }
}

void SofiaCoreController::setNotificationPeriod(int usPeriod) {
    this->notifier->setNotificationPeriod(usPeriod);
}

//--------- NOTIFIER -----------
SofiaUiNotifier::SofiaUiNotifier(Listener **listeners, JavaVM *vm, JNIEnv *env) {
    this->listeners = listeners;
    runDispatcher = true;
    usNotPeriod = 16666; //Default 60Hz (TODO: get screen information)
    device = nullptr;
    sem_init(&waitDeviceSem, 0, 0);
    dispatcherThread = thread(&SofiaUiNotifier::dispatcher, this, vm, env);
}

SofiaUiNotifier::~SofiaUiNotifier() {
    runDispatcher = false;
    dispatcherThread.join();
    sem_destroy(&waitDeviceSem);
}

void SofiaUiNotifier::setNotificationPeriod(int usPeriod) {
    this->usNotPeriod = usPeriod;
}

void SofiaUiNotifier::setDevice(GenericDevice *device) {
    this->device = device;
    if (this->device != nullptr) {
        sem_post(&waitDeviceSem);
    }
}

void SofiaUiNotifier::dispatcher(JavaVM *vm, JNIEnv *env) {
    vm->AttachCurrentThread(&env, nullptr);

    list<pair<int, string>> *notificationList;
    pair<int, string> notification;

    sem_wait(&waitDeviceSem);

    while (runDispatcher) {
        usleep(usNotPeriod);
        notificationList = device->getNotifications();
        notificationList->emplace_back(SCREEN_UPDATE_LISTENER, "");
        while (!notificationList->empty()) {
            notification = notificationList->front();
            listeners[notification.first](env, notification.second.c_str());
            notificationList->pop_front();
        }
    }
    vm->DetachCurrentThread();
}

