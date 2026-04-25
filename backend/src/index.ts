import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import path from "path";
import logsRouter from "./logs";
import capturesRouter from "./captures";

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.get("/health", (req, res) => {
  res.json({ status: "ok", timestamp: new Date().toISOString() });
});

app.use("/api/logs", logsRouter); // Note: fdp-vision-full/android/src/main/java/com/farmdirectory/pro/vision/sync/CaptureSyncClient.kt might expect specific paths
app.use("/api/captures", capturesRouter);

// Specific mount points for logs.ts sub-routers if needed, 
// but logs.ts exports a single router with /fuel-logs, /mileage etc.
// Based on INTEGRATION.md, it suggests: app.use('/api', logsRouter);
app.use("/api", logsRouter);

// Start Server
app.listen(port, () => {
  console.log(`🚀 FarmDirectoryPro Backend running at http://localhost:${port}`);
  console.log(`📁 Capture storage: ${process.env.CAPTURE_STORAGE_DIR || path.resolve(process.cwd(), "capture_storage")}`);
});
