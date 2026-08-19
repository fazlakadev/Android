package com.fazlaka.app.ui.screens.messages

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fazlaka.app.core.model.dto.MessageDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.Avatar
import com.fazlaka.app.ui.components.LoadingIndicator
import com.fazlaka.app.ui.components.currentUserIdFlow
import com.fazlaka.app.ui.util.formatDuration
import com.fazlaka.app.ui.util.formatRelative
import com.fazlaka.app.ui.viewmodel.ConversationViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SenderCyan = Color(0xFF22D3EE)

/** Localized day label for date separators: today / yesterday / full date. */
private fun dayLabel(iso: String?, todayLabel: String, yesterdayLabel: String): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val zone = ZoneId.systemDefault()
        val instant = Instant.parse(iso)
        val day = LocalDate.ofInstant(instant, zone)
        val today = LocalDate.now(zone)
        when {
            day == today -> todayLabel
            day == today.minusDays(1) -> yesterdayLabel
            else -> DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.forLanguageTag("ar"))
                .withZone(zone)
                .format(instant)
        }
    } catch (_: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    onBack: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val audioPlayer = rememberChatAudioPlayer()
    // Localized labels used across the chat UI
    val chatPrivate = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_private)
    val chatMembersFmt = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_members_fmt)
    val inputHint = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_input_hint)
    val todayLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_today)
    val yesterdayLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_yesterday)
    val toLastLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_to_last)
    val recordHint = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_record_hint)
    val sendLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_send)
    val attachLabel = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_attach)
    val attachPhoto = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_attach_photo)
    val attachVideo = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_attach_video)
    val attachGalleryImage = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_attach_gallery_image)
    val attachGalleryVideo = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_attach_gallery_video)
    val attachAudioFile = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.chat_attach_audio)
    val listState = rememberLazyListState()
    val messages = (state.detail as? ApiResult.Success)?.data?.messages ?: emptyList()
    var previousCount by remember { mutableIntStateOf(0) }

    var previewImage by remember { mutableStateOf<String?>(null) }
    var previewVideo by remember { mutableStateOf<String?>(null) }

    // ---------------- Voice recording ----------------
    val recorder = remember { VoiceRecorderController(context) }
    var recording by remember { mutableStateOf(false) }
    val elapsedSec by recorder.elapsedSec.collectAsStateWithLifecycle(initialValue = 0)
    val amplitude by recorder.amplitude.collectAsStateWithLifecycle(initialValue = 0f)

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) recorder.start().also { recording = true }
    }

    fun stopAndSendRecording() {
        val result = recorder.stop()
        recording = false
        result?.let { (file, duration) ->
            viewModel.sendMedia(Uri.fromFile(file), "audio", duration)
        }
    }

    LaunchedEffect(recording) {
        if (recording) recorder.poll()
    }

    // ---------------- Pickers ----------------
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMedia(it, "image") }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMedia(it, "video") }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendMedia(it, "audio") }
    }

    fun newCaptureFile(ext: String): File =
        File(File(context.cacheDir, "chat_media").apply { mkdirs() }, "cap_${System.currentTimeMillis()}.$ext")

    fun captureUri(file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    var pendingPhoto by remember { mutableStateOf<File?>(null) }
    var pendingVideo by remember { mutableStateOf<File?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pendingPhoto
        pendingPhoto = null
        if (ok && f != null && f.exists()) viewModel.sendMedia(Uri.fromFile(f), "image")
    }
    val takeVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { ok ->
        val f = pendingVideo
        pendingVideo = null
        if (ok && f != null && f.exists()) viewModel.sendMedia(Uri.fromFile(f), "video")
    }

    // ---------------- Pagination: load older when near the top ----------------
    val nearTop by remember {
        derivedStateOf {
            val first = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            first <= 3
        }
    }
    LaunchedEffect(nearTop, messages.size) {
        if (nearTop && messages.isNotEmpty()) viewModel.loadOlder()
    }

    // ---------------- Auto scroll on new messages ----------------
    LaunchedEffect(state.detail) {
        (state.detail as? ApiResult.Success)?.let {
            if (it.data.messages.isNotEmpty() && previousCount == 0) {
                listState.animateScrollToItem(it.data.messages.lastIndex)
            }
        }
    }
    LaunchedEffect(messages.size) {
        if (messages.size > previousCount && messages.isNotEmpty()) {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            if (previousCount == 0 || lastVisible >= previousCount - 1) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
        previousCount = messages.size
    }

    val detailData = (state.detail as? ApiResult.Success)?.data?.conversation
    val isGroup = detailData?.kind == "group"
    val memberCount = detailData?.group?.members?.size ?: 0
    val otherAvatar = detailData?.other?.avatarUrl ?: detailData?.group?.avatarUrl

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    val title = detailData?.other?.name ?: detailData?.group?.name ?: stringResource(com.fazlaka.app.R.string.conv_title)
                    val subtitle = if (isGroup) chatMembersFmt.format(memberCount) else chatPrivate
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Avatar(url = otherAvatar, name = title, size = 38)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(1.5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E)),
                                )
                            }
                        }
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveDraft(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                if (recording) {
                    VoiceRecordingBar(
                        elapsedSec = elapsedSec,
                        amplitude = amplitude,
                        onCancel = { recorder.cancel(); recording = false },
                        onSendNow = { stopAndSendRecording() },
                    )
                }
                MessageInput(
                    text = state.messageText,
                    onTextChange = viewModel::onTextChange,
                    onSend = { viewModel.send() },
                    sending = state.sending,
                    recording = recording,
                    onPickImage = { imagePicker.launch("image/*") },
                    onPickVideo = { videoPicker.launch("video/*") },
                    onPickAudio = { audioPicker.launch("audio/*") },
                    onTakePhoto = {
                        pendingPhoto = newCaptureFile("jpg").also {
                            takePicture.launch(captureUri(it))
                        }
                    },
                    onTakeVideo = {
                        pendingVideo = newCaptureFile("mp4").also {
                            takeVideo.launch(captureUri(it))
                        }
                    },
                    onMicDown = {
                        micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onMicUp = { if (recording) stopAndSendRecording() },
                    inputHint = inputHint,
                    attachLabel = attachLabel,
                    attachPhoto = attachPhoto,
                    attachVideo = attachVideo,
                    attachGalleryImage = attachGalleryImage,
                    attachGalleryVideo = attachGalleryVideo,
                    attachAudioFile = attachAudioFile,
                    recordHint = recordHint,
                    sendLabel = sendLabel,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.045f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        ) {
            when (val detail = state.detail) {
                null -> LoadingIndicator()
                is ApiResult.Failure -> Text(
                    stringResource(com.fazlaka.app.R.string.conv_error),
                    modifier = Modifier.padding(16.dp),
                )
                is ApiResult.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        if (state.loadingOlder) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(6.dp),
                                    contentAlignment = Alignment.Center,
                                ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                            }
                        }
                        itemsIndexed(detail.data.messages, key = { _, it -> it.id }, contentType = { _, _ -> "message" }) { index, message ->
                            val prevDay = remember(detail.data.messages.getOrNull(index - 1)?.createdAt) {
                                dayLabel(detail.data.messages.getOrNull(index - 1)?.createdAt, todayLabel, yesterdayLabel)
                            }
                            val currentDay = remember(message.createdAt) {
                                dayLabel(message.createdAt, todayLabel, yesterdayLabel)
                            }
                            if (currentDay.isNotBlank() && currentDay != prevDay) {
                                DateSeparator(currentDay)
                            }
                            MessageBubble(
                                message = message,
                                showAvatar = isGroup,
                                audioPlayer = audioPlayer,
                                onOpenImage = { previewImage = it },
                                onOpenVideo = { previewVideo = it },
                            )
                        }
                    }

                    // Scroll-to-bottom pill when the user is scrolled up
                    val awayFromBottom by remember {
                        derivedStateOf { listState.canScrollForward }
                    }
                    val scope = rememberCoroutineScope()
                    androidx.compose.animation.AnimatedVisibility(
                        visible = awayFromBottom && messages.isNotEmpty(),
                        enter = androidx.compose.animation.fadeIn() +
                            androidx.compose.animation.scaleIn(initialScale = 0.6f),
                        exit = androidx.compose.animation.fadeOut() +
                            androidx.compose.animation.scaleOut(targetScale = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                    ) {
                        Surface(
                            onClick = {
                                if (messages.isNotEmpty()) {
                                    scope.launch { listState.animateScrollToItem(messages.lastIndex) }
                                }
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(42.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = toLastLabel,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    previewImage?.let { url ->
        ZoomableImageDialog(url = url, onDismiss = { previewImage = null })
    }
    previewVideo?.let { url ->
        VideoPlayerDialog(url = url, onDismiss = { previewVideo = null })
    }
}

@Composable
private fun DateSeparator(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageDto,
    showAvatar: Boolean,
    audioPlayer: ChatAudioPlayer,
    onOpenImage: (String) -> Unit,
    onOpenVideo: (String) -> Unit,
) {
    val currentUserId by currentUserIdFlow()
    val isMine = currentUserId != null && message.senderId == currentUserId
    val isMedia = message.type != "text"
    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isMine) 20.dp else 6.dp,
        bottomEnd = if (isMine) 6.dp else 20.dp,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMine && showAvatar) {
            Avatar(
                url = message.sender?.avatarUrl,
                name = message.sender?.name ?: "؟",
                size = 30,
            )
            Spacer(Modifier.size(6.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = 305.dp)
                .clip(bubbleShape)
                .background(
                    if (isMine) {
                        Brush.linearGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    },
                ),
        ) {
            Column(modifier = Modifier.padding(if (isMedia) 0.dp else 12.dp, 8.dp)) {
                if (!isMine && message.sender != null) {
                    Text(
                        text = message.sender?.name ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = SenderCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
                if (isMedia) {
                    MediaContent(
                        message = message,
                        isMine = isMine,
                        audioPlayer = audioPlayer,
                        onOpenImage = onOpenImage,
                        onOpenVideo = onOpenVideo,
                    )
                    if (message.body.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = message.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                } else {
                    Text(
                        text = message.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                }
                val timeText = remember(message.createdAt) { formatRelative(message.createdAt) }
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isMine) Color.White.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End).padding(horizontal = 12.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun MediaContent(
    message: MessageDto,
    isMine: Boolean,
    audioPlayer: ChatAudioPlayer,
    onOpenImage: (String) -> Unit,
    onOpenVideo: (String) -> Unit,
) {
    val url = message.attachmentUrl.orEmpty()
    when (message.type) {
        "image" -> {
            AsyncImage(
                model = url,
                contentDescription = "صورة",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clickable { onOpenImage(url) },
            )
        }
        "video" -> {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFF111827))
                    .clickable { onOpenVideo(url) },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "فيديو",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                )
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = "تشغيل",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp),
                )
                message.durationSec?.let {
                    Text(
                        text = formatDuration(it),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
        "audio" -> {
            AudioBubbleContent(
                messageId = message.id,
                url = url,
                fallbackDurationSec = message.durationSec,
                isMine = isMine,
                player = audioPlayer,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        else -> {
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

/** Unified pill input: attach + text + mic/send in a single elevated bar. */
@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    sending: Boolean,
    recording: Boolean,
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit,
    onPickAudio: () -> Unit,
    onTakePhoto: () -> Unit,
    onTakeVideo: () -> Unit,
    onMicDown: () -> Unit,
    onMicUp: () -> Unit,
    inputHint: String,
    attachLabel: String,
    attachPhoto: String,
    attachVideo: String,
    attachGalleryImage: String,
    attachGalleryVideo: String,
    attachAudioFile: String,
    recordHint: String,
    sendLabel: String,
) {
    var attachMenu by remember { mutableStateOf(false) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(end = 5.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Box {
                IconButton(
                    onClick = { attachMenu = true },
                    enabled = !sending && !recording,
                    modifier = Modifier.size(46.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = attachLabel,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                DropdownMenu(
                    expanded = attachMenu,
                    onDismissRequest = { attachMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(attachPhoto) },
                        leadingIcon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
                        onClick = { attachMenu = false; onTakePhoto() },
                    )
                    DropdownMenuItem(
                        text = { Text(attachVideo) },
                        leadingIcon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
                        onClick = { attachMenu = false; onTakeVideo() },
                    )
                    DropdownMenuItem(
                        text = { Text(attachGalleryImage) },
                        leadingIcon = { Icon(Icons.Filled.Image, contentDescription = null) },
                        onClick = { attachMenu = false; onPickImage() },
                    )
                    DropdownMenuItem(
                        text = { Text(attachGalleryVideo) },
                        leadingIcon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
                        onClick = { attachMenu = false; onPickVideo() },
                    )
                    DropdownMenuItem(
                        text = { Text(attachAudioFile) },
                        leadingIcon = { Icon(Icons.Filled.Audiotrack, contentDescription = null) },
                        onClick = { attachMenu = false; onPickAudio() },
                    )
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp, max = 130.dp),
                placeholder = {
                    Text(
                        inputHint,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    )
                },
                maxLines = 4,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )

            // Mic (hold to record) when empty, gradient send otherwise
            if (text.isBlank()) {
                val micInteraction = remember { MutableInteractionSource() }
                val micPressed by micInteraction.collectIsPressedAsState()
                LaunchedEffect(micPressed) {
                    if (micPressed) onMicDown()
                    else onMicUp()
                }
                IconButton(
                    interactionSource = micInteraction,
                    onClick = { /* handled by press/release */ },
                    enabled = !sending && !recording,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0891B2), Color(0xFF22D3EE)),
                            ),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = recordHint,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = !sending,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                            ),
                        ),
                ) {
                    if (sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(19.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = sendLabel,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
