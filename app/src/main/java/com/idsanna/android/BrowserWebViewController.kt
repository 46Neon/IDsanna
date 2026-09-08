package com.idsanna.android

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.json.JSONObject

class BrowserWebViewController(
    private val webView: WebView,
    private val policy: BrowserNavigationPolicy = BrowserNavigationPolicy()
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastUrl: String? = null
    @Volatile private var pageEpoch = 0L
    @Volatile private var readyLatch = CountDownLatch(0)

    init { configure() }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configure() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                pageEpoch += 1
                readyLatch = CountDownLatch(1)
                lastUrl = url
            }

            override fun onPageFinished(view: WebView, url: String) {
                lastUrl = url
                readyLatch.countDown()
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                !policy.isAllowed(request.url)
        }
    }

    fun navigate(url: String): BrowserNavigationResult {
        val uri = Uri.parse(url)
        if (!policy.isAllowed(uri)) return BrowserNavigationResult(false, "url_not_allowed", lastUrl)
        lastUrl = uri.toString()
        mainHandler.post { webView.loadUrl(lastUrl!!) }
        return BrowserNavigationResult(true, "navigation_requested", lastUrl)
    }

    fun observe(timeoutMs: Long = 8_000L): BrowserObservationResult {
        val ready = readyLatch.await(timeoutMs.coerceIn(250L, 15_000L), TimeUnit.MILLISECONDS)
        if (!ready) return BrowserObservationResult(false, "page_not_ready", null)
        val ref = AtomicReference<String?>(null)
        val finished = CountDownLatch(1)
        mainHandler.post {
            val js = """
                (function(){
                  try {
                    var nodes = document.querySelectorAll('a,button,input,select,textarea,[role=button]');
                    var elements = [];
                    for (var i=0; i<nodes.length && elements.length<80; i++) {
                      var n=nodes[i], r=n.getBoundingClientRect();
                      if (!(r.width>0 && r.height>0)) continue;
                      elements.push({id:'e'+i,tag:(n.tagName||'').toLowerCase(),text:((n.innerText||n.value||n.getAttribute('aria-label')||'').trim()).slice(0,160),visible:true});
                    }
                    return JSON.stringify({url:location.href,title:document.title||'',text:((document.body&&document.body.innerText)||'').replace(/\\s+/g,' ').trim().slice(0,4000),elements:elements});
                  } catch(e) { return JSON.stringify({error:String(e)}); }
                })()
            """.trimIndent()
            webView.evaluateJavascript(js) { raw -> ref.set(unescapeJson(raw ?: "")); finished.countDown() }
        }
        finished.await(timeoutMs.coerceIn(250L, 15_000L), TimeUnit.MILLISECONDS)
        val raw = ref.get() ?: return BrowserObservationResult(false, "observation_timeout", null)
        return runCatching {
            val json = JSONObject(raw)
            if (json.has("error")) BrowserObservationResult(false, json.optString("error"), null)
            else BrowserObservationResult(true, "observation_ready", BrowserPageObservation(pageEpoch, json.optString("url", lastUrl ?: ""), json.optString("title", ""), json.optString("text", ""), json.optJSONArray("elements")?.toString() ?: "[]"))
        }.getOrElse { BrowserObservationResult(false, "invalid_observation", null) }
    }

    private fun unescapeJson(raw: String): String = runCatching {
        if (raw.length >= 2 && raw.first() == '"' && raw.last() == '"') JSONObject("{\"v\":$raw}").getString("v") else raw
    }.getOrDefault(raw)

    fun currentUrl(): String? = lastUrl
    fun currentPageEpoch(): Long = pageEpoch
}

data class BrowserPageObservation(
    val pageEpoch: Long,
    val url: String,
    val title: String,
    val visibleText: String,
    val elementsJson: String
)

data class BrowserObservationResult(
    val ready: Boolean,
    val evidence: String,
    val observation: BrowserPageObservation?
)

class BrowserNavigationPolicy(private val allowedHosts: Set<String> = emptySet()) {
    fun isAllowed(uri: Uri): Boolean {
        if (uri.scheme != "https") return false
        val host = uri.host?.lowercase() ?: return false
        return allowedHosts.isEmpty() || allowedHosts.any { host == it || host.endsWith(".$it") }
    }
}

data class BrowserNavigationResult(val accepted: Boolean, val evidence: String, val url: String?)
