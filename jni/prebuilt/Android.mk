LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := runtimecore_java-prebuilt
LOCAL_SRC_FILES := libruntimecore_java.so
include $(PREBUILT_SHARED_LIBRARY)
