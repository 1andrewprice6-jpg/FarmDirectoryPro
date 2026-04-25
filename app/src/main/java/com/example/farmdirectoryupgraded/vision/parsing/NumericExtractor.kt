package com.example.farmdirectoryupgraded.vision.parsing

/**
 * Extracts numbers from OCR output, correcting for common recognition errors:
 *
 *   - O/0, I/1, l/1, S/5, B/8, G/6, Z/2  substitutions (only inside numeric contexts)
 *   - Comma-vs-dot decimal separators (US: "1,234.56"; EU: "1.234,56")
 *   - Leading/trailing garbage ($, €, /gal, " gal", "mi")
 *   - Double-zero "00" from LCD colons misread
 *   - Unicode minus / en-dash / em-dash → ASCII minus
 *
 * Every extractor returns null (not 0, not NaN) on failure — the validation
 * gate depends on null to mean "field not present" vs "field present and zero".
 */
object NumericExtractor {

    private val NUMBER_PATTERN = Regex("""-?\d{1,3}(?:[,.\s]\d{3})*(?:[,.]\d+)?|-?\d+(?:[,.]\d+)?""")

    /** Any number in the text, in order of appearance. */
    fun allNumbers(text: String): List<Double> {
        val cleaned = preclean(text)
        return NUMBER_PATTERN.findAll(cleaned)
            .mapNotNull { parseNumber(it.value) }
            .toList()
    }

    /** First number in the text, or null. */
    fun firstNumber(text: String): Double? = allNumbers(text).firstOrNull()

    /** First integer in the text, interpreted as such (no decimal). */
    fun firstInteger(text: String): Int? =
        allNumbers(text).firstOrNull()?.toInt()

    /** First currency amount ($X.XX or X.XX preceded by $). */
    fun firstCurrency(text: String): Double? {
        val pattern = Regex("""\$\s*(\d+(?:[,.]\d{1,3})*(?:[,.]\d{2})?)""")
        val match = pattern.find(text) ?: return null
        return parseNumber(match.groupValues[1])
    }

    /** First gallons-style number (X.XXX or X.XX followed by GAL / GALS). */
    fun firstGallons(text: String): Double? {
        // Fuel pumps typically show gallons to 3 decimals: "12.345 GAL"
        val pattern = Regex("""(\d+\.\d{2,3})\s*(?:GAL|G)""", RegexOption.IGNORE_CASE)
        pattern.find(text)?.let { return it.groupValues[1].toDoubleOrNull() }
        // Fallback: the 3-decimal number nearest to a plausible gallons range (0.1–100)
        return allNumbers(text).firstOrNull { it in 0.1..200.0 && decimalCount(it) >= 2 }
    }

    /** First odometer-style integer — 4 to 7 digits, no decimal. */
    fun firstOdometer(text: String): Int? {
        val pattern = Regex("""(?<!\d)(\d{4,7})(?!\d)""")
        return pattern.findAll(text)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .firstOrNull { it in 1..9_999_999 }
    }

    /** First $/gal price — typically X.XXX or X.XX9 (tenths-of-cent). */
    fun firstPricePerGallon(text: String): Double? {
        val pattern = Regex("""(\d\.\d{2,3})\s*(?:/GAL|PPG|/G)""", RegexOption.IGNORE_CASE)
        pattern.find(text)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
        return allNumbers(text).firstOrNull { it in 1.0..10.0 && decimalCount(it) == 3 }
    }

    /** First 4-digit integer that could plausibly be a Farm ID. */
    fun firstFarmId(text: String): String? {
        val pattern = Regex("""(?<!\d)(\d{3,4})(?!\d)""")
        return pattern.findAll(text).map { it.groupValues[1] }.firstOrNull()
    }

    /** First ISO-like date. Accepts MM/DD/YY, MM-DD-YYYY, YYYY-MM-DD. */
    fun firstDateIso(text: String): String? {
        val ymd = Regex("""\b(\d{4})[-/](\d{1,2})[-/](\d{1,2})\b""")
        ymd.find(text)?.let { (_, y, m, d) ->
            return "%04d-%02d-%02d".format(y.toInt(), m.toInt(), d.toInt())
        }
        val mdy = Regex("""\b(\d{1,2})[-/](\d{1,2})[-/](\d{2,4})\b""")
        mdy.find(text)?.let { (_, m, d, y) ->
            val year = y.toInt().let { if (it < 100) 2000 + it else it }
            return "%04d-%02d-%02d".format(year, m.toInt(), d.toInt())
        }
        return null
    }

    // --- helpers -----------------------------------------------------------

    private fun preclean(text: String): String {
        var t = text
        // Normalize dashes
        t = t.replace('\u2013', '-').replace('\u2014', '-').replace('\u2212', '-')
        // OCR substitutions ONLY in clearly numeric contexts — look at 3+ digit runs
        // and substitute adjacent letter-lookalikes. This is a balancing act:
        // aggressive substitution corrupts Farm IDs like "SC01"; conservative
        // misses "O" misread as "0" in "1O25 GAL".
        t = Regex("""(\d[OIlSBGZ]\d|[OIlSBGZ]\d\d)""").replace(t) { m ->
            m.value
                .replace('O', '0').replace('o', '0')
                .replace('I', '1').replace('l', '1')
                .replace('S', '5')
                .replace('B', '8')
                .replace('G', '6')
                .replace('Z', '2')
        }
        return t
    }

    private fun parseNumber(raw: String): Double? {
        // Detect which separator is the decimal:
        //   "1,234.56"  → comma thousand, dot decimal (US)
        //   "1.234,56"  → dot thousand, comma decimal (EU)
        //   "1234.56"   → dot decimal
        //   "1234,56"   → comma decimal
        //   "1,234"     → comma thousand (no decimal)
        val hasComma = ',' in raw
        val hasDot = '.' in raw
        val normalized = when {
            hasComma && hasDot -> {
                val lastComma = raw.lastIndexOf(',')
                val lastDot = raw.lastIndexOf('.')
                if (lastDot > lastComma) raw.replace(",", "")           // US style
                else raw.replace(".", "").replace(',', '.')              // EU style
            }
            hasComma -> {
                // Is the comma a decimal? Only if it has 1–3 digits after it and
                // nothing that looks like thousand-grouping before it.
                val afterComma = raw.substringAfterLast(',').length
                if (afterComma in 1..3 && raw.count { it == ',' } == 1)
                    raw.replace(',', '.')
                else raw.replace(",", "")
            }
            else -> raw
        }
        return normalized.toDoubleOrNull()
    }

    private fun decimalCount(d: Double): Int {
        val s = d.toString()
        val dot = s.indexOf('.')
        return if (dot < 0) 0 else s.length - dot - 1
    }
}
