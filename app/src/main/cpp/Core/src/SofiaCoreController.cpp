//
// Created by kollins on 07/09/20.
//

#include <unistd.h>
#include "../include/SofiaCoreController.h"

static const char *SOFIA_CORE_TAG = "SOFIA CORE";

SofiaCoreController::SofiaCoreController(Listener **listeners) {
    this->schedulerThread = nullptr;
    this->listeners = listeners;
}
SofiaCoreController::~SofiaCoreController() {
    if (this->schedulerThread) {
        this->schedulerThread->join();
        delete this->schedulerThread;
        this->schedulerThread = nullptr;
    }
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