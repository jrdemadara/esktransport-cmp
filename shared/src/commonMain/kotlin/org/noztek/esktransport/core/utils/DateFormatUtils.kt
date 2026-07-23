package org.noztek.esktransport.core.utils

private val monthLabels = listOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
)

fun String?.formatApiDateForDisplay(fallback: String = "-"): String {
    val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return fallback
    val datePart = value.substringBefore('T').substringBefore(' ')
    val parts = datePart.split("-")
    if (parts.size != 3) return fallback

    val year = parts[0]
    val month = parts[1].toIntOrNull()?.let { monthIndex ->
        monthLabels.getOrNull(monthIndex - 1)
    } ?: return fallback
    val day = parts[2].toIntOrNull() ?: return fallback

    return "$month $day, $year"
}

fun String?.formatApiDateTimeForDisplay(fallback: String = "-"): String {
    val date = formatApiDateForDisplay(fallback)
    if (date == fallback) return fallback

    val time = formatApiTimeForDisplay(fallback = "")
    return if (time.isBlank()) date else "$date • $time"
}

fun String?.formatApiTimeForDisplay(fallback: String = "-"): String {
    val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return fallback
    val timePart = when {
        value.contains('T') -> value.substringAfter('T')
        value.contains(' ') -> value.substringAfter(' ')
        else -> return fallback
    }.take(5)

    val hour = timePart.take(2).toIntOrNull() ?: return fallback
    val minute = timePart.takeLast(2).takeIf { it.length == 2 } ?: return fallback
    val suffix = if (hour >= 12) "PM" else "AM"
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    return "$hour12:$minute $suffix"
}
