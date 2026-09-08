package com.idsanna.android

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class BrowserWebViewController(
    private val webView: WebView,
    private val policy: BrowserNavigationPolicy = BrowserNavigationPolicy()
) {
    private var lastUrl: String? = null

    init {
        configure()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configure() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                !policy.isAllowed(request.url)
        }
    }

    fun navigate(url: String): BrowserNavigationResult {
        val uri = Uri.parse(url)
        if (!policy.isAllowed(uri)) return BrowserNavigationResult(false, "url_not_allowed", lastUrl)
        lastUrl = uri.toString()
        webView.loadUrl(lastUrl!!)
        return BrowserNavigationResult(true, "navigation_requested", lastUrl)
    }

    fun currentUrl(): String? = lastUrl
}

class BrowserNavigationPolicy(
    private val allowedHosts: Set<String> = emptySet()
) {
    fun isAllowed(uri: Uri): Boolean {
        if (uri.scheme != "https") return false
        val host = uri.host?.lowercase() ?: return false
        return allowedHosts.isEmpty() || allowedHosts.any { host == it || host.endsWith(".$it") }
    }
}

data class BrowserNavigationResult(
    val accepted: Boolean,
    val evidence: String,
    val url: String?
)
