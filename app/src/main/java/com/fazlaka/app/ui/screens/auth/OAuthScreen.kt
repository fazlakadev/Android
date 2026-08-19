package com.fazlaka.app.ui.screens.auth

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.BuildConfig
import com.fazlaka.app.ui.components.AuthBackground
import com.fazlaka.app.ui.viewmodel.AuthViewModel
import java.net.URLDecoder

private fun extractParam(url: String, name: String): String? {
    val marker = "$name="
    val start = url.indexOf(marker)
    if (start < 0) return null
    var end = url.indexOf('&', start)
    if (end < 0) end = url.length
    val raw = url.substring(start + marker.length, end)
    return runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OAuthScreen(
    provider: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var pageLoading by remember { mutableStateOf(true) }
    var localError by remember { mutableStateOf<String?>(null) }

    val baseUrl = remember { BuildConfig.API_BASE_URL.trimEnd('/') }
    val origin = remember { baseUrl.removeSuffix("/api/v1") }
    // The OAuth callback lands on our API without the app's identity headers
    // (it is a browser redirect), so we re-load API-origin URLs with them —
    // that way the created session + login event + alert email correctly say
    // "Android device" instead of an anonymous web login.
    val apiHeaders = remember {
        mapOf(
            "x-platform" to "MOBILE",
            "x-device-type" to "mobile",
            "x-os" to "Android ${android.os.Build.VERSION.RELEASE}",
            "x-device-name" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "x-app-version" to BuildConfig.VERSION_NAME,
        )
    }
    val startUrl = remember {
        val p = when (provider.lowercase()) {
            "github" -> "github"
            "facebook" -> "facebook"
            else -> "google"
        }
        "$origin/api/v1/auth/$p"
    }
    val title = when (provider.lowercase()) {
        "github" -> "تسجيل الدخول عبر GitHub"
        "facebook" -> "تسجيل الدخول عبر Facebook"
        else -> "تسجيل الدخول عبر Google"
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(localError) {
        localError?.let {
            snackbar.showSnackbar(it)
            localError = null
        }
    }
    LaunchedEffect(state.success) {
        if (state.success) onDone()
    }

    val client = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                pageLoading = true
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                pageLoading = false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                pageLoading = false
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return intercept(view, request.url.toString())
            }

            @Deprecated("Deprecated in API 24 but kept for compatibility")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return intercept(view, url)
            }

            private fun intercept(view: WebView, url: String): Boolean {
                return when {
                    url.contains("accessToken=") -> {
                        viewModel.socialLogin(
                            extractParam(url, "accessToken"),
                            extractParam(url, "refreshToken"),
                        )
                        true
                    }
                    url.startsWith("http://localhost") && url.contains("error=") -> {
                        localError = "تعذر تسجيل الدخول عبر المزود، يرجى المحاولة مرة أخرى"
                        true
                    }
                    url.startsWith("$origin/") || url.startsWith("$origin/api/") -> {
                        view.loadUrl(url, apiHeaders)
                        true
                    }
                    url.startsWith("http://localhost") ||
                        url.startsWith("http://127.0.0.1") -> {
                        val rewritten = url
                            .replaceFirst("http://localhost", origin)
                            .replaceFirst("http://127.0.0.1", origin)
                        view.loadUrl(rewritten, apiHeaders)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    val webViewState = remember { mutableStateOf<WebView?>(null) }
    BackHandler {
        val webView = webViewState.value
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            onBack()
        }
    }

    AuthBackground {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White,
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                                webViewClient = client
                                webViewState.value = this
                                loadUrl(startUrl, apiHeaders)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (pageLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}
