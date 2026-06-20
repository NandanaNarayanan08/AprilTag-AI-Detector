import { LearningModule, EnvironmentScenario } from "../types";

export const learningModules: LearningModule[] = [
  {
    id: "what-are-apriltags",
    title: "What are AprilTags?",
    subtitle: "Understanding visual fiducial marker systems",
    icon: "Tag",
    category: "Computer Vision",
    description: "Learn about fiducial markers, how they differ from QR codes, and why they are vital in real-time camera target tracking.",
    difficulty: "Beginner",
    readTime: "4 min",
    sections: [
      {
        heading: "A Digital Reference in the Real World",
        body: "An AprilTag is a visual fiducial system (similar to a highly specialized QR code) developed by researchers at the University of Michigan. Unlike QR codes which are engineered to encode paragraphs of text or URLs, AprilTags encode a single integer ID. This restraint allows them to be scanned at massive distances, in extremely low lighting, and with severe motion blur.",
        illustrationType: "tag"
      },
      {
        heading: "Error Detection and Hamming Distance",
        body: "AprilTags use modern coding theory (families like block codes) to ensure robustness. The pattern of black and white blocks is designed with a high 'Hamming Distance'. This means that even if several pixels are corrupted or covered, the algorithm can recognize the tag, correct the reading, and eliminate false positives."
      },
      {
        heading: "Why Robotics Prefers AprilTags",
        body: "Robots do not have human intuition. They see grids of color. By placing an AprilTag on a charging dock, a robotic arm, or a threshold, we define a precise coordinate origin in 3D physical space, making autonomous docking, grasping, and maneuvering mathematically simple."
      }
    ]
  },
  {
    id: "pose-estimation",
    title: "Pose Estimation & Camera Intrinsics",
    subtitle: "Calculating translation and rotation matrices",
    icon: "Compass",
    category: "Mathematics",
    description: "Unlock the geometry of how a 2D camera sensor derives exact 3D coordinates (X, Y, Z) and orientation angles (pitch, roll, yaw) from a square tag.",
    difficulty: "Advanced",
    readTime: "7 min",
    sections: [
      {
        heading: "The Perspective-n-Point (PnP) Problem",
        body: "Pose estimation is the process of finding the position and orientation of an object relative to the camera. Because we know the physical size of the AprilTag (e.g., exactly 15cm wide) and its geometrical quad corners, OpenCV can map these absolute points to the 2D pixel coordinates on the image sensor. This is solved mathematically using the Perspective-n-Point (PnP) algorithm.",
        illustrationType: "pose"
      },
      {
        heading: "Decoding the Calibration Matrix",
        body: "To compute the pose, OpenCV needs 'Camera Intrinsics' - focal length (fx, fy), principal points (cx, cy), and radial distortion parameters. Camera calibration files are essential; they describe how light is bent by your device's lens. Without calibration, the distance estimation will have minor scale distortions."
      },
      {
        heading: "Translation and Rotation Vectors",
        body: "The final pose is represented by two values: Translation (T) which defines the object's distance along X (left/right), Y (up/down), and Z (depth) axes; and Rotation (R) which determines Euler angles (Pitch, Roll, and Yaw). Pitch is rotation relative to horizontal, Roll is tiling side to side, and Yaw is swivel."
      }
    ]
  },
  {
    id: "opencv-fundamentals",
    title: "OpenCV & Thresholding",
    subtitle: "How computer vision detects squares",
    icon: "Cpu",
    category: "Computer Vision",
    description: "Analyze the core OpenCV steps: grayscaling, adaptive thresholding, contour extraction, and line fitting.",
    difficulty: "Intermediate",
    readTime: "5 min",
    sections: [
      {
        heading: "1. Grayscaling and Noise Elimination",
        body: "The detection pipeline begins by converting the color RGB video frame into a single-channel grayscale image. High frequencies and noisy pixels are filtered out, focusing purely on contrast values.",
        illustrationType: "matrix"
      },
      {
        heading: "2. Adaptive Thresholding",
        body: "To find dark tags on bright surfaces, the image undergoes adaptive thresholding. This converts pixels to absolute binary black or white, depending on localized neighbor contrast, neutralizing glares or shadows."
      },
      {
        heading: "3. Contour Analysis & Corner Fitting",
        body: "Next, the system detects edge boundaries (contours). The algorithm searches specifically for quadrilaterals (polygons with exactly 4 corners). Each potential rectangle is analyzed to see if its inner payload matches a valid AprilTag bit matrix pattern."
      }
    ]
  },
  {
    id: "drone-landings",
    title: "Drone Landing Systems",
    subtitle: "Precision autonomous descents via visual feedback",
    icon: "Navigation",
    category: "Robotics",
    description: "Discover how commercial drones align themselves in heavy winds to land autonomously on precise helipads using AprilTags.",
    difficulty: "Intermediate",
    readTime: "6 min",
    sections: [
      {
        heading: "The GPS Accuracy Problem",
        body: "Standard GPS has a drift error of 1 to 5 meters. If an autonomous chemical spraying drone attempts to land on a narrow docking station, a 1-meter drift can lead to a crash. Drones need centimetre-level feedback during the final descent.",
        illustrationType: "drone"
      },
      {
        heading: "AprilTag Visual Servo Control",
        body: "By pointing a camera straight down, the drone's onboard flight controller searches for an AprilTag on the landing pad. The moment the tag is found, the loop switches to 'Visual Servoing'. This dynamically adjustments roll/pitch to align the camera center directly above the AprilTag center.",
      },
      {
        heading: "Multi-Scale Nested Tag Layers",
        body: "As the drone descends, a massive AprilTag will eventually leave the camera's field of view. To counter this, engineers place multiple scales of tags nested inside each other. A huge tag for high altitudes, and tight small tags inside for the final 10cm alignment."
      }
    ]
  }
];

