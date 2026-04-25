// backend/src/logs.ts
//
// Three downstream log endpoints + gauge calibrations.
//
// Wire into Express:
//   import logsRouter from './logs';
//   app.use('/api', logsRouter);
//
// Endpoints:
//   GET    /api/fuel-logs                ?farmId=&since=
//   POST   /api/fuel-logs
//   DELETE /api/fuel-logs/:id
//
//   GET    /api/mileage                  ?farmId=
//   POST   /api/mileage
//   DELETE /api/mileage/:id
//
//   GET    /api/chicken-house-logs       ?farmId=&since=
//   POST   /api/chicken-house-logs
//   DELETE /api/chicken-house-logs/:id
//
//   GET    /api/gauge-calibrations
//   PUT    /api/gauge-calibrations/:gaugeId
//   DELETE /api/gauge-calibrations/:gaugeId

import { Router, Request, Response } from "express";
import { prisma } from "./db";

const router: Router = Router();

// ─────────────────────────── Fuel logs ────────────────────────────────────
router.get("/fuel-logs", async (req, res) => {
  const farmId = (req.query.farmId as string) || undefined;
  const since = req.query.since ? new Date(Number(req.query.since)) : undefined;
  const rows = await prisma.fuelLog.findMany({
    where: { farmId, capturedAt: since ? { gte: since } : undefined },
    orderBy: { capturedAt: "desc" },
  });
  res.json(rows);
});

router.post("/fuel-logs", async (req, res) => {
  try {
    const b = req.body;
    const created = await prisma.fuelLog.create({
      data: {
        capturedAt: new Date(b.capturedAtEpochMs ?? Date.now()),
        farmId: b.farmId ?? null,
        totalCost: b.totalCost ?? null,
        gallons: b.gallons ?? null,
        pricePerGallon: b.pricePerGallon ?? null,
        odometerMiles: b.odometerMiles ?? null,
        sourceCaptureId: b.sourceCaptureId ?? null,
        notes: b.notes ?? null,
      },
      select: { id: true },
    });
    res.status(201).json(created);
  } catch (e: any) {
    res.status(500).json({ error: e?.message ?? "create failed" });
  }
});

router.delete("/fuel-logs/:id", async (req, res) => {
  const id = Number(req.params.id);
  if (!Number.isFinite(id)) return res.status(400).json({ error: "invalid id" });
  await prisma.fuelLog.delete({ where: { id } });
  res.json({ deleted: true });
});

// ─────────────────────────── Mileage ──────────────────────────────────────
router.get("/mileage", async (req, res) => {
  const farmId = (req.query.farmId as string) || undefined;
  const rows = await prisma.mileageEntry.findMany({
    where: { farmId },
    orderBy: { capturedAt: "desc" },
  });
  res.json(rows);
});

router.post("/mileage", async (req, res) => {
  try {
    const b = req.body;
    if (typeof b.odometerMiles !== "number") {
      return res.status(400).json({ error: "odometerMiles required" });
    }
    const created = await prisma.mileageEntry.create({
      data: {
        capturedAt: new Date(b.capturedAtEpochMs ?? Date.now()),
        odometerMiles: b.odometerMiles,
        farmId: b.farmId ?? null,
        sourceCaptureId: b.sourceCaptureId ?? null,
        notes: b.notes ?? null,
      },
      select: { id: true },
    });
    res.status(201).json(created);
  } catch (e: any) {
    res.status(500).json({ error: e?.message ?? "create failed" });
  }
});

router.delete("/mileage/:id", async (req, res) => {
  const id = Number(req.params.id);
  if (!Number.isFinite(id)) return res.status(400).json({ error: "invalid id" });
  await prisma.mileageEntry.delete({ where: { id } });
  res.json({ deleted: true });
});

// ─────────────────────── Chicken house logs ───────────────────────────────
router.get("/chicken-house-logs", async (req, res) => {
  const farmId = (req.query.farmId as string) || undefined;
  const since = req.query.since ? new Date(Number(req.query.since)) : undefined;
  const rows = await prisma.chickenHouseLog.findMany({
    where: { farmId, createdAt: since ? { gte: since } : undefined },
    orderBy: { createdAt: "desc" },
  });
  res.json(rows);
});

router.post("/chicken-house-logs", async (req, res) => {
  try {
    const b = req.body;
    const created = await prisma.chickenHouseLog.create({
      data: {
        dateIso: b.dateIso ?? null,
        farmId: b.farmId ?? null,
        metricsJson: typeof b.metrics === "string" ? b.metrics : JSON.stringify(b.metrics ?? {}),
        sourceCaptureId: b.sourceCaptureId ?? null,
        notes: b.notes ?? null,
      },
      select: { id: true },
    });
    res.status(201).json(created);
  } catch (e: any) {
    res.status(500).json({ error: e?.message ?? "create failed" });
  }
});

router.delete("/chicken-house-logs/:id", async (req, res) => {
  const id = Number(req.params.id);
  if (!Number.isFinite(id)) return res.status(400).json({ error: "invalid id" });
  await prisma.chickenHouseLog.delete({ where: { id } });
  res.json({ deleted: true });
});

// ─────────────────────── Gauge calibrations ───────────────────────────────
router.get("/gauge-calibrations", async (_req, res) => {
  const rows = await prisma.gaugeCalibration.findMany({
    orderBy: { gaugeLabel: "asc" },
  });
  res.json(rows);
});

router.put("/gauge-calibrations/:gaugeId", async (req, res) => {
  const gaugeId = req.params.gaugeId;
  const b = req.body;
  try {
    const created = await prisma.gaugeCalibration.upsert({
      where: { gaugeId },
      create: {
        gaugeId,
        gaugeLabel: b.gaugeLabel ?? gaugeId,
        centerXNorm: b.centerXNorm,
        centerYNorm: b.centerYNorm,
        radiusNorm: b.radiusNorm,
        minAngleDeg: b.minAngleDeg,
        minValue: b.minValue,
        maxAngleDeg: b.maxAngleDeg,
        maxValue: b.maxValue,
        unit: b.unit,
        counterclockwise: !!b.counterclockwise,
      },
      update: {
        gaugeLabel: b.gaugeLabel ?? gaugeId,
        centerXNorm: b.centerXNorm,
        centerYNorm: b.centerYNorm,
        radiusNorm: b.radiusNorm,
        minAngleDeg: b.minAngleDeg,
        minValue: b.minValue,
        maxAngleDeg: b.maxAngleDeg,
        maxValue: b.maxValue,
        unit: b.unit,
        counterclockwise: !!b.counterclockwise,
      },
    });
    res.json(created);
  } catch (e: any) {
    res.status(500).json({ error: e?.message ?? "upsert failed" });
  }
});

router.delete("/gauge-calibrations/:gaugeId", async (req, res) => {
  const gaugeId = req.params.gaugeId;
  await prisma.gaugeCalibration.delete({ where: { gaugeId } });
  res.json({ deleted: true });
});

export default router;
