package com.fazlaka.app.ui.util

import android.content.Context
import android.content.Intent

object ShareUtils {

    private const val BASE_URL = "https://fazlaka-platform.vercel.app"

    fun shareEpisode(context: Context, slug: String, title: String) {
        shareLink(context, "$BASE_URL/episode/$slug", title)
    }

    fun shareSeason(context: Context, slug: String, title: String) {
        shareLink(context, "$BASE_URL/season/$slug", title)
    }

    fun shareArticle(context: Context, slug: String, title: String) {
        shareLink(context, "$BASE_URL/article/$slug", title)
    }

    fun sharePlaylist(context: Context, slug: String, title: String) {
        shareLink(context, "$BASE_URL/playlist/$slug", title)
    }

    fun shareProfile(context: Context, username: String, displayName: String) {
        shareLink(context, "$BASE_URL/profile/$username", displayName)
    }

    private fun shareLink(context: Context, url: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$title\n$url")
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة"))
    }
}
