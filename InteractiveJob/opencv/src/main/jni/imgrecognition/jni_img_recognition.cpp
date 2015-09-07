/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include <jni.h>
#include "img_recognition.h"
#include "msl_com_interactivejob_JNIHelper.h"
#include "wrapper_log.h"

std::string jstring2string(JNIEnv* env, jstring jstr)
{
    if ( !jstr) return "";

    const jsize len = env->GetStringUTFLength(jstr);
    const char* strChars = env->GetStringUTFChars(jstr, (jboolean *)0);

    std::string result(strChars, len);

    env->ReleaseStringUTFChars(jstr, strChars);

    return result;
}

jfloatArray JNICALL Java_msl_com_interactivejob_JNIHelper_test
(JNIEnv *env, jobject jobj, jstring jstr_classifier, jstring jstr_voclist, jlong jlong_img_pointer, jfloat threshold)

{
    cv::Mat* img = (cv::Mat*) jlong_img_pointer;
    std::string classifier_data = jstring2string(env, jstr_classifier);
    std::string voclist_data = jstring2string(env, jstr_voclist);

    float classId, distance;
    predict(classifier_data, voclist_data, *img, (float) threshold, classId, distance);

    float* data = new float[2];
    data[0] = classId;
    data[1] = distance;
    jfloatArray returnValues;
    returnValues = env->NewFloatArray(2);
    if (returnValues == NULL) {
        return NULL; /* out of memory error thrown */
    }

    (env)->SetFloatArrayRegion(returnValues, 0, 2, data);
    for(int i=0; i<2; i++) {
        LOGI("Result: %f %f ", data[0], data[1]);
    }
    free(data);
    return returnValues;
}
