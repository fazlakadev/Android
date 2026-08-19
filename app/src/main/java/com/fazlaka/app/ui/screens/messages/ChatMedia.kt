package com.fazlaka.app.ui.screens.messages

import android.content.Context
import android.media.MediaRecorder
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientMid
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.util.formatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.math.abs
import kotlin.math.sin

// ===========================================================================
// Voice recorder
// ===========================================================================

/**
 * Hold-to-talk voice note recorder backed by [MediaRecorder] (AAC/M4A).
 * Exposes elapsed seconds + live amplitude for the waveform UI.
 */
class VoiceRecorderController(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outFile: File? = null
    private var startedAtMs: Long = 0

    private val _elapsedSec = MutableStateFlow(0)
    val elapsedSec: StateFlow<Int> = _elapsedSec

    private val _amplitude = MutableStateFlow(0f)
    /** Normalized 0..1 loudness for waveform animation. */
    val amplitude: StateFlow<Float> = _amplitude

    val isRecording: Boolean get() = recorder != null

    /** Must hold [android.Manifest.permission.RECORD_AUDIO]. */
    fun start() {
        if (recorder != null) return
        val dir = File(context.cacheDir, "chat_media").apply { mkdirs() }
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        @Suppress("DEPRECATION")
        val mr = MediaRecorder()
        mr.setAudioSource(MediaRecorder.AudioSource.MIC)
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mr.setAudioEncodingBitRate(96_000)
        mr.setAudioSamplingRate(44_100)
        mr.setOutputFile(file.absolutePath)
        mr.prepare()
        mr.start()
        recorder = mr
        outFile = file
        startedAtMs = System.currentTimeMillis()
        _elapsedSec.value = 0
    }

    /** Stops and returns the recorded file with its duration in seconds,
     *  or null when too short / failed. */
    fun stop(minDurationMs: Long = 900): Pair<File, Int>? {
        val mr = recorder ?: return null
        val file = outFile
        val durationMs = System.currentTimeMillis() - startedAtMs
        return try {
            mr.stop()
            if (file != null && durationMs >= minDurationMs) {
                file to ((durationMs / 1000L).toInt() + 1)
            } else {
                file?.delete()
                null
            }
        } catch (_: Exception) {
            file?.delete()
            null
        } finally {
            runCatching { mr.release() }
            recorder = null
            outFile = null
        }
    }

    fun cancel() {
        val mr = recorder ?: return
        try {
            mr.stop()
        } catch (_: Exception) {
            // not started recording audio yet
        }
        runCatching { mr.release() }
        outFile?.delete()
        recorder = null
        outFile = null
    }

    /** Polls recorder amplitude while active; call from a coroutine. */
    suspend fun poll() {
        while (recorder != null) {
            val amp = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)
            _amplitude.value = (amp / 32767f).coerceIn(0.08f, 1f)
            _elapsedSec.value = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
            delay(100)
        }
        _amplitude.value = 0f
    }
}

/** Live waveform + timer + cancel, shown while the mic button is held. */
@Composable
fun VoiceRecordingBar(
    elapsedSec: Int,
    amplitude: Float,
    onCancel: () -> Unit,
    onSendNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "voiceBar")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(650),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            FazlakaGradientStart.copy(alpha = 0.4f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer { alpha = pulse }
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(FazlakaGradientStart, FazlakaGradientMid),
                        ),
                    )
                    .padding(6.dp),
            )
            Waveform(amplitude = amplitude, modifier = Modifier.weight(1f))
            Text(
                text = formatDuration(elapsedSec),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "إلغاء",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            IconButton(onClick = onSendNow) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "إرسال",
                    tint = FazlakaCyan,
                )
            }
        }
    }
}

@Composable
private fun Waveform(amplitude: Float, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "wavePhase",
    )
    val bars = 22
    Canvas(modifier = modifier.height(28.dp)) {
        val barWidth = size.width / (bars * 1.6f)
        val center = size.height / 2
        for (i in 0 until bars) {
            val wobble = 0.45f + 0.55f * abs(sin(phase + i * 0.7f))
            val h = (size.height * amplitude * wobble).coerceAtLeast(3f)
            drawRoundRect(
                color = if (i % 3 == 0) FazlakaCyan else FazlakaGradientStart,
                topLeft = androidx.compose.ui.geometry.Offset(
                    i * barWidth * 1.6f,
                    center - h / 2,
                ),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2),
            )
        }
    }
}

