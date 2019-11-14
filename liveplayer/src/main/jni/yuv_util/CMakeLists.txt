add_library(yuv_util SHARED yuv_util.cc)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../libyuv/include)
set(lib_dir ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../app/src/main/jniLibs)
set_target_properties(yuv_util
        PROPERTIES LIBRARY_OUTPUT_DIRECTORY
        ${lib_dir}/${ANDROID_ABI})
find_library(log-lib log)

target_link_libraries(
        yuv_util
        yuv
        ${log-lib})
