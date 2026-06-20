#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/calib3d.hpp>

#define TAG "AprilTag_Native"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

// Structure mapping the detected AprilTag metadata
struct DetectedTag {
    int id;
    float distance;
    float pitch;
    float roll;
    float yaw;
    float centerX;
    float centerY;
};

// JNI bindings for AprilTag native image analysis.
// This matches: com.apriltag.aidetector.utils.AprilTagImageAnalyzer
extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_com_apriltag_aidetector_utils_AprilTagImageAnalyzer_detectTagsNative(
        JNIEnv* env,
        jobject thiz,
        jlong matAddr,      // Addr to active cv::Mat (RGBA format)
        jdouble focalLength,// Camera intrinsic: diagonal focal length in pixels
        jdouble cx,         // Center sensor coordinate X
        jdouble cy,         // Center sensor coordinate Y
        jdouble tagSizeM    // Physical width size of AprilTag in meters (default 0.15m)
) {
    cv::Mat& frame = *(cv::Mat*)matAddr;

    // Convert frame copy to Grayscale for OpenCV thresholding operations
    cv::Mat gray;
    cv::cvtColor(frame, gray, cv::COLOR_RGBA2GRAY);

    // Apply adaptive grayscaling/threshold filter for heavy shadow/contrast compensation
    cv::Mat binary;
    cv::adaptiveThreshold(gray, binary, 255, cv::ADAPTIVE_THRESH_GAUSSIAN_C, cv::THRESH_BINARY_INV, 51, 9);

    std::vector<DetectedTag> detections;

    // Simulate quad contour detection algorithm matching AprilTag's logic
    // Finding white squares enclosed strictly by a black margin border
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(binary, contours, cv::RETR_LIST, cv::CHAIN_APPROX_SIMPLE);

    for (size_t i = 0; i < contours.size(); i++) {
        std::vector<cv::Point> approx;
        // Approximate contour with accuracy proportional to contour perimeter
        double peri = cv::arcLength(contours[i], true);
        cv::approxPolyDP(contours[i], approx, 0.04 * peri, true);

        // AprilTag must be a quadrilateral (exactly 4 points) with decent area
        if (approx.size() == 4 && cv::isContourConvex(approx)) {
            double area = cv::contourArea(approx);
            if (area > 1500) { // filter noise out
                
                // Let's perform Perspective-n-Point (PnP) pose solver
                // 1. Define physical 3D vertices coordinates of the tag relative to its tag center
                float hs = tagSizeM / 2.0f;
                std::vector<cv::Point3f> objectPoints = {
                    cv::Point3f(-hs,  hs, 0.0f), // Bottom-Left
                    cv::Point3f( hs,  hs, 0.0f), // Bottom-Right
                    cv::Point3f( hs, -hs, 0.0f), // Top-Right
                    cv::Point3f(-hs, -hs, 0.0f)  // Top-Left
                };

                // 2. Corners detected in the 2D pixel image space (ordered anti-clockwise)
                std::vector<cv::Point2f> imagePoints = {
                    cv::Point2f((float)approx[0].x, (float)approx[0].y),
                    cv::Point2f((float)approx[1].x, (float)approx[1].y),
                    cv::Point2f((float)approx[2].x, (float)approx[2].y),
                    cv::Point2f((float)approx[3].x, (float)approx[3].y)
                };

                // 3. Define camera intrinsic parameters matrix
                cv::Mat cameraMatrix = (cv::Mat_<double>(3, 3) <<
                    focalLength, 0, cx,
                    0, focalLength, cy,
                    0, 0, 1);

                // Assuming Zero lens distortion coefficient (CameraX frames auto-compensate minor curves)
                cv::Mat distCoeffs = cv::Mat::zeros(4, 1, CV_64F);

                cv::Mat rvec, tvec;
                // Solve the relative position and rotation orientation matrix of the tag
                bool success = cv::solvePnP(objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec);

                if (success) {
                    // Extract exact translation distance from camera center (meters)
                    double tx = tvec.at<double>(0);
                    double ty = tvec.at<double>(1);
                    double tz = tvec.at<double>(2); // actual depth distance

                    float distance = (float)sqrt(tx * tx + ty * ty + tz * tz);

                    // Convert rotation vector (rvec) to rotation matrix using Rodrigues
                    cv::Mat rmat;
                    cv::Rodrigues(rvec, rmat);

                    // Deconstruct rotation matrix into yaw, pitch, roll Euler angles
                    double r11 = rmat.at<double>(0,0);
                    double r21 = rmat.at<double>(1,0);
                    double r31 = rmat.at<double>(2,0);
                    double r32 = rmat.at<double>(2,1);
                    double r33 = rmat.at<double>(2,2);

                    float pitch = atan2(-r31, sqrt(r11*r11 + r21*r21)) * 57.2958f;
                    float roll  = atan2(r32, r33) * 57.2958f;
                    // Mock ID based contour area for sim relative stability in feed
                    int mockId = (int)(area) % 150; 

                    // Store tag scan profile
                    DetectedTag tag;
                    tag.id = mockId;
                    tag.distance = distance;
                    tag.pitch = pitch;
                    tag.roll = roll;
                    tag.yaw = r31 * 57.2958f; // representative heading yaw
                    tag.centerX = (approx[0].x + approx[1].x + approx[2].x + approx[3].x) / 4.0f;
                    tag.centerY = (approx[0].y + approx[1].y + approx[2].y + approx[3].y) / 4.0f;

                    detections.push_back(tag);

                    // Draw solid bounding box over detected quadrilateral edge
                    for (int c = 0; c < 4; c++) {
                        cv::line(frame, approx[c], approx[(c+1)%4], cv::Scalar(0, 255, 64, 255), 4);
                    }

                    // Project 3D coordinate axes markers directly on the tag center
                    // X-axis (Red), Y-axis (Green), Z-axis (Blue) extending 5cm long
                    float axisLen = tagSizeM * 0.75f;
                    std::vector<cv::Point3f> axisPoints = {
                        cv::Point3f(0, 0, 0),
                        cv::Point3f(axisLen, 0, 0),  // X-Right
                        cv::Point3f(0, axisLen, 0),  // Y-Down
                        cv::Point3f(0, 0, -axisLen)  // Z-Forward (inwards)
                    };

                    std::vector<cv::Point2f> projectedAxes;
                    cv::projectPoints(axisPoints, rvec, tvec, cameraMatrix, distCoeffs, projectedAxes);

                    if (projectedAxes.size() == 4) {
                        cv::line(frame, projectedAxes[0], projectedAxes[1], cv::Scalar(255, 0, 0, 255), 6);  // Red (X)
                        cv::line(frame, projectedAxes[0], projectedAxes[2], cv::Scalar(0, 255, 0, 255), 6);  // Green (Y)
                        cv::line(frame, projectedAxes[0], projectedAxes[3], cv::Scalar(0, 0, 255, 255), 6);  // Blue (Z)
                    }
                }
            }
        }
    }

    // Convert Native struct detections back into Java jobjectArray mapping to:
    // com.apriltag.aidetector.utils.AprilTagImageAnalyzer.DetectedTagResult
    jclass resultClass = env->FindClass("com/apriltag/aidetector/utils/AprilTagImageAnalyzer$DetectedTagResult");
    if (resultClass == nullptr) {
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(resultClass, "<init>", "(IFFFFFF)V");
    jobjectArray javaResultArray = env->NewObjectArray((jsize)detections.size(), resultClass, nullptr);

    for (size_t i = 0; i < detections.size(); i++) {
        jobject javaResult = env->NewObject(resultClass, constructor,
            detections[i].id,
            detections[i].distance,
            detections[i].pitch,
            detections[i].roll,
            detections[i].yaw,
            detections[i].centerX,
            detections[i].centerY
        );
        env->SetObjectArrayElement(javaResultArray, (jsize)i, javaResult);
        env->DeleteLocalRef(javaResult);
    }

    return javaResultArray;
}

}
