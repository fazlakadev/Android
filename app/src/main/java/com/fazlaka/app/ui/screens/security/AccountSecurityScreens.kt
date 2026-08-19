@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fazlaka.app.ui.screens.security

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.BuildConfig
import com.fazlaka.app.core.model.dto.AuthEventDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.AuthField
import com.fazlaka.app.ui.components.EmptyState
import com.fazlaka.app.ui.components.ListSkeleton
import com.fazlaka.app.ui.components.GlassCard
import com.fazlaka.app.ui.components.HeroAccent
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.OtpInputField
import com.fazlaka.app.ui.components.glowShadow
import com.fazlaka.app.ui.theme.FazlakaCyan
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.theme.FazlakaSuccess
import com.fazlaka.app.ui.util.formatDateTime
import com.fazlaka.app.ui.viewmodel.ActivityLogUiState
import com.fazlaka.app.ui.viewmodel.ActivityLogViewModel
import com.fazlaka.app.ui.viewmodel.ChangeEmailUiState
import com.fazlaka.app.ui.viewmodel.ChangeEmailViewModel
import com.fazlaka.app.ui.viewmodel.LinkedAccountsUiState
import com.fazlaka.app.ui.viewmodel.LinkedAccountsViewModel
import com.fazlaka.app.ui.viewmodel.SecondaryEmailsUiState
import com.fazlaka.app.ui.viewmodel.SecondaryEmailsViewModel

// ===========================================================================
// Shared helpers
// ===========================================================================

/** Maps the backend platform tag to a friendly Arabic label + emoji + color. */
data class PlatformBadge(val label: String, val color: Color, val emoji: String)

fun platformBadge(platform: String?, os: String? = null): PlatformBadge {
    val osl = os?.lowercase().orEmpty()
    return when (platform?.uppercase()) {
        "MOBILE" -> when {
            osl.contains("android") -> PlatformBadge("أندرويد", FazlakaCyan, "🤖")
            osl.contains("ios") || osl.contains("iphone") ->
                PlatformBadge("آيفون", Color(0xFF94A3B8), "📱")
            else -> PlatformBadge("موبايل", FazlakaCyan, "📱")
        }
        "DESKTOP" -> PlatformBadge("كمبيوتر", FazlakaGradientStart, "💻")
        "WEB" -> PlatformBadge("ويب", FazlakaSuccess, "🌐")
        else -> PlatformBadge("غير معروف", Color(0xFF94A3B8), "❔")
    }
}

