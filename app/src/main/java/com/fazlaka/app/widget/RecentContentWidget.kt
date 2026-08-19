package com.fazlaka.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.unit.ColorProvider
import com.fazlaka.app.MainActivity
import com.fazlaka.app.R
import org.json.JSONArray
import org.json.JSONObject

data class WidgetEpisode(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val seasonTitle: String = "",
)

class RecentContentWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val episodes = loadEpisodesFromStorage(context)

        provideContent {
            GlanceTheme {
                WidgetContent(context, episodes)
            }
        }
    }

    companion object {
        /**
         * Reads the episodes JSON persisted by [RecentContentWidgetReceiver].
         * Falls back to an empty list on any parse error.
         */
        internal fun loadEpisodesFromStorage(context: Context): List<WidgetEpisode> {
            return try {
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                val json = prefs.getString("episodes", null) ?: return emptyList()
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    WidgetEpisode(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        subtitle = obj.optString("subtitle"),
                        seasonTitle = obj.optString("seasonTitle"),
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        internal fun saveEpisodes(context: Context, episodes: List<WidgetEpisode>) {
            val arr = JSONArray()
            episodes.forEach { ep ->
                arr.put(JSONObject().apply {
                    put("id", ep.id)
                    put("title", ep.title)
                    put("subtitle", ep.subtitle)
                    put("seasonTitle", ep.seasonTitle)
                })
            }
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("episodes", arr.toString()).apply()
        }
    }
}

@Composable
private fun WidgetContent(context: Context, episodes: List<WidgetEpisode>) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(ColorProvider(R.color.widget_background))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxWidth(),
            ) {
                Text(
                    text = "\u0641\u0630\u0644\u0643\u0629",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(R.color.widget_text_primary),
                    ),
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (episodes.isEmpty()) {
                Text(
                    text = "\u0644\u0627 \u062a\u0648\u062c\u062f \u0645\u062d\u062a\u0648\u0649",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = ColorProvider(R.color.widget_text_secondary),
                    ),
                )
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(episodes) { ep ->
                        EpisodeRow(context, ep)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(context: Context, episode: WidgetEpisode) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(12.dp)
            .background(ColorProvider(R.color.widget_row_background))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = episode.title.ifBlank { "\u0645\u0642\u0637\u0639\u0629" },
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(R.color.widget_text_primary),
                ),
                maxLines = 1,
            )
            if (episode.seasonTitle.isNotBlank()) {
                Text(
                    text = episode.seasonTitle,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = ColorProvider(R.color.widget_text_secondary),
                    ),
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = "\u25B6",
            style = TextStyle(
                fontSize = 16.sp,
                color = ColorProvider(R.color.widget_accent),
            ),
        )
    }
}

class RecentContentWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecentContentWidget()
}
