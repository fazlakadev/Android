package com.fazlaka.app.ui.screens.security

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.TotpSetupDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.AuthField
import com.fazlaka.app.ui.components.GlassCard
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.components.glowShadow
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.theme.FazlakaGradientStart
import com.fazlaka.app.ui.viewmodel.LoginAlertsViewModel
import com.fazlaka.app.ui.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: SecurityViewModel = hiltViewModel(),
    alertsViewModel: LoginAlertsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val alertsState by alertsViewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var showTotpSetup by remember { mutableStateOf(false) }
    var totpCode by remember { mutableStateOf("") }
    var showTotpDisable by remember { mutableStateOf(false) }
    var disableCode by remember { mutableStateOf("") }

    LaunchedEffect(state.message, state.error) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(alertsState.message, alertsState.error) {
        alertsState.message?.let { snackbar.showSnackbar(it); alertsViewModel.clearMessages() }
        alertsState.error?.let { snackbar.showSnackbar(it); alertsViewModel.clearMessages() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.st_security_sessions), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadSessions) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )

            HeroSection(
                title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.more_security),
                subtitle = "تحكم كامل في حماية حسابك: كلمة المرور، التحقق بخطوتين، الجلسات، والتتبع",
                badge = if (state.twoFactorEnabled) "🛡️ محمي بالتحقق بخطوتين" else "🛡️ حماية أساسية",
                accent = HeroAccents.Security,
                minHeight = 165.dp,
            )

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(20.dp))

                // ---------------- Quick access ----------------
                SecurityNavCard(
                    icon = Icons.Filled.History,
                    title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.pf_activity_log),
                    subtitle = "كل عملية موثقة بالمنصة والجهاز والمكان",
                    onClick = { onNavigate(Routes.ACTIVITY_LOG) },
                )
                Spacer(Modifier.height(10.dp))
                SecurityNavCard(
                    icon = Icons.Filled.AlternateEmail,
                    title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.pf_email),
                    subtitle = "البريد الأساسي والبريدات الثانوية الموثقة",
                    onClick = { onNavigate(Routes.SECONDARY_EMAILS) },
                )
                Spacer(Modifier.height(10.dp))
                SecurityNavCard(
                    icon = Icons.Filled.Link,
                    title = androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.pf_linked),
                    subtitle = "Google · GitHub · Facebook · الهاتف",
                    onClick = { onNavigate(Routes.LINKED_ACCOUNTS) },
                )
                Spacer(Modifier.height(20.dp))

                // ---------------- Login alerts ----------------
                GlassCard(modifier = Modifier.glowShadow(elevation = 10.dp, glowColor = FazlakaGradientStart)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "تنبيهات تسجيل الدخول",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "إيميل فوري عند كل دخول يتضمن الجهاز والموقع والوقت",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = alertsState.loginAlerts,
                            onCheckedChange = alertsViewModel::setLoginAlerts,
                            enabled = !alertsState.busy && alertsState.emailNotifications,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))

                // ---------------- Change password ----------------
                SectionTitle("تغيير كلمة المرور", Icons.Filled.Password)
                Spacer(Modifier.height(12.dp))
                AuthField(
                    value = current,
                    onValueChange = { current = it },
                    label = "كلمة المرور الحالية",
                    isPassword = true,
                )
                Spacer(Modifier.height(12.dp))
                AuthField(
                    value = new,
                    onValueChange = { new = it },
                    label = "كلمة المرور الجديدة",
                    isPassword = true,
                )
                Spacer(Modifier.height(16.dp))
                AuthButton(
                    text = "تحديث كلمة المرور",
                    onClick = { viewModel.changePassword(current, new) },
                    loading = state.changingPassword,
                    enabled = current.isNotBlank() && new.length >= 8,
                )
                Spacer(Modifier.height(24.dp))

                // ---------------- 2FA ----------------
                SectionTitle(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.twofa_title), Icons.Filled.Shield)
                Spacer(Modifier.height(12.dp))
                if (state.twoFactorEnabled) {
                    Text(
                        text = "التحقق بخطوتين مفعّل حالياً — حسابك محمي بطبقة إضافية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { showTotpDisable = true }) {
                        Text("تعطيل التحقق بخطوتين")
                    }
                } else {
                    Text(
                        text = "أضف طبقة حماية إضافية عبر تطبيق مصادقة (Google Authenticator مثلاً)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.loadTotp()
                            showTotpSetup = true
                        },
                        enabled = !state.totpBusy,
                    ) {
                        Text("إعداد التحقق بخطوتين (TOTP)")
                    }
                }
                Spacer(Modifier.height(24.dp))

                // ---------------- Sessions ----------------
                SectionTitle("الجلسات النشطة", Icons.Filled.Devices)
                Spacer(Modifier.height(6.dp))
                TextButtonRow("تسجيل الخروج من جميع الأجهزة الأخرى") { viewModel.revokeOtherSessions() }
                Spacer(Modifier.height(10.dp))
                SessionsList(viewModel)
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    (state.totp as? ApiResult.Success)?.data?.let { setup ->
        if (showTotpSetup) {
            TotpSetupDialog(
                setup = setup,
                code = totpCode,
                onCodeChange = { totpCode = it },
                busy = state.totpBusy,
                onEnable = { viewModel.enableTotp(totpCode) },
                onDismiss = {
                    showTotpSetup = false
                    totpCode = ""
                    viewModel.clearMessages()
                },
            )
        }
    }

    if (showTotpDisable) {
        AlertDialog(
            onDismissRequest = { showTotpDisable = false },
            title = { Text("تعطيل التحقق بخطوتين") },
            text = {
                Column {
                    Text(
                        text = "أدخل رمز المصادقة الحالي لتأكيد التعطيل",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = disableCode,
                        onValueChange = { disableCode = it },
                        label = { Text("رمز التحقق") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disableTotp(disableCode)
                        showTotpDisable = false
                        disableCode = ""
                    },
                    enabled = disableCode.isNotBlank() && !state.totpBusy,
                ) { Text("تعطيل") }
            },
            dismissButton = {
                TextButton(onClick = { showTotpDisable = false }) { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_cancel)) }
            },
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SecurityNavCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .glowShadow(elevation = 8.dp, glowColor = FazlakaGradientStart.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(13.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TotpSetupDialog(
    setup: TotpSetupDto,
    code: String,
    onCodeChange: (String) -> Unit,
    busy: Boolean,
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("إعداد التحقق بخطوتين") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                setup.qrDataUrl?.let { url ->
                    QrImage(url, modifier = Modifier.size(180.dp))
                }
                setup.secret?.let {
                    Text(
                        text = "المفتاح السري:\n$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { onCodeChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("رمز التحقق (6 أرقام)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(onClick = onEnable, enabled = code.length >= 6 && !busy) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("تفعيل")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text(androidx.compose.ui.res.stringResource(com.fazlaka.app.R.string.fr_cancel)) }
        },
    )
}

@Composable
private fun QrImage(dataUrl: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                val encoded = dataUrl.substringAfter("base64,", dataUrl)
                try {
                    val bytes = Base64.decode(encoded, Base64.DEFAULT)
                    setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                } catch (_: Exception) {
                    // malformed qr payload
                }
            }
        },
    )
}

@Composable
private fun TextButtonRow(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun SessionsList(viewModel: SecurityViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state.sessions) {
        null -> Unit
        is ApiResult.Failure -> Text(
            "تعذّر تحميل الجلسات",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is ApiResult.Success -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (s.data.isEmpty()) {
                    Text(
                        text = "لا توجد جلسات نشطة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                s.data.forEach { session ->
                    GlassCard {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = session.deviceName
                                            ?: platformBadge(session.platform, session.os).label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    PlatformBadgeChip(session.platform, session.os)
                                    if (session.isCurrent) {
                                        Spacer(Modifier.size(6.dp))
                                        Surface(
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        ) {
                                            Text(
                                                text = "هذه الجهاز",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = listOfNotNull(
                                        session.os,
                                        session.browser,
                                        listOfNotNull(session.city, session.country)
                                            .joinToString("، ").ifBlank { null },
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                            if (!session.isCurrent) {
                                TextButton(onClick = { viewModel.revokeSession(session.id) }) {
                                    Text("إنهاء", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
