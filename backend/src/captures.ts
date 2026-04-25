// backend/src/captures.ts
//
// POST /api/captures           — multipart upload (image + parsedFields JSON)
// GET  /api/captures           — list recent
// GET  /api/captures/:id       — load full record
// GET  /api/captures/:id/raw   — stream the archived JPEG
// DELETE /api/captures/:id     — forensic delete (images+row)
//
// Wire into Express:
//   import capturesRouter from './captures';
//   app.use('/api/captures', capturesRouter);

import { Router, Request, Response } from "express";
import multer from "multer";
import path from "path";
import fs from "fs";
import { prisma } from "./db";

const UPLOAD_DIR = process.env.CAPTURE_STORAGE_DIR ?? path.resolve(process.cwd(), "capture_storage");
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, UPLOAD_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname) || ".jpg";
    const unique = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}${ext}`;
    cb(null, unique);
  },
});
const upload = multer({
  storage,
  limits: { fileSize: 25 * 1024 * 1024 }, // 25 MB / image
  fileFilter: (_req, file, cb) => {
    const ok = file.mimetype.startsWith("image/");
    cb(ok ? null : new Error("Only image uploads permitted"), ok);
  },
});

const router: Router = Router();

router.post("/", upload.single("image"), async (req: Request, res: Response) => {
  try {
    const file = req.file;
    if (!file) return res.status(400).json({ error: "image file required" });

    const fieldsJson = req.body.parsedFields as string | undefined;
    const mode = req.body.mode as string | undefined;
    const status = req.body.status as string | undefined;
    const capturedAtEpochMs = Number(req.body.capturedAtEpochMs ?? Date.now());
    const farmId = req.body.farmId as string | undefined;
    const latitude = req.body.latitude !== undefined ? Number(req.body.latitude) : null;
    const longitude = req.body.longitude !== undefined ? Number(req.body.longitude) : null;
    const linkedEntities = req.body.linkedEntities as string | undefined;

    if (!mode) return res.status(400).json({ error: "mode required" });
    if (!status) return res.status(400).json({ error: "status required" });

    const created = await prisma.capture.create({
      data: {
        capturedAt: new Date(capturedAtEpochMs),
        mode,
        status,
        rawImagePath: file.path,
        parsedFields: fieldsJson ?? "{}",
        linkedEntities: linkedEntities ?? "{}",
        farmId: farmId ?? null,
        latitude,
        longitude,
      },
      select: { id: true },
    });

    return res.status(201).json({ id: created.id });
  } catch (e: any) {
    return res.status(500).json({ error: e?.message ?? "upload failed" });
  }
});

router.get("/", async (req: Request, res: Response) => {
  const limit = Math.min(Number(req.query.limit ?? 50), 500);
  const farmId = (req.query.farmId as string) || undefined;
  const mode = (req.query.mode as string) || undefined;
  const status = (req.query.status as string) || undefined;

  const list = await prisma.capture.findMany({
    where: { farmId, mode, status },
    orderBy: { capturedAt: "desc" },
    take: limit,
    select: {
      id: true, capturedAt: true, mode: true, status: true,
      farmId: true, parsedFields: true, linkedEntities: true,
    },
  });
  return res.json(list);
});

router.get("/:id", async (req: Request, res: Response) => {
  const id = Number(req.params.id);
  if (!Number.isFinite(id)) return res.status(400).json({ error: "invalid id" });
  const cap = await prisma.capture.findUnique({ where: { id } });
  if (!cap) return res.status(404).json({ error: "not found" });
  return res.json(cap);
});

router.get("/:id/raw", async (req: Request, res: Response) => {
  const id = Number(req.params.id);
  if (!Number.isFinite(id)) return res.status(400).json({ error: "invalid id" });
  const cap = await prisma.capture.findUnique({
    where: { id },
    select: { rawImagePath: true },
  });
  if (!cap || !fs.existsSync(cap.rawImagePath)) return res.status(404).json({ error: "raw image missing" });
  res.setHeader("Content-Type", "image/jpeg");
  fs.createReadStream(cap.rawImagePath).pipe(res);
});

router.delete("/:id", async (req: Request, res: Response) => {
  const id = Number(req.params.id);
  if (!Number.isFinite(id)) return res.status(400).json({ error: "invalid id" });
  const cap = await prisma.capture.findUnique({ where: { id } });
  if (!cap) return res.status(404).json({ error: "not found" });
  try { fs.unlinkSync(cap.rawImagePath); } catch { /* ignore */ }
  await prisma.capture.delete({ where: { id } });
  return res.json({ deleted: true });
});

export default router;
