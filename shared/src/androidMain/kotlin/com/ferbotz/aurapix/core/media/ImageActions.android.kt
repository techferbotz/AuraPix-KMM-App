package com.ferbotz.aurapix.core.media

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream
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
            startShare(uri, text = null)
        }
    }

    override suspend fun shareWithImage(text: String, imageUrl: String?) {
        val bytes = imageUrl?.let { fetch(it) }
        val uri = bytes?.let { withContext(Dispatchers.IO) { cacheForShare(it) } }
        startShare(uri, text)
    }

    /** Opens the share sheet with the cached image at [imageUri] (if any) and [text] as its caption. */
    private fun startShare(imageUri: Uri?, text: String?) {
        val send = Intent(Intent.ACTION_SEND).apply {
            if (imageUri != null) {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
            if (text != null) putExtra(Intent.EXTRA_TEXT, text)
        }
        appContext.startActivity(
            Intent.createChooser(send, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private suspend fun fetch(url: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            // Bounded, so a dead connection fails the action instead of leaving it hanging.
            URL(url).openConnection().run {
                connectTimeout = 10_000
                readTimeout = 15_000
                getInputStream().use { it.readBytes() }
            }
        }.getOrNull()
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

    /**
     * Writes the image to the app's cache as JPEG and returns a shareable content:// uri via
     * [FileProvider]. The API serves WebP, which not every share target accepts; JPEG goes anywhere.
     */
    private fun cacheForShare(bytes: ByteArray): Uri? = runCatching {
        val dir = File(appContext.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(dir, "AuraPix_${System.currentTimeMillis()}.jpg")
        file.writeBytes(toJpeg(bytes))
        FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
    }.getOrNull()

    /** [bytes] as JPEG: passed through when they already are one, re-encoded otherwise. */
    private fun toJpeg(bytes: ByteArray): ByteArray {
        val isJpeg = bytes.size > 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
        if (isJpeg) return bytes
        val bitmap = checkNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)) { "Not an image" }
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    private fun toast(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }
}
