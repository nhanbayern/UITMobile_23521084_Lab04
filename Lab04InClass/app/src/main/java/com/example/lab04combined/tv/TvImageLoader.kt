package com.example.lab04combined.tv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class TvImageLoader(private val scope: CoroutineScope) {
    private val cache = ConcurrentHashMap<String, Bitmap>()

    fun load(url: String?, imageView: ImageView) {
        imageView.tag = url
        if (url.isNullOrBlank()) {
            imageView.setImageDrawable(null)
            return
        }

        cache[url]?.let {
            imageView.setImageBitmap(it)
            return
        }

        scope.launch {
            val bitmap = withContext(Dispatchers.IO) { download(url) }
            if (bitmap != null && imageView.tag == url) {
                cache[url] = bitmap
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun download(url: String): Bitmap? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
        }
        return try {
            connection.inputStream.use(BitmapFactory::decodeStream)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }
}
