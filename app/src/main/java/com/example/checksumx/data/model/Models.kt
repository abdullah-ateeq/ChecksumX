package com.example.checksumx.data.model

import android.net.Uri

data class ChecksumResult(
    val algorithm: ChecksumAlgorithm,
    val hexValue: String,
    val calculationTimeMs: Long,
    val byteCount: Long
)

data class VerificationResult(
    val isMatch: Boolean,
    val algorithm: ChecksumAlgorithm,
    val calculatedChecksum: String,
    val expectedChecksum: String,
    val inputFormatValid: Boolean = true,
    val errorMessage: String? = null
)

data class FileMetadata(
    val uri: Uri,
    val fileName: String,
    val fileSize: Long
)

enum class InputMode {
    TEXT,
    FILE
}

enum class OperationMode(val displayName: String) {
    GENERATE("Generate"),
    VERIFY("Verify")
}

enum class AppTheme(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}
