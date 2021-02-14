//
// Created by kollins on 07/09/20.
//

#ifndef PROJECTSOFIA_COMMONCORE_H
#define PROJECTSOFIA_COMMONCORE_H

#include <jni.h>
#include <android/log.h>

typedef void Listener(JNIEnv *env, const char *msg);

typedef __uint32_t spc32;
typedef __uint16_t smemaddr16;
typedef __uint8_t sbyte;
typedef __uint16_t sword16;

#define MAX_LISTENERS 6

#define TIME_UPDATE_LISTENER 0

#define LOAD_SUCCESS_LISTENER  1
#define CHECKSUM_ERROR_LISTENER  2
#define FILE_OPEN_FAIL_LISTENER  3
#define INVALID_FILE_LISTENER  4

#define IO_CHANGED_LISTENER  5

#ifdef NDEBUG
#define LOGD(TAG, ...)
#define LOGI(TAG, ...)
#define LOGW(TAG, ...)
#define LOGE(TAG, ...)
#else
#define LOGD(TAG, ...)   \
//    ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))
#define LOGI(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__))
#define LOGW(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__))
#define LOGE(TAG, ...)   \
    ((void)__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))
#endif

enum Device {
        ATMEGA328P,
    };

#endif //PROJECTSOFIA_COMMONCORE_H
