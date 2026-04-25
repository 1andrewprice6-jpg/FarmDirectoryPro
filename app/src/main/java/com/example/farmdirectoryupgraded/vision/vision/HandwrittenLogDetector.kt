package com.example.farmdirectoryupgraded.vision.vision

import com.example.farmdirectoryupgraded.vision.capture.DetectedRegion

/**
 * Turns a flat list of text regions (from ML Kit) into a structured table.
 *
 * Approach:
 *   1. Cluster regions into ROWS by y-coordinate proximity.
 *   2. Within each row, sort by x-coordinate — that's the column order.
 *   3. If a header row can be detected (first row with mostly non-numeric text),
 *      use it to name columns; otherwise emit anonymous "col0, col1, ..."
 *
 * Good for:
 *   - Chicken house daily log sheets (Date | Mort | Feed lb | Water gal | Temp)
 *   - Fuel cards (Date | Vehicle | Gal | Price | Mileage)
 *   - Maintenance logbooks
 *
 * Not good for:
 *   - Heavily merged cells, multi-page spreads
 *   - Free-form handwritten notes (use GENERIC_LOG mode)
 */
object HandwrittenLogDetector {

    data class Row(val cells: List<DetectedRegion>) {
        val centerY: Int get() = cells.map { (it.top + it.bottom) / 2 }.average().toInt()
    }

    data class Table(
        val header: List<String>,   // normalized column names, or "col0"..."colN"
        val rows: List<List<String>>,   // each inner list matches header.size
    )

    /**
     * @param rowToleranceFrac fraction of image height within which regions
     *                         are considered the same row. 0.02 ≈ 2% of frame height
     *                         is a good default for standard-size log sheets.
     */
    fun buildTable(
        regions: List<DetectedRegion>,
        imageHeight: Int,
        rowToleranceFrac: Double = 0.02,
    ): Table {
        if (regions.isEmpty()) return Table(emptyList(), emptyList())
        val tol = (imageHeight * rowToleranceFrac).toInt().coerceAtLeast(8)

        // Cluster rows
        val sorted = regions.sortedBy { (it.top + it.bottom) / 2 }
        val rowBuckets = mutableListOf<MutableList<DetectedRegion>>()
        for (r in sorted) {
            val cy = (r.top + r.bottom) / 2
            val bucket = rowBuckets.lastOrNull()
            if (bucket != null) {
                val lastCy = bucket.last().let { (it.top + it.bottom) / 2 }
                if (kotlin.math.abs(cy - lastCy) <= tol) {
                    bucket += r
                    continue
                }
            }
            rowBuckets += mutableListOf(r)
        }
        val rows = rowBuckets.map { b -> Row(b.sortedBy { it.left }) }

        // Detect header — first row with >50% non-numeric cells
        val (headerRow, bodyRows) = pickHeader(rows)

        val header: List<String> = if (headerRow != null) {
            headerRow.cells.map { normalizeHeader(it.rawText) }
        } else {
            val cols = rows.maxOfOrNull { it.cells.size } ?: 0
            (0 until cols).map { "col$it" }
        }
        val cols = header.size

        val bodyText = bodyRows.map { row ->
            val padded = MutableList(cols) { "" }
            row.cells.take(cols).forEachIndexed { i, cell -> padded[i] = cell.rawText.trim() }
            padded
        }
        return Table(header, bodyText)
    }

    private fun pickHeader(rows: List<Row>): Pair<Row?, List<Row>> {
        if (rows.isEmpty()) return null to emptyList()
        val first = rows.first()
        val nonNumericCells = first.cells.count { cell ->
            val digits = cell.rawText.count { it.isDigit() }
            digits.toDouble() / cell.rawText.length.coerceAtLeast(1) < 0.3
        }
        val looksLikeHeader = first.cells.isNotEmpty() &&
                nonNumericCells >= first.cells.size / 2
        return if (looksLikeHeader) first to rows.drop(1) else null to rows
    }

    private fun normalizeHeader(raw: String): String =
        raw.trim().lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "col" }
}
