package com.example.farmdirectoryupgraded.vision.capture

/**
 * The physical asset being photographed. Drives detector choice,
 * preprocessing, and the downstream validation gate.
 */
enum class CaptureMode(
    val displayName: String,
    val primaryField: String,
    val description: String,
) {
    DASHBOARD_ODOMETER(
        displayName = "Dashboard / Odometer",
        primaryField = "odometerMiles",
        description = "Digital or analog odometer reading",
    ),
    FUEL_PUMP(
        displayName = "Fuel Pump",
        primaryField = "gallons",
        description = "Fuel dispenser display: cost + gallons + price-per-gallon",
    ),
    ANALOG_GAUGE(
        displayName = "Analog Gauge",
        primaryField = "gaugeValue",
        description = "Needle-based pressure, temperature, or tank level gauge",
    ),
    CHICKEN_HOUSE_LOG(
        displayName = "Chicken House Log",
        primaryField = "metrics",
        description = "Handwritten tabular log (mortality, feed, water, temp)",
    ),
    GENERIC_LOG(
        displayName = "Generic Log / Sheet",
        primaryField = "text",
        description = "Any printed or handwritten document for OCR archival",
    );

    /** Mandatory fields — a capture missing any of these is flagged, not committed. */
    val requiredFields: List<String>
        get() = when (this) {
            DASHBOARD_ODOMETER -> listOf("odometerMiles")
            FUEL_PUMP          -> listOf("totalCost", "gallons")
            ANALOG_GAUGE       -> listOf("gaugeValue")
            CHICKEN_HOUSE_LOG  -> listOf("date")
            GENERIC_LOG        -> emptyList()
        }
}
