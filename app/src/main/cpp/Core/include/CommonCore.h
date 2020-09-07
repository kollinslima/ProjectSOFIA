//
// Created by kollins on 07/09/20.
//

#ifndef PROJECTSOFIA_COMMONCORE_H
#define PROJECTSOFIA_COMMONCORE_H

#include <jni.h>
#include <android/log.h>

typedef void Listener(JNIEnv *env, const char *msg);

#define MAX_LISTENERS 1

#define TIME_UPDATE_LISTENER 0

#define LOGI(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__))

#endif //PROJECTSOFIA_COMMONCORE_H
