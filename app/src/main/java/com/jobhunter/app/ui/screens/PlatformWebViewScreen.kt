package com.jobhunter.app.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

data class PlatformConfig(
    val name: String,
    val icon: String,
    val color: Color,
    val searchUrl: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformWebView(
    platform: PlatformConfig,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var currentUrl by remember { mutableStateOf(platform.searchUrl) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(platform.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            currentUrl,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                    IconButton(onClick = { webView?.loadUrl(platform.searchUrl) }) {
                        Icon(Icons.Default.Home, contentDescription = "Ana Sayfa")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = platform.color)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            allowFileAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = settings.userAgentString.replace("wv", "")
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false

                                // intent:// scheme'leri yakala
                                if (url.startsWith("intent://")) {
                                    val fallbackUrl = extractFallbackUrl(url)
                                    if (!fallbackUrl.isNullOrEmpty()) {
                                        try {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(fallbackUrl)
                                            )
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                    return true
                                }

                                // tel://, sms://, mailto: gibi scheme'leri disari ac
                                if (url.startsWith("tel:") || url.startsWith("sms:") || 
                                    url.startsWith("mailto:")) {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(url)
                                        )
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                    return true
                                }

                                // app:// scheme'leri engelle (mobil uygulama yonlendirmeleri)
                                if (url.startsWith("app://") || url.startsWith("kariyerisara://") || 
                                    url.startsWith("linkedin://") || url.startsWith("indeed://")) {
                                    return true
                                }

                                // play.google.com linkleri disari ac
                                if (url.contains("play.google.com")) {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(url)
                                        )
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                    return true
                                }

                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                currentUrl = url ?: ""
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                currentUrl = url ?: ""

                                // Kariyer.net icin mobil uygulama yonlendirmelerini engelle
                                if (platform.name == "Kariyer.net") {
                                    view?.evaluateJavascript("""
                                        // Mobil uygulama yonlendirmelerini engelle
                                        document.querySelectorAll('a[href*="intent://"]').forEach(el => {
                                            el.removeAttribute('href');
                                        });
                                        // "Uygulamada Ac" butonlarini gizle
                                        document.querySelectorAll('[class*="app"], [class*="mobile"]').forEach(el => {
                                            if (el.textContent.includes('Uygulamada') || el.textContent.includes('Uygulamay')) {
                                                el.style.display = 'none';
                                            }
                                        });
                                        // Open in app popup'larini gizle
                                        document.querySelectorAll('.open-in-app, .app-download, .download-app').forEach(el => {
                                            el.style.display = 'none';
                                        });
                                    """, null)
                                }

                                // LinkedIn icin login popup'i kapat
                                if (platform.name == "LinkedIn") {
                                    view?.evaluateJavascript("""
                                        document.querySelectorAll('[aria-label="Dismiss"]').forEach(el => el.click());
                                        document.querySelectorAll('.msg-overlay-bubble-header__control--dismiss').forEach(el => el.click());
                                    """, null)
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                // Hata yonetimi
                            }
                        }

                        webChromeClient = object : WebChromeClient() {}

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webView = this
                        loadUrl(platform.searchUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = platform.color
                )
            }

            // Floating action buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(currentUrl)
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Tarayicida Ac")
                }
            }
        }
    }
}

/**
 * intent:// URL'sinden browser_fallback_url'yi cikarir
 * Orn: intent://anasayfa#Intent;scheme=kariyerisara;...S.browser_fallback_url=https%3A%2F%2Fwww.kariyer.net;end
 */
private fun extractFallbackUrl(intentUrl: String): String? {
    try {
        // Yontem 1: S.browser_fallback_url
        val pattern1 = Regex("""S\.browser_fallback_url=([^;]+)""")
        val match1 = pattern1.find(intentUrl)
        if (match1 != null) {
            return java.net.URLDecoder.decode(match1.groupValues[1], "UTF-8")
        }

        // Yontem 2: scheme parametresini bul ve fallback olustur
        val schemePattern = Regex("""scheme=([^;]+)""")
        val schemeMatch = schemePattern.find(intentUrl)
        if (schemeMatch != null) {
            val scheme = schemeMatch.groupValues[1]
            // Bilinen scheme'ler icin fallback URL olustur
            return when (scheme) {
                "kariyerisara" -> "https://www.kariyer.net"
                "linkedin" -> "https://www.linkedin.com"
                "indeed" -> "https://tr.indeed.com"
                else -> null
            }
        }

        // Yontem 3: intent URL'sindeki tum parametreleri parse et
        val packagePattern = Regex("""package=([^;]+)""")
        val packageMatch = packagePattern.find(intentUrl)
        if (packageMatch != null) {
            // Package bulunduysa, fallback URL yok demektir
            // En azindan kariyer.net anasayfasina yonlendir
            if (intentUrl.contains("kariyer")) {
                return "https://www.kariyer.net"
            }
        }
    } catch (_: Exception) {}
    return null
}
