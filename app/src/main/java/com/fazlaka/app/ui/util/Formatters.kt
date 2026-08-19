package com.fazlaka.app.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatDuration(totalSeconds: Int?): String {
    if (totalSeconds == null || totalSeconds <= 0) return "—"
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.ROOT, "%d:%02d", m, s)
}

fun formatDateTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val format = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
        val parsed = java.time.Instant.parse(iso)
        format.format(Date.from(parsed))
    } catch (_: Exception) {
        iso
    }
}

fun formatRelative(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val instant = java.time.Instant.parse(iso)
        val now = java.time.Instant.now()
        val diff = java.time.Duration.between(instant, now)
        val days = diff.toDays()
        val hours = diff.toHours()
        val minutes = diff.toMinutes()
        when {
            days > 365 -> "منذ ${days / 365} سنة"
            days > 30 -> "منذ ${days / 30} شهر"
            days >= 1 -> "منذ $days يوم"
            hours >= 1 -> "منذ $hours ساعة"
            minutes >= 1 -> "منذ $minutes دقيقة"
            else -> "الآن"
        }
    } catch (_: Exception) {
        iso
    }
}

fun localizedTitle(translations: List<com.fazlaka.app.core.model.dto.EpisodeTranslationDto>, fallback: String = ""): String =
    translations.firstOrNull { it.locale == "ar" }?.title
        ?: translations.firstOrNull()?.title
        ?: fallback

fun localizedSeasonTitle(translations: List<com.fazlaka.app.core.model.dto.SeasonTranslationDto>, fallback: String = ""): String =
    translations.firstOrNull { it.locale == "ar" }?.title
        ?: translations.firstOrNull()?.title
        ?: fallback

fun localizedArticleTitle(translations: List<com.fazlaka.app.core.model.dto.ArticleTranslationDto>, fallback: String = ""): String =
    translations.firstOrNull { it.locale == "ar" }?.title
        ?: translations.firstOrNull()?.title
        ?: fallback
