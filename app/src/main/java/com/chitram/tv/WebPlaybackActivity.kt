package com.chitram.tv

import android.graphics.Color
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.chitram.tv.data.verifiedWebPlaybackPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Debug-only experiment: displays the official watch page without inspecting its player or URLs. */
class WebPlaybackActivity : ComponentActivity() {
    companion object { const val MOVIE_ID = "movie_id" }

    private lateinit var root: FrameLayout
    private lateinit var pageLayout: LinearLayout
    private lateinit var pageContainer: FrameLayout
    private lateinit var status: TextView
    private var browser: WebView? = null
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null
    private var loadJob: Job? = null
    private var pageLoaded = false
    private var fullscreenRequested = false
    private var pageError: String? = null
    private var officialPage: String? = null
    private var previousSystemUiVisibility = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        pageLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        status = TextView(this).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF171B26.toInt())
            textSize = 14f
            setPadding(24, 12, 24, 12)
            isFocusable = true
            setOnClickListener {
                if (pageError != null) {
                    pageError = null
                    browser?.reload() ?: officialPage?.let(::createBrowser)
                }
            }
        }
        pageContainer = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        pageLayout.addView(status, LinearLayout.LayoutParams(-1, -2))
        pageLayout.addView(pageContainer, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(pageLayout, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    customView != null -> closeCustomView(notifyPage = true)
                    browser?.canGoBack() == true -> browser?.goBack()
                    else -> finish()
                }
            }
        })
        updateStatus("Finding official watch page…")
        if (!BuildConfig.DEBUG) { updateStatus("Web playback test is unavailable in this build."); return }
        loadJob = lifecycleScope.launch {
            val page = try {
                withContext(Dispatchers.IO) {
                    val movieId = intent.getLongExtra(MOVIE_ID, 0L)
                    (application as ChitramApplication).repository.findMovie(movieId)?.let(::verifiedWebPlaybackPage)
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (_: Exception) {
                showPageError("Local catalogue could not be opened.")
                return@launch
            }
            if (!isActive) return@launch
            if (page == null) updateStatus("No valid official watch page for this listing.")
            else { officialPage = page; createBrowser(page) }
        }
    }

    private fun createBrowser(page: String) {
        try {
            val view = WebView(this)
            browser = view
            view.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = true
                allowFileAccess = false
                allowContentAccess = false
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                if (Build.VERSION.SDK_INT >= 26) safeBrowsingEnabled = true
            }
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (request.url.scheme != "https") {
                        if (request.isForMainFrame) updateStatus("Blocked an insecure page navigation.")
                        return true
                    }
                    return false
                }

                override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                    pageLoaded = false
                    pageError = null
                    updateStatus("Loading official page…")
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    if (pageError == null) {
                        pageLoaded = true
                        updateStatus("Page loaded; use the TV remote to try its controls.")
                        view.requestFocus()
                    }
                }

                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame) showPageError("Page load failed (${error.errorCode}).")
                }

                override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                    if (request.isForMainFrame && errorResponse.statusCode >= 400) {
                        showPageError("Website returned HTTP ${errorResponse.statusCode}.")
                    }
                }

                override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                    handler.cancel()
                    showPageError("Secure connection failed.")
                }

                override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                    closeCustomView(notifyPage = false)
                    destroyBrowser(view)
                    showPageError("Web player stopped unexpectedly.")
                    return true
                }
            }
            view.webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    if (customView != null) { callback.onCustomViewHidden(); return }
                    fullscreenRequested = true
                    customView = view
                    customCallback = callback
                    previousSystemUiVisibility = window.decorView.systemUiVisibility
                    pageLayout.visibility = View.GONE
                    root.addView(view, FrameLayout.LayoutParams(-1, -1))
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    view.requestFocus()
                    updateStatus("HTML5 fullscreen requested.")
                }

                override fun onHideCustomView() { closeCustomView(notifyPage = false) }
            }
            pageContainer.addView(view, FrameLayout.LayoutParams(-1, -1))
            view.requestFocus()
            view.loadUrl(page)
        } catch (_: Exception) {
            browser?.let(::destroyBrowser)
            showPageError("WebView is unavailable on this TV.")
        }
    }

    private fun closeCustomView(notifyPage: Boolean) {
        val view = customView ?: return
        val callback = customCallback
        customView = null
        customCallback = null
        root.removeView(view)
        pageLayout.visibility = View.VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = previousSystemUiVisibility
        if (notifyPage) callback?.onCustomViewHidden()
        browser?.requestFocus()
        updateStatus("Returned from HTML5 fullscreen.")
    }

    private fun showPageError(message: String) {
        pageLoaded = false
        pageError = message
        updateStatus("$message Press OK to retry.")
        status.requestFocus()
    }

    private fun updateStatus(message: String) {
        status.text = "WEB TEST  ·  $message\nPage loaded: ${if (pageLoaded) "yes" else "no"}  ·  Fullscreen requested: ${if (fullscreenRequested) "yes" else "no"}  ·  Back returns to Details"
    }

    override fun onResume() { super.onResume(); browser?.onResume() }
    override fun onPause() { browser?.onPause(); super.onPause() }

    override fun onDestroy() {
        loadJob?.cancel()
        closeCustomView(notifyPage = true)
        browser?.let(::destroyBrowser)
        super.onDestroy()
    }

    private fun destroyBrowser(view: WebView) {
        if (browser === view) browser = null
        (view.parent as? ViewGroup)?.removeView(view)
        runCatching { view.stopLoading() }
        view.webChromeClient = null
        view.webViewClient = WebViewClient()
        runCatching { view.destroy() }
    }
}