export const environmentScenarios: EnvironmentScenario[] = [
  {
    id: "general",
    name: "General Workspace",
    description: "Standard table alignment setup with mixed tags marking boundaries.",
    tagsActive: [
      { id: 12, distance: 0.95, pose: { pitch: 12, roll: -4, yaw: 8, xOffset: -0.15, yOffset: 0.05, zDepth: 0.95 } },
      { id: 45, distance: 1.64, pose: { pitch: -2, roll: 0, yaw: -15, xOffset: 0.3, yOffset: -0.1, zDepth: 1.64 } }
    ]
  },
  {
    id: "drone",
    name: "Autonomous UAV Landing Pad",
    description: "Horizontal ground helicopter pad with a dual-scale visual target.",
    tagsActive: [
      { id: 0, distance: 3.42, pose: { pitch: 1, roll: 85, yaw: 0, xOffset: -0.05, yOffset: 0.2, zDepth: 3.42 } },
      { id: 3, distance: 0.72, pose: { pitch: 0, roll: 90, yaw: -45, xOffset: 0.12, yOffset: 0.12, zDepth: 0.72 } }
    ]
  },
  {
    id: "robotics",
    name: "6-DoF Robotic Arm Servoing",
    description: "AprilTag mounted on an end-effector tool to align precise grasping operations.",
    tagsActive: [
      { id: 110, distance: 0.45, pose: { pitch: 45, roll: -12, yaw: 35, xOffset: 0.02, yOffset: 0.0, zDepth: 0.45 } },
      { id: 114, distance: 0.52, pose: { pitch: 15, roll: 10, yaw: -10, xOffset: -0.22, yOffset: 0.15, zDepth: 0.52 } }
    ]
  },
  {
    id: "warehouse",
    name: "Warehouse AGV Grid Localization",
    description: "AprilTags painted on floors or ceilings at regular grid layouts for mobile robot visual odometry.",
    tagsActive: [
      { id: 401, distance: 2.15, pose: { pitch: 0, roll: 0, yaw: 90, xOffset: -0.4, yOffset: -0.3, zDepth: 2.15 } },
      { id: 402, distance: 2.50, pose: { pitch: 0, roll: 0, yaw: 90, xOffset: 0.4, yOffset: -0.3, zDepth: 2.50 } },
      { id: 403, distance: 2.85, pose: { pitch: 0, roll: 0, yaw: 90, xOffset: 0.0, yOffset: -0.3, zDepth: 2.85 } }
    ]
  }
];
