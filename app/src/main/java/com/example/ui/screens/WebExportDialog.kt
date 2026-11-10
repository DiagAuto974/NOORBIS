package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private class AndroidWebBridge(private val context: Context) {
    @JavascriptInterface
    fun shareStandaloneHtml(htmlContent: String) {
        shareTextAsHtml(context, htmlContent)
    }

    @JavascriptInterface
    fun exportSaveJson(jsonContent: String) {
        context.getSharedPreferences("noorbis_local_storage", Context.MODE_PRIVATE)
            .edit()
            .putString("noorbis_web_state_v2", jsonContent)
            .apply()
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val fileName = "noorbis_sauvegarde_$dateStr.json"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            putExtra(Intent.EXTRA_TEXT, jsonContent)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Exporter la sauvegarde (.json)"))
    }
}

private fun readAssetWithFallback(context: Context, primaryPath: String, fallbackPath: String): String {
    val assets = context.assets
    return runCatching {
        assets.open(primaryPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }.getOrElse {
        assets.open(fallbackPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

internal fun buildStandaloneHtmlFromAssets(context: Context): String {
    return try {
        val assets = context.assets
        val css = readAssetWithFallback(context, "web/css/style.css", "web/style.css")
        val catalogJs = readAssetWithFallback(context, "web/js/catalog.js", "web/catalog.js")
        val audioJs = readAssetWithFallback(context, "web/js/audio.js", "web/audio.js")
        val gameJs = readAssetWithFallback(context, "web/js/game.js", "web/game.js")
        val appJs = readAssetWithFallback(context, "web/js/app.js", "web/app.js")
        val indexHtml = assets.open("web/index.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val matrixJson = runCatching {
            assets.open("meditations_matrix.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrDefault("[]")

        indexHtml
            .replace("""<link rel="stylesheet" href="css/style.css" />""", "<style>\n$css\n</style>")
            .replace("""<link rel="stylesheet" href="./css/style.css" />""", "<style>\n$css\n</style>")
            .replace("""<link rel="stylesheet" href="./style.css" />""", "<style>\n$css\n</style>")
            .replace(
                """<script src="js/catalog.js"></script>""",
                "<script>window.NOORBIS_MEDITATIONS_MATRIX = $matrixJson;</script>\n<script>\n$catalogJs\n</script>"
            )
            .replace(
                """<script src="./js/catalog.js"></script>""",
                "<script>window.NOORBIS_MEDITATIONS_MATRIX = $matrixJson;</script>\n<script>\n$catalogJs\n</script>"
            )
            .replace(
                """<script src="./catalog.js"></script>""",
                "<script>window.NOORBIS_MEDITATIONS_MATRIX = $matrixJson;</script>\n<script>\n$catalogJs\n</script>"
            )
            .replace("""<script src="js/audio.js"></script>""", "<script>\n$audioJs\n</script>")
            .replace("""<script src="./js/audio.js"></script>""", "<script>\n$audioJs\n</script>")
            .replace("""<script src="./audio.js"></script>""", "<script>\n$audioJs\n</script>")
            .replace("""<script src="js/game.js"></script>""", "<script>\n$gameJs\n</script>")
            .replace("""<script src="./js/game.js"></script>""", "<script>\n$gameJs\n</script>")
            .replace("""<script src="./game.js"></script>""", "<script>\n$gameJs\n</script>")
            .replace("""<script src="js/app.js"></script>""", "<script>\n$appJs\n</script>")
            .replace("""<script src="./js/app.js"></script>""", "<script>\n$appJs\n</script>")
            .replace("""<script src="./app.js"></script>""", "<script>\n$appJs\n</script>")
    } catch (e: Exception) {
        "<!-- Error building bundle: ${e.message} -->"
    }
}

private fun buildGitHubPagesZipBytes(context: Context): ByteArray {
    val assets = context.assets
    val indexHtml = assets.open("web/index.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
    val css = readAssetWithFallback(context, "web/css/style.css", "web/style.css")
    val catalogJs = readAssetWithFallback(context, "web/js/catalog.js", "web/catalog.js")
    val audioJs = readAssetWithFallback(context, "web/js/audio.js", "web/audio.js")
    val gameJs = readAssetWithFallback(context, "web/js/game.js", "web/game.js")
    val appJs = readAssetWithFallback(context, "web/js/app.js", "web/app.js")
    val matrixJson = runCatching {
        assets.open("meditations_matrix.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
    }.getOrDefault("[]")

    val entries = linkedMapOf(
        "index.html" to indexHtml,
        "css/style.css" to css,
        "js/catalog.js" to catalogJs,
        "js/audio.js" to audioJs,
        "js/game.js" to gameJs,
        "js/app.js" to appJs,
        "meditations_matrix.json" to matrixJson
    )

    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos).use { zos ->
        for ((name, text) in entries) {
            val entry = ZipEntry(name)
            zos.putNextEntry(entry)
            zos.write(text.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }
    return baos.toByteArray()
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
    var statusBanner by remember { mutableStateOf<String?>(null) }

    val saveZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val zipBytes = buildGitHubPagesZipBytes(context)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(zipBytes)
                }
                statusBanner = "✅ Pack GitHub Pages (index.html, css/style.css, js/*) enregistré avec succès !"
            }.onFailure {
                statusBanner = "⚠️ Erreur lors de l'enregistrement de l'archive .zip."
            }
        }
    }

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
                            text = "Chemins relatifs prêts : index.html, css/style.css, js/*",
                            style = MaterialTheme.typography.labelSmall,
                            color = NebulaGray
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showInstructions = !showInstructions },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("web_github_help_button")
                        ) {
                            Text(
                                text = if (showInstructions) "Masquer Guide" else "Guide",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolarGold
                            )
                        }

                        Button(
                            onClick = {
                                runCatching {
                                    saveZipLauncher.launch("noorbis_github_pages.zip")
                                }.onFailure {
                                    val bundled = buildStandaloneHtmlFromAssets(context)
                                    shareTextAsHtml(context, bundled)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = SpaceBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_github_zip_pack_button")
                        ) {
                            Text(
                                text = "📦 Pack .ZIP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
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
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_standalone_html_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Exporter HTML",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ".HTML",
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

                if (statusBanner != null) {
                    Text(
                        text = statusBanner!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldNova,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SpaceSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
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
                                text = "1. Structure prête à la racine : index.html, css/style.css, js/catalog.js, js/audio.js, js/game.js, js/app.js et meditations_matrix.json (avec chemins relatifs sans / ni . au début).",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarlightWhite
                            )
                            Text(
                                text = "2. Cliquez sur « 📦 Pack .ZIP » pour télécharger directement l'archive contenant index.html, css/style.css et le dossier js/, ou exportez le projet depuis AI Studio vers GitHub.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StarlightWhite
                            )
                            Text(
                                text = "3. Dans votre dépôt GitHub, ouvrez Settings ➔ Pages ➔ Branch : main / (root) ➔ Save.",
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
