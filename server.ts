import express from "express";
import path from "path";
import dotenv from "dotenv";
import { GoogleGenAI, Type } from "@google/genai";
import { createServer as createViteServer } from "vite";

dotenv.config();

const app = express();
const PORT = 3000;

// Initialize Google GenAI on the server with recommended User-Agent header
const ai = new GoogleGenAI({
  apiKey: process.env.GEMINI_API_KEY,
  httpOptions: {
    headers: {
      "User-Agent": "aistudio-build",
    },
  },
});

app.use(express.json({ limit: "15mb" }));

// Route to detect AprilTags in uploaded images using Gemini
app.post("/api/gemini/detect", async (req, res) => {
  try {
    const { imageBase64, mimeType } = req.body;
    if (!imageBase64) {
      return res.status(400).json({ error: "imageBase64 is required" });
    }

    const imagePart = {
      inlineData: {
        data: imageBase64,
        mimeType: mimeType || "image/jpeg"
      }
    };

    const textPart = {
      text: `Identify, locate, and analyze any visual AprilTag fiducial targets (tag36h11 family markers) in this image.
      If you see any squares with black borders and custom inner white matrices (AprilTags, QR-like patterns, or even similar square tags):
      1. Define a unique ID (integer - for example 0, 3, 12, 45, 110, 401).
      2. Provide the confidence level (0.0 to 1.0) of this detection (e.g., 0.92).
      3. Estimate the distance in meters, representing depth (e.g. 1.25m).
      4. Estimate the 3D Pose angles (pitch, roll, yaw in degrees) and center spatial offset (xOffset and yOffset, ranging from -0.5 to 0.5).
      5. Specify the exact four element corner points [[x1, y1], [x2, y2], [x3, y3], [x4, y4]] in percentage of width and height (from 0 to 100), in the clockwise order: Top-Left, Top-Right, Bottom-Right, Bottom-Left of the tag bounds.
      
      Return a list of all detected AprilTags. If none are found, return an empty list.`
    };

    const response = await ai.models.generateContent({
      model: "gemini-3.5-flash",
      contents: [imagePart, textPart],
      config: {
        systemInstruction: "You are an advanced Computer Vision system that detects AprilTag fiducial targets and estimates their 3D Pose parameters from single monocular images. Return the detection parameters in JSON matching the specified schema format.",
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              id: { type: Type.INTEGER, description: "The ID of the detected AprilTag" },
              confidence: { type: Type.NUMBER, description: "Confidence of the detection (0.0 to 1.0)" },
              distance: { type: Type.NUMBER, description: "Estimated distance in meters" },
              pose: {
                type: Type.OBJECT,
                properties: {
                  pitch: { type: Type.INTEGER, description: "Pitch angle in degrees" },
                  roll: { type: Type.INTEGER, description: "Roll angle in degrees" },
                  yaw: { type: Type.INTEGER, description: "Yaw angle in degrees" },
                  xOffset: { type: Type.NUMBER, description: "X offset relative center (-0.5 to 0.5)" },
                  yOffset: { type: Type.NUMBER, description: "Y offset relative center (-0.5 to 0.5)" }
                },
                required: ["pitch", "roll", "yaw", "xOffset", "yOffset"]
              },
              corners: {
                type: Type.ARRAY,
                items: {
                  type: Type.ARRAY,
                  items: { type: Type.NUMBER },
                  description: "A [percentage_x, percentage_y] coordinate on 0..100 scale"
                },
                description: "Four corner coordinates: Top-Left, Top-Right, Bottom-Right, Bottom-Left"
              }
            },
            required: ["id", "confidence", "distance", "pose", "corners"]
          }
        },
        temperature: 0.1
      }
    });

    const results = JSON.parse(response.text || "[]");
    res.json({ success: true, tags: results });
  } catch (error: any) {
    console.error("Gemini Detection Error:", error);
    res.status(500).json({
      error: "Failed to run visual tag detection pipeline.",
      details: error.message || error
    });
  }
});

// API route first: Explain detected AprilTag with Gemini AI
app.post("/api/gemini/explain", async (req, res) => {
  try {
    const { tagId, family, distance, pose, scenario } = req.body;

    if (!tagId) {
      return res.status(400).json({ error: "tagId is required" });
    }

    const systemPrompt = `You are an expert AI Robotics & Computer Vision tutor.
Your task is to provide a beginner-friendly yet highly informative and professional explanation about a tapped AprilTag detection.
Use clear headers, rich bullet points, and clean Markdown. Avoid generic introductions. Dive straight into satisfying the user's curiosity.`;

    const prompt = `Explain AprilTag detection with the following telemetry:
- Tag ID: #${tagId}
- Family: ${family || "tag36h11"}
- Distance: ${distance || "0.85m"}
- Pose Estimation: Pitch ${pose?.pitch || "0.0"}°, Roll ${pose?.roll || "0.0"}°, Yaw ${pose?.yaw || "0.0"}°
- Active Environment/Scenario: ${scenario || "General Robotics Workspace"}

Please structure your response with exact, engaging sections:
1. **Tag Breakdown & Identity**: What does are AprilTag families, specifically #ID and how it is used?
2. **Robotics Applications**: How is this tag used within the "${scenario || "General Robotics"}" context (e.g., drone landing align, mobile rover localization, visual servoing constraints, warehouse logistics)?
3. **Computer Vision & Pose Analysis**: Briefly explain what the pose orientation (Pitch: ${pose?.pitch || "0.0"}°, Roll: ${pose?.roll || "0.0"}°, Yaw: ${pose?.yaw || "0.0"}°) and distance (${distance || "0.85m"}) represent. Add a fun visual representation of the coordinate frame (X-Right, Y-Down, Z-Forward).
4. **Beginner-Friendly Learning Nugget**: Share a simple computer vision term or mathematical concept relevant to this (like Homography, Corner Detection, Intrinsic Camera Matrix Calibration, or Hamming Distance/error correction) in an understandable, delightful analogy.`;

    const response = await ai.models.generateContent({
      model: "gemini-3.5-flash",
      contents: prompt,
      config: {
        systemInstruction: systemPrompt,
        temperature: 0.7,
      },
    });

    const markdownText = response.text || "Failed to generate tag explanation. Please verify Gemini API Key configuration.";
    res.json({ success: true, explanation: markdownText });
  } catch (error: any) {
    console.error("Gemini Explanation Error:", error);
    res.status(500).json({
      error: "Failed to generate AI explanation.",
      details: error.message || error,
    });
  }
});

// Serve the Vite app as dev server or production static files
async function setupVite() {
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`AprilTag AI Detector server listening on port ${PORT}`);
  });
}

setupVite().catch((err) => {
  console.error("Vite setup error:", err);
});