// ===========================================================================
// Shared audio player for voice bubbles
// ===========================================================================

/**
 * One ExoPlayer shared by every audio bubble in the conversation;
 * playing a message pauses the previous one automatically.
 */
class ChatAudioPlayer(context: Context) {
    val currentId = mutableStateOf<String?>(null)
    val isPlaying = mutableStateOf(false)
    val progress = mutableFloatStateOf(0f)
    val positionMs = mutableIntStateOf(0)
    val durationMs = mutableIntStateOf(0)

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                this@ChatAudioPlayer.isPlaying.value = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    this@ChatAudioPlayer.isPlaying.value = false
                    progress.floatValue = 0f
                    positionMs.intValue = 0
                    currentId.value = null
                }
                if (playbackState == Player.STATE_READY) {
                    durationMs.intValue = this@apply.duration.coerceAtLeast(0L).toInt()
                }
            }
        })
    }

    private var tickerRunning = false

    fun toggle(messageId: String, url: String) {
        if (currentId.value == messageId) {
            if (player.isPlaying) player.pause() else player.play()
        } else {
            currentId.value = messageId
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.playWhenReady = true
            player.play()
        }
        if (!tickerRunning) startTicker()
    }

    private fun startTicker() {
        tickerRunning = true
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (currentId.value != null) {
                    positionMs.intValue = player.currentPosition.coerceAtLeast(0L).toInt()
                    val dur = player.duration
                    if (dur > 0) {
                        durationMs.intValue = dur.toInt()
                        progress.floatValue =
                            (player.currentPosition.toFloat() / dur).coerceIn(0f, 1f)
                    }
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, 200)
                } else {
                    tickerRunning = false
                }
            }
        }
        handler.post(runnable)
    }

    fun release() {
        player.release()
    }
}

@Composable
fun rememberChatAudioPlayer(): ChatAudioPlayer {
    val context = LocalContext.current
    val controller = remember { ChatAudioPlayer(context) }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }
    return controller
}

/** Voice-note bubble content: play/pause + animated progress + duration. */
@Composable
fun AudioBubbleContent(
    messageId: String,
    url: String,
    fallbackDurationSec: Int?,
    isMine: Boolean,
    player: ChatAudioPlayer,
    modifier: Modifier = Modifier,
) {
    val active = player.currentId.value == messageId
    val playing = active && player.isPlaying.value
    val progress = if (active) player.progress.floatValue else 0f
    val posSec = if (active) (player.positionMs.intValue / 1000) else 0
    val totalSec = if (active && player.durationMs.intValue > 0) {
        player.durationMs.intValue / 1000
    } else {
        fallbackDurationSec ?: 0
    }

    Row(
        modifier = modifier
            .widthIn(max = 240.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { player.toggle(messageId, url) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isMine) Color.White.copy(alpha = 0.22f)
                    else FazlakaGradientStart.copy(alpha = 0.16f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (playing) "إيقاف" else "تشغيل",
                tint = if (isMine) Color.White else FazlakaGradientStart,
                modifier = Modifier
                    .size(22.dp)
                    .padding(2.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isMine) Color.White.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isMine) Color.White else FazlakaGradientStart,
                        ),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${formatDuration(if (active) posSec else 0)} / ${formatDuration(totalSec)}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) Color.White.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ===========================================================================
// Fullscreen video player
// ===========================================================================

@Composable
fun VideoPlayerDialog(url: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() },
                    )
                },
                contentAlignment = Alignment.Center,
        ) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "إغلاق", tint = Color.White)
            }
        }
    }
}

// ===========================================================================
// Zoomable image viewer
// ===========================================================================

@Composable
fun ZoomableImageDialog(url: String, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() },
                        onDoubleTap = {
                            scale = if (scale > 1f) 1f else 2.5f
                            if (scale == 1f) {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            coil.compose.AsyncImage(
                model = url,
                contentDescription = "صورة",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    },
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "إغلاق", tint = Color.White)
            }
            Text(
                text = stringResource(com.fazlaka.app.R.string.conv_close_hint),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
            )
        }
    }
}