@Composable
fun PlatformBadgeChip(platform: String?, os: String? = null, modifier: Modifier = Modifier) {
    val badge = platformBadge(platform, os)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = badge.color.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(badge.emoji, style = MaterialTheme.typography.labelSmall)
            Text(
                text = badge.label,
                style = MaterialTheme.typography.labelSmall,
                color = badge.color,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun eventLabel(eventType: String): String = when (eventType) {
    "login" -> "تسجيل دخول"
    "logout" -> "تسجيل خروج"
    "register" -> "إنشاء حساب"
    "refresh" -> "تجديد جلسة"
    "failed_login" -> "محاولة دخول فاشلة"
    "lockout" -> "قفل مؤقت للحساب"
    "two_factor" -> "التحقق بخطوتين"
    "password_reset_request" -> "طلب استعادة كلمة السر"
    "password_reset" -> "استعادة كلمة السر"
    "password_changed" -> "تغيير كلمة السر"
    "email_verified" -> "توثيق البريد"
    "email_changed" -> "تغيير البريد الأساسي"
    "verify_email_resend" -> "إعادة إرسال التوثيق"
    "terms_accepted" -> "قبول الشروط"
    "session_revoked" -> "إنهاء جلسة"
    "message_sent" -> "إرسال رسالة"
    "media_upload" -> "رفع وسائط"
    "conversation_created" -> "إنشاء محادثة"
    "group_created" -> "إنشاء مجموعة"
    "group_member_added" -> "إضافة عضو لمجموعة"
    "group_member_removed" -> "إزالة عضو من مجموعة"
    "group_left" -> "مغادرة مجموعة"
    "friend_request_sent" -> "إرسال طلب صداقة"
    "friend_request_accepted" -> "قبول طلب صداقة"
    "friend_request_rejected" -> "رفض طلب صداقة"
    "friend_removed" -> "حذف صديق"
    "user_blocked" -> "حظر مستخدم"
    "user_unblocked" -> "فك حظر"
    "profile_updated" -> "تحديث الملف الشخصي"
    "avatar_uploaded" -> "تغيير الصورة الشخصية"
    "banner_uploaded" -> "تغيير الغلاف"
    "preferences_updated" -> "تحديث التفضيلات"
    "geolocation_updated" -> "تحديث الموقع الجغرافي"
    "secondary_email_added" -> "إضافة بريد ثانوي"
    "secondary_email_verified" -> "توثيق بريد ثانوي"
    "secondary_email_removed" -> "حذف بريد ثانوي"
    "primary_email_changed" -> "تغيير البريد الأساسي"
    "oauth_link_intent" -> "محاولة ربط حساب"
    "phone_verified" -> "توثيق الهاتف"
    "phone_removed" -> "فك ربط الهاتف"
    "phone_login_request" -> "طلب دخول بالهاتف"
    "google", "github", "facebook" -> "دخول عبر ${eventType.replaceFirstChar { it.uppercase() }}"
    else -> eventType
}

private fun eventIcon(eventType: String): ImageVector = when {
    eventType.contains("login") || eventType in listOf("google", "github", "facebook") ->
        Icons.Filled.Login
    eventType == "logout" -> Icons.Filled.Logout
    eventType.contains("password") -> Icons.Filled.Password
    eventType.contains("email") -> Icons.Filled.Email
    eventType.contains("friend") || eventType.contains("block") -> Icons.Filled.Person
    eventType.contains("message") || eventType.contains("group") || eventType.contains("conversation") ->
        Icons.Filled.Person
    eventType.contains("media") || eventType.contains("upload") -> Icons.Filled.Star
    eventType.startsWith("session") || eventType == "refresh" -> Icons.Filled.Devices
    eventType.contains("phone") -> Icons.Filled.Phone
    else -> Icons.Filled.Key
}

// ===========================================================================
// Activity / security events log
// ===========================================================================

@Composable
fun ActivityLogScreen(
    onBack: () -> Unit,
    viewModel: ActivityLogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Load more when the user approaches the top of the list (newest first)
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("سجل النشاط", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "تحديث")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HeroSection(
                title = "سجل النشاط",
                subtitle = "كل عملية على حسابك موثقة بالمنصة والجهاز والمكان والوقت",
                badge = "🔒 تتبع كامل",
                accent = HeroAccents.Security,
                minHeight = 150.dp,
            )
            Spacer(Modifier.height(8.dp))
            when {
                state.loading -> ListSkeleton(rowCount = 5)
                state.events.isEmpty() -> EmptyState(
                    title = "لا يوجد نشاط بعد",
                    subtitle = "ستظهر هنا كل العمليات التي تجريها على حسابك",
                    emoji = "🛡️",
                )
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.events, key = { it.id }) { event ->
                        ActivityEventCard(event)
                    }
                    if (state.loadingMore) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(12.dp),
                                contentAlignment = Alignment.Center,
                            ) { CircularProgressIndicator(modifier = Modifier.size(26.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityEventCard(event: AuthEventDto) {
    val failed = event.status == "failed"
    GlassCard(modifier = Modifier.glowShadow(elevation = 8.dp, glowColor = if (failed) MaterialTheme.colorScheme.error else FazlakaGradientStart)) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (failed) MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(13.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = eventIcon(event.eventType),
                    contentDescription = null,
                    tint = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = eventLabel(event.eventType),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.size(8.dp))
                    PlatformBadgeChip(event.platform, event.os)
                }
                Spacer(Modifier.height(3.dp))
                val details = listOfNotNull(
                    listOfNotNull(event.device, event.os).joinToString(" · ").ifBlank { null },
                    listOfNotNull(event.city, event.country).joinToString("، ").ifBlank { null },
                    event.method?.uppercase(),
                ).joinToString("  •  ")
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = formatDateTime(event.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
            if (failed) {
                Spacer(Modifier.size(8.dp))
                Icon(
                    Icons.Filled.Block,
                    contentDescription = "فشل",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ===========================================================================
// Secondary emails
// ===========================================================================

@Composable
fun SecondaryEmailsScreen(
    onBack: () -> Unit,
    viewModel: SecondaryEmailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var showAdd by remember { mutableStateOf(false) }
    var verifyTarget by remember { mutableStateOf<String?>(null) }
    var primaryTarget by remember { mutableStateOf<String?>(null) }
    var removeTarget by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message, state.error) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("البريد الإلكتروني", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            HeroSection(
                title = "بريدك الإلكتروني",
                subtitle = "البريد الأساسي + البريد الثانوي الموثق لحسابك",
                badge = "✉️ ${state.emails?.secondary?.size ?: 0} بريد ثانوي",
                accent = HeroAccents.Security,
                minHeight = 140.dp,
            )
            Spacer(Modifier.height(16.dp))

            if (state.loading) {
                ListSkeleton(rowCount = 3)
            } else if (state.emails != null) {
                // Primary
                GlassCard {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.MarkEmailRead,
                                contentDescription = null,
                                tint = FazlakaCyan,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                "البريد الأساسي",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = state.emails?.primary?.email ?: "—",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (state.emails?.primary?.isVerified == false) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "غير موثق — تحقق من بريدك",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "البريدات الثانوية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { showAdd = true }) { Text("+ إضافة بريد") }
                }

                val secondary = state.emails?.secondary.orEmpty()
                if (secondary.isEmpty()) {
                    Text(
                        "لا توجد بريدات ثانوية — أضف بريداً احتياطياً لتأمين حسابك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
                secondary.forEach { email ->
                    SecondaryEmailRow(
                        email = email,
                        busy = state.busy,
                        onVerify = { verifyTarget = email.email },
                        onMakePrimary = { primaryTarget = email.email },
                        onRemove = { removeTarget = email.email },
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showAdd) {
        var newEmail by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("إضافة بريد ثانوي") },
            text = {
                Column {
                    Text(
                        "سنرسل رمز توثيق إلى هذا البريد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.add(newEmail)
                        showAdd = false
                    },
                    enabled = newEmail.contains("@") && !state.busy,
                ) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("إلغاء") } },
        )
    }

    verifyTarget?.let { target ->
        var otp by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { verifyTarget = null },
            title = { Text("توثيق $target") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "أدخل الرمز المرسل إلى بريدك",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(12.dp))
                    OtpInputField(value = otp, onValueChange = { otp = it })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verify(target, otp)
                        verifyTarget = null
                    },
                    enabled = otp.length >= 6 && !state.busy,
                ) { Text("توثيق") }
            },
            dismissButton = {
                TextButton(onClick = { verifyTarget = null }) { Text("إلغاء") }
            },
        )
    }

    primaryTarget?.let { target ->
        OtpActionDialog(
            title = "تعيين $target أساسياً",
            hint = "أرسلنا رمز تأكيد إلى بريدك الحالي. سيتم إنهاء الجلسات على باقي الأجهزة بعد التغيير.",
            busy = state.busy,
            onConfirm = { otp ->
                viewModel.makePrimary(target, otp)
                primaryTarget = null
            },
            onDismiss = { primaryTarget = null },
        )
    }

    removeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("حذف $target") },
            text = { Text("هل تريد حذف هذا البريد الثانوي من حسابك؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.remove(target)
                        removeTarget = null
                    },
                ) { Text("حذف", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) { Text("إلغاء") }
            },
        )
    }
}

@Composable
private fun SecondaryEmailRow(
    email: com.fazlaka.app.core.model.dto.UserEmailDto,
    busy: Boolean,
    onVerify: () -> Unit,
    onMakePrimary: () -> Unit,
    onRemove: () -> Unit,
) {
    GlassCard {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = email.email,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (email.isVerified) {
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = "موثق",
                        tint = FazlakaSuccess,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            "غير موثق",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!email.isVerified) {
                    OutlinedButton(onClick = onVerify, enabled = !busy) {
                        Text("توثيق", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    OutlinedButton(onClick = onMakePrimary, enabled = !busy) {
                        Text("تعيين أساسياً", style = MaterialTheme.typography.labelMedium)
                    }
                }
                OutlinedButton(onClick = onRemove, enabled = !busy) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("حذف", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun OtpActionDialog(
    title: String,
    hint: String,
    busy: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var otp by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(hint, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                OtpInputField(value = otp, onValueChange = { otp = it })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(otp) }, enabled = otp.length >= 6 && !busy) {
                Text("تأكيد")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )
}

// ===========================================================================
// Linked accounts
// ===========================================================================

private data class ProviderUi(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
fun LinkedAccountsScreen(
    onBack: () -> Unit,
    viewModel: LinkedAccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var linkProvider by remember { mutableStateOf<String?>(null) }
    var unlinkProvider by remember { mutableStateOf<String?>(null) }
    var otpProvider by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message, state.error) {
        state.message?.let {
            if (it == "otp-required") {
                otpProvider = state.linkProvider ?: linkProvider
            } else {
                snackbar.showSnackbar(it)
            }
            viewModel.clearMessages()
        }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    val providers = listOf(
        ProviderUi("google", "Google", Icons.Filled.Star, Color(0xFF4285F4)),
        ProviderUi("github", "GitHub", Icons.Filled.Key, Color(0xFF0D1117)),
        ProviderUi("facebook", "Facebook", Icons.Filled.Person, Color(0xFF1877F2)),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("الحسابات المرتبطة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            HeroSection(
                title = "الحسابات المرتبطة",
                subtitle = "طرق الدخول المرتبطة بحسابك — يمكنك فك أي طريقة غير مستخدمة",
                badge = "🔗 ${state.status?.let { listOf(it.password, it.phone, it.google, it.github, it.facebook).count { linked -> linked } } ?: 0} طرق دخول",
                accent = HeroAccents.Profile,
                minHeight = 145.dp,
            )
            Spacer(Modifier.height(16.dp))

            if (state.loading) {
                ListSkeleton(rowCount = 4)
            } else {
                // Password
                val hasPassword = state.status?.password == true || state.user?.hasPassword == true
                LinkedRow(
                    title = "كلمة المرور",
                    icon = Icons.Filled.Lock,
                    color = FazlakaGradientStart,
                    linked = hasPassword,
                    actionLabel = null,
                ) {}
                Spacer(Modifier.height(10.dp))

                // Phone
                LinkedRow(
                    title = if (state.status?.phone == true) {
                        "الهاتف ${state.user?.phone?.takeLast(4)?.let { "****$it" } ?: ""}"
                    } else "رقم الهاتف",
                    icon = Icons.Filled.Phone,
                    color = FazlakaSuccess,
                    linked = state.status?.phone == true,
                    actionLabel = if (state.status?.phone == true) "فك الربط" else null,
                ) {
                    if (state.status?.phone == true) {
                        viewModel.removePhone()
                    }
                }
                Spacer(Modifier.height(10.dp))

                providers.forEach { provider ->
                    val linked = when (provider.key) {
                        "google" -> state.status?.google == true
                        "github" -> state.status?.github == true
                        else -> state.status?.facebook == true
                    }
                    LinkedRow(
                        title = provider.title,
                        icon = provider.icon,
                        color = provider.color,
                        linked = linked,
                        actionLabel = if (linked) "فك الربط" else "ربط",
                    ) {
                        if (linked) {
                            unlinkProvider = provider.key
                        } else {
                            linkProvider = provider.key
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Text(
                    "ملاحظة أمنية: لا يمكن فك ربط آخر طريقة دخول متبقية لحسابك، وكل عمليات الربط وفك الربط تُسجَّل في سجل النشاط.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                )
            }
        }
    }

    // Link flow: password confirmation → WebView
    linkProvider?.let { provider ->
        val hasPassword = state.status?.password == true || state.user?.hasPassword == true
        if (hasPassword) {
            PasswordConfirmDialog(
                title = "ربط ${providers.firstOrNull { it.key == provider }?.title ?: provider}",
                busy = state.busy,
                onConfirm = { password ->
                    viewModel.startLink(provider, password)
                    linkProvider = null
                },
                onDismiss = { linkProvider = null },
            )
        } else {
            LaunchedEffect(provider) {
                viewModel.startLink(provider, null)
                linkProvider = null
            }
        }
    }

    // Passwordless accounts: OTP → WebView
    otpProvider?.let { provider ->
        OtpActionDialog(
            title = "تأكيد ربط ${providers.firstOrNull { it.key == provider }?.title ?: provider}",
            hint = "حسابك بدون كلمة مرور — أرسلنا رمز تأكيد إلى بريدك",
            busy = state.busy,
            onConfirm = { otp ->
                viewModel.confirmLinkOtp(provider, otp)
                otpProvider = null
            },
            onDismiss = { otpProvider = null },
        )
    }

    unlinkProvider?.let { provider ->
        AlertDialog(
            onDismissRequest = { unlinkProvider = null },
            title = { Text("فك ربط ${providers.firstOrNull { it.key == provider }?.title ?: provider}") },
            text = { Text("لن تتمكن من الدخول بهذه الطريقة بعد الآن. هل أنت متأكد؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unlink(provider)
                        unlinkProvider = null
                    },
                    enabled = !state.busy,
                ) { Text("فك الربط") }
            },
            dismissButton = {
                TextButton(onClick = { unlinkProvider = null }) { Text("إلغاء") }
            },
        )
    }

    // OAuth link WebView
    state.linkUrl?.let { url ->
        OAuthLinkWebView(
            url = url,
            cookie = viewModel.linkCookie,
            onClose = { linked -> viewModel.closeWebView(linked) },
        )
    }
}

@Composable
private fun LinkedRow(
    title: String,
    icon: ImageVector,
    color: Color,
    linked: Boolean,
    actionLabel: String?,
    onClick: () -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (linked) "مرتبط" else "غير مرتبط",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (linked) FazlakaSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (linked) {
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = null,
                    tint = FazlakaSuccess,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
            }
            if (actionLabel != null) {
                OutlinedButton(onClick = onClick, enabled = true) {
                    Icon(
                        if (linked) Icons.Filled.LinkOff else Icons.Filled.Link,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(actionLabel, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PasswordConfirmDialog(
    title: String,
    busy: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    "أكّد بكلمة مرورك لبدء الربط الآمن",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    label = "كلمة المرور الحالية",
                    isPassword = true,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(password) }, enabled = password.isNotBlank() && !busy) {
                Text("متابعة")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OAuthLinkWebView(
    url: String,
    cookie: String?,
    onClose: (linked: Boolean) -> Unit,
) {
    val baseUrl = remember { BuildConfig.API_BASE_URL.trimEnd('/') }
    val origin = remember { baseUrl.removeSuffix("/api/v1") }
    val apiHeaders = remember {
        mapOf(
            "x-platform" to "MOBILE",
            "x-device-type" to "mobile",
            "x-os" to "Android ${android.os.Build.VERSION.RELEASE}",
            "x-device-name" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "x-app-version" to BuildConfig.VERSION_NAME,
        )
    }

    AlertDialog(
        onDismissRequest = { onClose(false) },
        title = { Text("ربط الحساب") },
        text = {
            Box(Modifier.fillMaxWidth().height(420.dp)) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            cookie?.let { token ->
                                CookieManager.getInstance().setCookie(
                                    origin,
                                    "fazlaka_link=$token; Path=/",
                                )
                                CookieManager.getInstance().flush()
                            }
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    request: WebResourceRequest,
                                ): Boolean = intercept(view, request.url.toString())

                                @Deprecated("Deprecated in API 24")
                                override fun shouldOverrideUrlLoading(
                                    view: WebView,
                                    url: String,
                                ): Boolean = intercept(view, url)

                                private fun intercept(view: WebView, url: String): Boolean = when {
                                    url.contains("link=success") -> {
                                        post { onClose(true) }
                                        true
                                    }
                                    url.contains("link=failed") -> {
                                        post { onClose(false) }
                                        true
                                    }
                                    url.contains("accessToken=") -> {
                                        // Should not happen during linking; treat as abort.
                                        post { onClose(false) }
                                        true
                                    }
                                    url.startsWith("$origin/api/") -> {
                                        view.loadUrl(url, apiHeaders)
                                        true
                                    }
                                    url.startsWith("http://localhost:3001") ||
                                        url.startsWith("http://127.0.0.1:3001") -> {
                                        view.loadUrl(
                                            url
                                                .replace("http://localhost:3001", origin)
                                                .replace("http://127.0.0.1:3001", origin),
                                            apiHeaders,
                                        )
                                        true
                                    }
                                    else -> false
                                }
                            }
                            loadUrl(url, apiHeaders)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onClose(false) }) { Text("إغلاق") }
        },
    )
}

// ===========================================================================
// Change primary email
// ===========================================================================

@Composable
fun ChangeEmailScreen(
    onBack: () -> Unit,
    viewModel: ChangeEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var newEmail by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    LaunchedEffect(state.message, state.error) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.done) {
        if (state.done) {
            kotlinx.coroutines.delay(2500)
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("تغيير البريد الأساسي", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            HeroSection(
                title = "تغيير البريد الأساسي",
                subtitle = "ستُسجَّل خروجك من جميع الأجهزة بعد التغيير لأمانك",
                badge = "✉️ خطوتان فقط",
                accent = HeroAccents.Security,
                minHeight = 140.dp,
            )
            Spacer(Modifier.height(24.dp))

            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "بريدك الحالي",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = state.currentEmail ?: "…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(visible = state.step == 1) {
                Column {
                    Text(
                        "الخطوة ١ — البريد الجديد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = "البريد الإلكتروني الجديد",
                        icon = Icons.Filled.Email,
                    )
                    Spacer(Modifier.height(16.dp))
                    AuthButton(
                        text = "إرسال رمز التأكيد",
                        onClick = { viewModel.request(newEmail) },
                        loading = state.busy,
                        enabled = newEmail.contains("@") && newEmail != state.currentEmail,
                    )
                }
            }

            AnimatedVisibility(visible = state.step == 2) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "الخطوة ٢ — أدخل الرمز المرسل إلى بريدك الحالي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(16.dp))
                    OtpInputField(
                        value = otp,
                        onValueChange = { otp = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    AuthButton(
                        text = "تأكيد التغيير",
                        onClick = { viewModel.confirm(newEmail, otp) },
                        loading = state.busy,
                        enabled = otp.length >= 6,
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
