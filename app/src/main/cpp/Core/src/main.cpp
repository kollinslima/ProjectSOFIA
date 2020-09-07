#include <cstring>

#include "../include/CommonCore.h"
#include "../include/SofiaCoreController.h"

static const char *MAIN_CORE_TAG = "CORE MAIN";

typedef struct {
    JavaVM *vm;
    JNIEnv *env;
    jclass activityClz;
    jobject activityObj;
    SofiaCoreController *scc;
    Listener *listeners[MAX_LISTENERS];
} MainCtx;

MainCtx mainCtx;

void notifyTimeUpdate(JNIEnv *env, const char *msg);

////////////////////// UI -> NDK Interface//////////////////////////////////////////
extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {

    LOGI(MAIN_CORE_TAG, "Loading NDK...");

    memset(&mainCtx, 0, sizeof(mainCtx));

    if (vm->GetEnv(reinterpret_cast<void**>(&mainCtx.env), JNI_VERSION_1_6) != JNI_OK){
        return JNI_ERR;
    }

    mainCtx.vm = vm;
    mainCtx.scc = nullptr;
    mainCtx.activityClz = nullptr;
    mainCtx.activityObj = nullptr;

    mainCtx.listeners[TIME_UPDATE_LISTENER] = notifyTimeUpdate;

    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL
Java_com_kollins_project_sofia_MainActivity_startCore(
        JNIEnv* env,
        jobject instance) {

    LOGI(MAIN_CORE_TAG, "Starting SOFIA Core");

    if (!mainCtx.activityObj) {
        jclass clz = env->GetObjectClass(instance);
        mainCtx.activityClz = reinterpret_cast<jclass>(env->NewGlobalRef(clz));
        mainCtx.activityObj = env->NewGlobalRef(instance);
    }

    if (!mainCtx.scc) {
        mainCtx.scc = new SofiaCoreController(mainCtx.listeners);
        mainCtx.scc->start(mainCtx.vm, mainCtx.env);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_kollins_project_sofia_MainActivity_stopCore(
        JNIEnv* env,
        jobject instance) {

    LOGI(MAIN_CORE_TAG, "Stopping SOFIA Core");

    if (mainCtx.scc) {
        mainCtx.scc->stop();
        delete mainCtx.scc;
        mainCtx.scc = nullptr;
    }

    if (mainCtx.activityObj) {
        env->DeleteGlobalRef(mainCtx.activityClz);
        env->DeleteGlobalRef(mainCtx.activityObj);
        mainCtx.activityObj = nullptr;
    }
}

////////////////////// NDK -> UI Interface//////////////////////////////////////////
void notifyTimeUpdate(JNIEnv *env, const char *msg) {
    jmethodID id  = env->GetMethodID(mainCtx.activityClz, "timeUpdate", "()V");
    env->CallVoidMethod(mainCtx.activityObj, id);
}