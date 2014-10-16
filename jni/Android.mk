LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := SMIMap
LOCAL_SRC_FILES := SMIMap.cpp
LOCAL_SHARED_LIBRARIES := runtimecore_java-prebuilt

include $(BUILD_SHARED_LIBRARY)

include $(LOCAL_PATH)/prebuilt/Android.mk