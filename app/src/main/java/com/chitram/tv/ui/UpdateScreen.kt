package com.chitram.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitram.tv.BuildConfig
import com.chitram.tv.updates.GitHubUpdates
import com.chitram.tv.updates.NoPublishedRelease
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun UpdateScreen(entryFocus: FocusRequester) {
    val context = LocalContext.current
    val updater = remember(context) { GitHubUpdates(context) }
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Press Check now to look for the latest GitHub release.") }
    var available by remember { mutableStateOf<com.chitram.tv.updates.AvailableUpdate?>(null) }
    var downloaded by remember { mutableStateOf<File?>(null) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entryFocus.requestFocus() }
    Column(Modifier.fillMaxSize().background(Ink).padding(horizontal = 34.dp, vertical = 26.dp)) {
        ScreenHeading("CHITRAM", "App updates", "Installed version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Spacer(Modifier.height(30.dp))
        Text(status, color = Cream, fontSize = 16.sp)
        Spacer(Modifier.height(24.dp))
        FocusAction("Check now", {
            if (!busy) scope.launch {
                busy = true; status = "Checking GitHub…"; available = null; downloaded = null
                try {
                    available = updater.check()
                    status = available?.let { "Version ${it.versionCode} is available." } ?: "You have the latest published version."
                } catch (_: NoPublishedRelease) { status = "No installable update has been published yet." }
                catch (_: Exception) { status = "Could not check for updates. Check the connection and try again." }
                busy = false
            }
        }, modifier = Modifier.focusRequester(entryFocus), quiet = true, menuOnLeft = true)
        if (available != null) {
            Spacer(Modifier.height(14.dp))
            FocusAction(if (downloaded == null) "Download update" else "Install update", {
                if (!busy) scope.launch {
                    if (downloaded == null) {
                        busy = true; status = "Downloading and verifying APK…"
                        try { downloaded = updater.download(available!!); status = "Download verified. Press Install update." }
                        catch (_: Exception) { status = "Download or verification failed. Try again." }
                        busy = false
                    } else {
                        try {
                            updater.install(downloaded!!)
                            status = "Approve installation on the TV. If asked, allow Chitram to install apps, then press Install update again."
                        } catch (_: Exception) { status = "Could not open the TV installer." }
                    }
                }
            }, quiet = true, menuOnLeft = true)
        }
        Spacer(Modifier.height(24.dp))
        Text("Updates keep your local profiles and catalogue when the APK uses the same signing key. Android asks you to approve installation.",
            color = Muted, fontSize = 14.sp)
    }
}
