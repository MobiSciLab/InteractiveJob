//
// Created by Cao Minh Vu on 9/4/15.
//
#include <android/log.h>

#ifndef INTERACTIVEJOB_WRAPPER_LOG_H
#define INTERACTIVEJOB_WRAPPER_LOG_H
#ifdef __cplusplus
extern "C" {
#endif

#define  LOG_TAG    "INTERACTIVE_JOB"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)

#ifdef __cplusplus
}
#endif
#endif //INTERACTIVEJOB_WRAPPER_LOG_H