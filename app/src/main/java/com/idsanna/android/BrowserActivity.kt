package com.idsanna.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class BrowserActivity : Activity() {
    private lateinit var controller: BrowserWebViewController
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = EditText(this).apply { hint = "https://..."; setSingleLine(true) }
        status = TextView(this).apply { text = "Sesión visible · sin navegación"; setTextColor(Color.DKGRAY); setPadding(0, 8, 0, 8) }
        val webView = WebView(this)
        controller = BrowserWebViewController(webView)
        BrowserSession.attach(controller)

        val go = Button(this).apply {
            text = "Navegar"
            setOnClickListener {
                val result = controller.navigate(address.text.toString().trim())
                status.text = if (result.accepted) "Navegación solicitada · esperando verificación" else "Navegación bloqueada: ${result.evidence}"
            }
        }
        val observe = Button(this).apply {
            text = "Observar"
            setOnClickListener {
                val result = controller.observe()
                status.text = if (result.ready) {
                    val page = result.observation!!
                    "Observación verificada · epoch ${page.pageEpoch}\n${page.title}\n${page.url}\nTexto: ${page.visibleText.take(240)}"
                } else "Observación no disponible: ${result.evidence}"
            }
        }
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; addView(address, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); addView(go); addView(observe) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12, 12, 12, 12); addView(toolbar); addView(status); addView(webView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)) }
        setContentView(root)
    }

    override fun onDestroy() {
        BrowserSession.detach(controller)
        super.onDestroy()
    }
}
