//
// Created by kollins on 07/09/20.
//

#ifndef PROJECTSOFIA_COMMONCORE_H
#define PROJECTSOFIA_COMMONCORE_H

#include <jni.h>
#include <android/log.h>

typedef void Listener(JNIEnv *env, const char *msg);

typedef __uint32_t spc;
typedef __uint16_t smemaddr;
typedef __uint8_t sbyte;
typedef __uint16_t sword16;

#define MAX_LISTENERS 1
#define TIME_UPDATE_LISTENER 0

#ifdef NDEBUG
#define LOGD(TAG, ...)
#define LOGI(TAG, ...)
#define LOGW(TAG, ...)
#define LOGE(TAG, ...)
#else
#define LOGD(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))
#define LOGI(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__))
#define LOGW(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__))
#define LOGE(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))
#endif

enum Device {
        ARDUINO_UNO,
    };

#endif //PROJECTSOFIA_COMMONCORE_H
