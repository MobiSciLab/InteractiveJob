# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE := opencv_hal
LOCAL_SRC_FILES := $(LOCAL_PATH)/../jniLibs/armeabi-v7a/libopencv_hal.a

include $(PREBUILT_STATIC_LIBRARY)


#Script build xfeatured2d
include $(CLEAR_VARS)
XFEATURES2D := $(LOCAL_PATH)/xfeatures2d

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off

include $(LOCAL_PATH)/OpenCV.mk

LOCAL_STATIC_LIBRARIES += opencv_hal

LOCAL_MODULE    := libopencv_contrib

LOCAL_SRC_FILES :=  $(XFEATURES2D)/xfeatures2d_init.cpp \
                    $(XFEATURES2D)/brief.cpp \
                    $(XFEATURES2D)/daisy.cpp \
                    $(XFEATURES2D)/freak.cpp \
                    $(XFEATURES2D)/latch.cpp \
                    $(XFEATURES2D)/lucid.cpp \
                    $(XFEATURES2D)/sift.cpp \
                    $(XFEATURES2D)/stardetector.cpp \
                    $(XFEATURES2D)/surf.cpp \

include $(BUILD_STATIC_LIBRARY)

#Script build image recognition
include $(CLEAR_VARS)
IMG_RECOGNITION := $(LOCAL_PATH)/imgrecognition

LOCAL_STATIC_LIBRARIES += libopencv_contrib

LOCAL_MODULE    := libimg_recognition

LOCAL_SRC_FILES := $(IMG_RECOGNITION)/img_recognition.cpp \
                    $(IMG_RECOGNITION)/jni_img_recognition.cpp

# for native log
LOCAL_LDLIBS    += -llog
# for native asset manager
LOCAL_LDLIBS    += -landroid

LOCAL_LDLIBS    += -ldl

include $(BUILD_SHARED_LIBRARY)