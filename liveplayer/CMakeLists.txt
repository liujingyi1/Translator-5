cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(src/main/jni/libyuv ../app/src/main/jniLibs/${ANDROID_ABI})

add_subdirectory(src/main/jni/yuv_util)