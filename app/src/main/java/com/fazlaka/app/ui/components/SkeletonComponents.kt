package com.fazlaka.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer(),
    )
}

@Composable
fun SkeletonText(
    width: Dp,
    height: Dp = 14.dp,
    modifier: Modifier = Modifier,
) {
    SkeletonBox(
        modifier = modifier
            .width(width)
            .height(height),
        shape = RoundedCornerShape(4.dp),
    )
}

@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    SkeletonBox(
        modifier = modifier.size(size),
        shape = CircleShape,
    )
}

/** Fades children in with a per-index stagger so skeletons don't pop in all at once. */
@Composable
private fun StaggeredItem(
    index: Int,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(50L * index)
        alpha.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
    }
    Box(modifier = Modifier.graphicsLayer { this.alpha = alpha.value }) { content() }
}

/** A list of shimmering rows: leading thumbnail + two text lines. */
@Composable
fun ListSkeleton(
    rowCount: Int = 8,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(rowCount) { index ->
            StaggeredItem(index = index) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(90.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(8.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonText(width = if (index % 2 == 0) 160.dp else 120.dp, height = 16.dp)
                        Spacer(Modifier.height(8.dp))
                        SkeletonText(width = 90.dp, height = 12.dp)
                    }
                }
            }
        }
    }
}

/** A grid of shimmering posters (seasons). Columns share the available width. */
@Composable
fun GridSkeleton(
    columns: Int = 3,
    rowCount: Int = 6,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(rowCount) { row ->
            StaggeredItem(index = row) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(columns) {
                        Column(modifier = Modifier.weight(1f)) {
                            SkeletonBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.7f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            SkeletonText(width = 90.dp, height = 12.dp)
                        }
                    }
                }
            }
        }
    }
}

/** Skeleton for the home screen: header, banner with chips, poster rows, episode rows. */
@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.statusBarsPadding()) {
        item {
            StaggeredItem(0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonText(width = 150.dp, height = 26.dp)
                        Spacer(Modifier.height(10.dp))
                        SkeletonText(width = 200.dp, height = 13.dp)
                    }
                    SkeletonCircle(size = 46.dp)
                }
            }
        }
        item {
            StaggeredItem(1) {
                Column {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .aspectRatio(1.9f),
                        shape = RoundedCornerShape(24.dp),
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(3) {
                            SkeletonBox(
                                modifier = Modifier.height(28.dp).width(72.dp),
                                shape = RoundedCornerShape(14.dp),
                            )
                        }
                    }
                }
            }
        }
        item {
            StaggeredItem(2) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonText(width = 130.dp, height = 20.dp)
                    SkeletonText(width = 40.dp, height = 14.dp)
                }
            }
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(4) { index ->
                    StaggeredItem(index = index) {
                        Column {
                            SkeletonBox(
                                modifier = Modifier
                                    .width(120.dp)
                                    .aspectRatio(0.7f),
                                shape = RoundedCornerShape(14.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            SkeletonText(width = 90.dp, height = 11.dp)
                        }
                    }
                }
            }
        }
        item {
            StaggeredItem(3) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonText(width = 130.dp, height = 20.dp)
                    SkeletonText(width = 40.dp, height = 14.dp)
                }
            }
        }
        items(4) { index ->
            StaggeredItem(index = index) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(1.7f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonText(width = 160.dp, height = 15.dp)
                        Spacer(Modifier.height(8.dp))
                        SkeletonText(width = 110.dp, height = 11.dp)
                        Spacer(Modifier.height(6.dp))
                        SkeletonText(width = 70.dp, height = 11.dp)
                    }
                }
            }
        }
    }
}

/** Skeleton for profile: avatar + name + menu rows. */
@Composable
fun ProfileSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.statusBarsPadding()) {
        item {
            StaggeredItem(0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SkeletonCircle(size = 104.dp)
                    Spacer(Modifier.height(14.dp))
                    SkeletonText(width = 140.dp, height = 22.dp)
                    Spacer(Modifier.height(8.dp))
                    SkeletonText(width = 100.dp, height = 14.dp)
                }
            }
        }
        items(6) { index ->
            StaggeredItem(index = index) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SkeletonBox(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(11.dp),
                    )
                    SkeletonText(width = 140.dp, height = 16.dp)
                }
            }
        }
    }
}

/** Skeleton for a content detail screen: hero + title + description. */
@Composable
fun DetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f),
            shape = RoundedCornerShape(0.dp),
        )
        Column(modifier = Modifier.padding(16.dp)) {
            StaggeredItem(0) { SkeletonText(width = 220.dp, height = 24.dp) }
            Spacer(Modifier.height(12.dp))
            StaggeredItem(1) { SkeletonText(width = 150.dp, height = 14.dp) }
            Spacer(Modifier.height(16.dp))
            StaggeredItem(2) { SkeletonText(width = 320.dp, height = 12.dp) }
            Spacer(Modifier.height(8.dp))
            StaggeredItem(3) { SkeletonText(width = 280.dp, height = 12.dp) }
            Spacer(Modifier.height(8.dp))
            StaggeredItem(4) { SkeletonText(width = 240.dp, height = 12.dp) }
        }
    }
}
