package com.fazlaka.app.ui.screens.search

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.analytics.AnalyticsTracker
import com.fazlaka.app.core.model.dto.SearchResultDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.ListSkeleton
import com.fazlaka.app.ui.components.PosterImage
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.accessibility.accessibleButton
import com.fazlaka.app.ui.accessibility.accessibleImage
import com.fazlaka.app.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onNavigate: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recent by viewModel.recentSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    val analyticsTracker = remember { AnalyticsTracker() }

    LaunchedEffect(state.results) {
        val result = state.results
        if (result is com.fazlaka.app.core.network.ApiResult.Success) {
            analyticsTracker.logSearch(
                query = result.data.query,
                resultCount = result.data.results.size,
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HeroSection(
            title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.search_hero_title),
            subtitle = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.search_hero_sub),
            badge = "🔍 اكتشف المزيد",
            accent = HeroAccents.Search,
            minHeight = 140.dp,
            fullscreenTop = true,
        )
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent,
                            ),
                        ),
                    )
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(com.fazlaka.app.R.string.accessibility_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp),
                )
                androidx.compose.material3.OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = {
                        Text(
                            text = "ابحث عن حلقات، مواسيم، مقالات...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { viewModel.submitSearch() },
                    ),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedLabelColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
                if (state.query.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.onQueryChange("") },
                        modifier = Modifier.accessibleButton(
                            label = "مسح",
                            hint = stringResource(com.fazlaka.app.R.string.accessibility_clear_search),
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(com.fazlaka.app.R.string.accessibility_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }
            }
        }

        if (state.submitted || state.query.isNotBlank()) {
            FilterBar(state, viewModel)
        }

        when {
            !state.submitted && state.query.isBlank() -> {
                RecentSearches(recent, viewModel) { q -> viewModel.submitSearch(q) }
            }
            !state.submitted -> {
                Suggestions(state, onNavigate, viewModel)
            }
            else -> {
                ApiResultContent(
                    result = state.results,
                    onRetry = { viewModel.submitSearch() },
                    emptyTitle = "لا توجد نتائج",
                    loadingContent = { ListSkeleton() },
                ) { response ->
                    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                        item {
                            Text(
                                text = "نتائج البحث عن: \"${response.query}\"",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        itemsIndexed(
                            response.results,
                            key = { _, it -> it.id },
                            contentType = { _, _ -> "searchResult" },
                        ) { index, result ->
                            SearchResultRow(
                                result = result,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(400, delayMillis = (index % 8) * 50, easing = LinearOutSlowInEasing),
                                ),
                                onClick = {
                                    when (result.type) {
                                        "episode" -> onNavigate(Routes.episode(result.slug.ifBlank { result.id }))
                                        "season" -> onNavigate(Routes.season(result.slug.ifBlank { result.id }))
                                        "article" -> onNavigate(Routes.article(result.slug.ifBlank { result.id }))
                                        "playlist" -> onNavigate(Routes.playlist(result.slug.ifBlank { result.id }))
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterBar(state: com.fazlaka.app.ui.viewmodel.SearchUiState, viewModel: SearchViewModel) {
    val types = listOf(
        "episode" to "حلقات",
        "season" to "مواسم",
        "article" to "مقالات",
        "playlist" to "قوائم",
    )
    val categories = listOf(
        "توعية" to "توعية",
        "تعليم" to "تعليم",
        "تثقيف" to "تثقيف",
        "إرشاد" to "إرشاد",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    label = "الكل",
                    selected = state.activeType == null,
                    onClick = { viewModel.onTypeFilter(null) },
                )
            }
            items(types.size) { index ->
                FilterChip(
                    label = types[index].second,
                    selected = state.activeType == types[index].first,
                    onClick = { viewModel.onTypeFilter(types[index].first) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    label = "كل التصنيفات",
                    selected = state.activeCategory == null,
                    onClick = { viewModel.onCategoryFilter(null) },
                )
            }
            items(categories.size) { index ->
                FilterChip(
                    label = categories[index].second,
                    selected = state.activeCategory == categories[index].first,
                    onClick = { viewModel.onCategoryFilter(categories[index].first) },
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.semantics {
            role = Role.Button
            contentDescription = if (selected) "مُحدّث: $label" else label
        },
        shape = RoundedCornerShape(50),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun RecentSearches(
    recent: List<com.fazlaka.app.core.database.SearchHistoryEntity>,
    viewModel: SearchViewModel,
    onSearch: (String) -> Unit,
) {
    if (recent.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("ابحث عن ما تريد مشاهدته", style = MaterialTheme.typography.titleMedium)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("عمليات البحث الأخيرة", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { viewModel.clearSearches() }) { Text("مسح الكل") }
            }
        }
        items(recent, key = { it.query }, contentType = { "searchHistory" }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearch(item.query) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.History, contentDescription = stringResource(com.fazlaka.app.R.string.accessibility_search_history_item), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Text(item.query, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { viewModel.removeSearch(item.query) },
                    modifier = Modifier.accessibleButton(label = "حذف"),
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(com.fazlaka.app.R.string.accessibility_remove_search_item), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Suggestions(
    state: com.fazlaka.app.ui.viewmodel.SearchUiState,
    onNavigate: (String) -> Unit,
    viewModel: SearchViewModel,
) {
    val results = (state.suggestions as? ApiResult.Success)?.data?.results ?: emptyList()
    if (results.isEmpty()) {
        Text(
            text = "اضغط بحث للعرض الكامل",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
        itemsIndexed(results, key = { _, it -> it.title }, contentType = { _, _ -> "searchResult" }) { index, s ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .animateItem(
                        fadeInSpec = tween(350, delayMillis = (index % 8) * 40, easing = LinearOutSlowInEasing),
                    ),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (s.type) {
                                "episode" -> onNavigate(Routes.episode(s.slug))
                                "season" -> onNavigate(Routes.season(s.slug))
                                "article" -> onNavigate(Routes.article(s.slug))
                                else -> viewModel.submitSearch(s.title)
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp).accessibleImage("نتيجة بحث"))
                    Text(s.title, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(result: SearchResultDto, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PosterImage(
                url = result.coverImage,
                contentDescription = result.title,
                modifier = Modifier
                    .width(90.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(
                        result.type,
                        result.seasonTitle,
                        result.duration?.let { com.fazlaka.app.ui.util.formatDuration(it) },
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
