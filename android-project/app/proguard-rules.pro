# ProGuard rules for AprilTag AI Detector

# Optimizations for Jetpack Compose, Kotlin standard classes, and Coroutines are pre-packaged.
# Below are specific rules protecting native C++ code bindings and database model bindings from name obfuscation:

# Protect all native (JNI) methods and class names from renaming, as the JNI loader binds them strictly by name:
-keepclasseswithmembernames class * {
    native <methods>;
}

# Protect OpenCV Classes and native methods:
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

# Protect our Kotlin custom native bindings (AprilTagImageAnalyzer):
-keep class com.apriltag.aidetector.utils.AprilTagImageAnalyzer { *; }
-keep class com.apriltag.aidetector.utils.AprilTagImageAnalyzer$** { *; }

# Prevent Room database entities and DAOs from being obfuscated, ensuring SQLite can correctly match tables to entities:
-keep class com.apriltag.aidetector.data.** { *; }
-dontwarn com.apriltag.aidetector.data.**

# Keep standard Google Generative AI references correct:
-keep class com.google.generativeai.** { *; }
-dontwarn com.google.generativeai.**
