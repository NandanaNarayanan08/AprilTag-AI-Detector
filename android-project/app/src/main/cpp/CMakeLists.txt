cmake_minimum_required(VERSION 3.22.1)

project("apriltag_native")

# Locate pre-compiled OpenCV Android SDK
# In standard setup, developers import OpenCV as an Android module or specify OpenCV_DIR
find_package(OpenCV REQUIRED)

# Include directories for AprilTag library C-headers and JNI layouts
include_directories(${OpenCV_INCLUDE_DIRS})

# Define the shared native library target containing our JNI wrappers
add_library(
    apriltag_native
    SHARED
    apriltag_native.cpp
)

# Link the native library with the required external SDKs (OpenCV, standard Android log, etc.)
target_link_libraries(
    apriltag_native
    ${OpenCV_LIBS}
    log
)
