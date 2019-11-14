aux_source_directory(source SRC_LIST)

add_library(yuv SHARED ${SRC_LIST})

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)