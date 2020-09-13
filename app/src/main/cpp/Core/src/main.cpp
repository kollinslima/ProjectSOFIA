#include "../include/CommonCore.h"
#include "../include/SofiaCoreController.h"

static const char *MAIN_CORE_TAG = "SOFIA CORE MAIN";

typedef struct {
    JavaVM *vm;
    JNIEnv *env;
    jclass activityClz;
    jobject activityObj;
    SofiaCoreController *scc;
    Listener *listeners[MAX_LISTENERS];
} MainCtx;

MainCtx mainCtx;

////////////////////// Utils //////////////////////////////////////////
std::string jstringToString(JNIEnv *env, jstring string) {
    std::string ret;
    if (string) {
        const jclass stringClass = env->GetObjectClass(string);
        const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
        const jbyteArray stringJBytes = reinterpret_cast<jbyteArray>(env->CallObjectMethod(string, getBytes, env->NewStringUTF("UTF-8")));

        size_t length = static_cast<size_t>(env->GetArrayLength(stringJBytes));
        jbyte *pBytes = env->GetByteArrayElements(stringJBytes, nullptr);

        ret = std::string(reinterpret_cast<char*>(pBytes), length);

        env->ReleaseByteArrayElements(stringJBytes, pBytes, JNI_ABORT);

        env->DeleteLocalRef(stringJBytes);
        env->DeleteLocalRef(stringClass);
    } else {
        ret = std::string("");
    }
     return ret;
}

Device enumMap(JNIEnv *env, jobject uiEnum) {
    Device ret = Device::ARDUINO_UNO;
    if (uiEnum) {
        const jclass enumClass = env->FindClass("com/kollins/project/sofia/Device");
        const jmethodID name = env->GetMethodID(enumClass, "name", "()Ljava/lang/String;");
        const jstring value = reinterpret_cast<jstring>(env->CallObjectMethod(uiEnum, name));

        const char *nativeEnum = env->GetStringUTFChars(value, nullptr);

        if (!strcmp(nativeEnum, "ARDUINO_UNO")) {
            ret = Device ::ARDUINO_UNO;
        } else {
            LOGW(MAIN_CORE_TAG, "Could not parse UI enum, assign ARDUINO_UNO");
            ret = Device ::ARDUINO_UNO;
        }

        env->ReleaseStringUTFChars(value, nativeEnum);

        env->DeleteLocalRef(value);
        env->DeleteLocalRef(enumClass);
    } else {
        LOGW(MAIN_CORE_TAG, "UI enum is NULL, using ARDUINO_UNO");
    }
    return ret;
}
////////////////////// NDK -> UI Interface//////////////////////////////////////////
void notifyTimeUpdate(JNIEnv *env, const char *msg) {
    jmethodID id  = env->GetMethodID(mainCtx.activityClz, "timeUpdate", "()V");
    env->CallVoidMethod(mainCtx.activityObj, id);
}

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
    if (mainCtx.scc) {
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

extern "C" JNIEXPORT void JNICALL
Java_com_kollins_project_sofia_MainActivity_loadCore(JNIEnv *env, jobject instance,
                                                     jobject device, jint fd) {

    Java_com_kollins_project_sofia_MainActivity_stopCore(env, instance);

    Device nativeDevice = enumMap(env, device);
    mainCtx.scc = new SofiaCoreController(nativeDevice, reinterpret_cast<int>(fd));
    mainCtx.scc->setListeners(mainCtx.listeners);
}