package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

private class AndroidWebBridge(private val context: Context) {
    @JavascriptInterface
    fun shareStandaloneHtml(htmlContent: String) {
        shareTextAsHtml(context, htmlContent)
    }
}

private fun buildStandaloneHtmlFromAssets(context: Context): String {
    return try {
        val assets = context.assets
        val css = assets.open("web/style.css").bufferedReader().use { it.readText() }
        val catalogJs = assets.open("web/catalog.js").bufferedReader().use { it.readText() }
        val audioJs = assets.open("web/audio.js").bufferedReader().use { it.readText() }
        val gameJs = assets.open("web/game.js").bufferedReader().use { it.readText() }
        val appJs = assets.open("web/app.js").bufferedReader().use { it.readText() }
        val indexHtml = assets.open("web/index.html").bufferedReader().use { it.readText() }
        val matrixJson = runCatching {
            assets.open("meditations_matrix.json").bufferedReader().use { it.readText() }
        }.getOrDefault("[]")

        indexHtml
            .replace("""<link rel="stylesheet" href="./style.css" />""", "<style>\n$css\n</style>")
            .replace(
                """<script src="./catalog.js"></script>""",
                "<script>window.NOORBIS_MEDITATIONS_MATRIX = $matrixJson;</script>\n<script>\n$catalogJs\n</script>"
            )
            .replace("""<script src="./audio.js"></script>""", "<script>\n$audioJs\n</script>")
            .replace("""<script src="./game.js"></script>""", "<script>\n$gameJs\n</script>")
            .replace("""<script src="./app.js"></script>""", "<script>\n$appJs\n</script>")
    } catch (e: Exception) {
        "<!-- Error building bundle: ${e.message} -->"
    }
}

private fun shareTextAsHtml(context: Context, htmlContent: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "index.html — Noôrbis (Version Web HTML5 GitHub Pages)")
        putExtra(Intent.EXTRA_TEXT, htmlContent)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Exporter index.html (GitHub Pages)"))
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebGitHubPagesDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceBackground),
            color = SpaceBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🌐 Noôrbis — Application Web HTML5",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prête pour GitHub Pages (/index.html, /css, /js)",
                            style = MaterialTheme.typography.labelSmall,
                            color = NebulaGray
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showInstructions = !showInstructions },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("web_github_help_button")
                        ) {
                            Text(
                                text = if (showInstructions) "Masquer Guide" else "Guide GitHub Pages",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolarGold
                            )
                        }

                        Button(
                            onClick = {
                                val bundled = buildStandaloneHtmlFromAssets(context)
                                shareTextAsHtml(context, bundled)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldNova,
                                contentColor = SpaceBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_standalone_html_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Exporter HTML",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Exporter .HTML",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_web_preview_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer l'aperçu Web",
                                tint = StarlightWhite
                            )
                        }
                    }
                }

                if (showInstructions) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = SpaceSurfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🚀 Comment héberger sur GitHub Pages :",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1. Les fichiers Web complets sont générés à la racine du projet : /index.html, /css/style.css, /js/catalog.js, /js/audio.js, /js/game.js et /js/app.js.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarlightWhite
                            )
                            Text(
                                text = "2. Poussez ce projet vers GitHub (ou téléchargez le ZIP depuis AI Studio), puis dans votre dépôt GitHub ouvrez Settings ➔ Pages ➔ Branch : main / (root) ➔ Save.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarlightWhite
                            )
                            Text(
                                text = "3. Vous pouvez aussi cliquer sur « 💾 Exporter index.html autonome » pour obtenir un fichier HTML unique contenant tout le jeu et les 810 méditations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldNova
                            )
                        }
                    }
                }

                // Live Interactive HTML5 WebView
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            @Suppress("DEPRECATION")
                            settings.allowFileAccessFromFileURLs = true
                            @Suppress("DEPRECATION")
                            settings.allowUniversalAccessFromFileURLs = true
                            settings.mediaPlaybackRequiresUserGesture = false

                            addJavascriptInterface(AndroidWebBridge(ctx), "AndroidWebBridge")
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    // Inject the 810 meditations matrix from Android assets into the WebView
                                    runCatching {
                                        val json = ctx.assets.open("meditations_matrix.json")
                                            .bufferedReader().use { it.readText() }
                                        view?.evaluateJavascript(
                                            "window.NOORBIS_MEDITATIONS_MATRIX = $json; if (window.NoorbisCatalog) window.NoorbisCatalog.loadMeditationsMatrix();",
                                            null
                                        )
                                    }
                                }
                            }
                            loadUrl("file:///android_asset/web/index.html")
                        }
                    }
                )
            }
        }
    }
}
