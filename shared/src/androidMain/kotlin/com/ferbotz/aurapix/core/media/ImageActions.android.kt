package com.ferbotz.aurapix.core.media

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
actual fun rememberImageActions(): ImageActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context) { AndroidImageActions(context, scope) }
}

private class AndroidImageActions(
    context: Context,
    private val scope: CoroutineScope,
) : ImageActions {
    private val appContext = context.applicationContext

    override fun download(url: String) {
        scope.launch {
            val bytes = fetch(url) ?: return@launch toast("Couldn't download image")
            val saved = withContext(Dispatchers.IO) { saveToGallery(bytes) }
            toast(if (saved) "Saved to gallery" else "Couldn't save image")
        }
    }

    override fun share(url: String) {
        scope.launch {
            val bytes = fetch(url) ?: return@launch toast("Couldn't load image")
            val uri = withContext(Dispatchers.IO) { cacheForShare(bytes) }
                ?: return@launch toast("Couldn't share image")
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            appContext.startActivity(
                Intent.createChooser(send, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun shareLink(url: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        appContext.startActivity(
            Intent.createChooser(send, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private suspend fun fetch(url: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching { URL(url).openStream().use { it.readBytes() } }.getOrNull()
    }

    /** MediaStore insert into Pictures/AuraPix. No permission needed on API 29+. */
    private fun saveToGallery(bytes: ByteArray): Boolean = runCatching {
        val resolver = appContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "AuraPix_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AuraPix")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        true
    }.getOrDefault(false)

    /** Writes to the app's cache and returns a shareable content:// uri via [FileProvider]. */
    private fun cacheForShare(bytes: ByteArray): Uri? = runCatching {
        val dir = File(appContext.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(dir, "AuraPix_${System.currentTimeMillis()}.jpg")
        file.writeBytes(bytes)
        FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
    }.getOrNull()

    private fun toast(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }
}
