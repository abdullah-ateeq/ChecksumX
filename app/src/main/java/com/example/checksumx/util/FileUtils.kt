package com.example.checksumx.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.checksumx.data.model.FileMetadata
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    fun getFileMetadata(context: Context, uri: Uri): FileMetadata {
        var name = "Unknown file"
        var size = -1L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (cursor.moveToFirst()) {
                    if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                        name = cursor.getString(nameIndex)
                    }
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback if cursor query fails
        }

        if (name == "Unknown file") {
            uri.lastPathSegment?.let { segment ->
                val lastSlash = segment.lastIndexOf('/')
                name = if (lastSlash != -1) segment.substring(lastSlash + 1) else segment
            }
        }

        return FileMetadata(
            uri = uri,
            fileName = name,
            fileSize = size
        )
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes < 0) return "Unknown size"
        if (bytes == 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val formatted = DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble()))
        return "$formatted ${units[digitGroups]}"
    }
}
