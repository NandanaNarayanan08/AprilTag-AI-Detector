/**
 * Represents a 3D Pose rotation profile.
 */
export interface TagPose {
  pitch: number; // degrees
  roll: number;  // degrees
  yaw: number;   // degrees
  xOffset: number; // relative side offset
  yOffset: number; // relative vertical height offset
  zDepth: number;  // actual depth distance
}

/**
 * Represents a real-time detected AprilTag in the frame.
 */
export interface DetectedTag {
  id: number;
  family: string;
  confidence: number;
  distance: number; // in meters (e.g., 1.42)
  pose: TagPose;
  boundingBox: [number, number][]; // 4 points [x, y] in percentage or pixel space
  center: [number, number]; // [x, y]
  timestamp: string;
}

/**
 * Scan history item saved locally.
 */
export interface ScanHistoryItem {
  id: string; // uuid or serial timestamp
  tagId: number;
  family: string;
  timestamp: string;
  distance: number;
  pitch: number;
  roll: number;
  yaw: number;
  scenario: string;
  notes?: string;
  confidence?: number;
  screenshot?: string;
  explanation?: string;
}

/**
 * Built-in educational material module.
 */
export interface LearningModule {
  id: string;
  title: string;
  subtitle: string;
  icon: string; // Lucide icon name
  category: "Computer Vision" | "Robotics" | "Mathematics" | "Hardware";
  description: string;
  difficulty: "Beginner" | "Intermediate" | "Advanced";
  readTime: string;
  sections: {
    heading: string;
    body: string;
    illustrationType?: "tag" | "pose" | "drone" | "matrix" | "coordinates";
  }[];
}

/**
 * Simulative computer vision layout scenarios.
 */
export interface EnvironmentScenario {
  id: string;
  name: string;
  description: string;
  tagsActive: {
    id: number;
    distance: number;
    pose: TagPose;
  }[];
}
