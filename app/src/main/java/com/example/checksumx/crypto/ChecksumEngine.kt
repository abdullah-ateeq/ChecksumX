package com.example.checksumx.crypto

import com.example.checksumx.data.model.ChecksumAlgorithm
import com.example.checksumx.data.model.ChecksumResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.CRC32
import kotlin.coroutines.coroutineContext

/**
 * Unified accumulator for computing a checksum or digest incrementally.
 */
interface HashAccumulator {
    val algorithm: ChecksumAlgorithm
    fun update(buffer: ByteArray, offset: Int, length: Int)
    fun finish(uppercase: Boolean): String
    fun reset()
}

/**
 * Table-driven implementation of standard CRC-8.
 * Parameters:
 *  - Polynomial: 0x07 (x⁸ + x² + x + 1)
 *  - Initial value: 0x00
 *  - RefIn: false
 *  - RefOut: false
 *  - XorOut: 0x00
 * Check value for ASCII "123456789": 0xF4
 */
class Crc8Accumulator(override val algorithm: ChecksumAlgorithm = ChecksumAlgorithm.CRC8) : HashAccumulator {
    private var crc: Int = 0x00

    companion object {
        val TABLE = IntArray(256) { i ->
            var curr = i
            for (bit in 0 until 8) {
                curr = if ((curr and 0x80) != 0) {
                    ((curr shl 1) xor 0x07) and 0xFF
                } else {
                    (curr shl 1) and 0xFF
                }
            }
            curr
        }
    }

    override fun update(buffer: ByteArray, offset: Int, length: Int) {
        for (i in offset until (offset + length)) {
            val byteVal = buffer[i].toInt() and 0xFF
            crc = TABLE[crc xor byteVal]
        }
    }

    override fun finish(uppercase: Boolean): String {
        val formatted = "%02x".format(Locale.ROOT, crc and 0xFF)
        return if (uppercase) formatted.uppercase(Locale.ROOT) else formatted
    }

    override fun reset() {
        crc = 0x00
    }
}

/**
 * Table-driven implementation of CRC-16/CCITT-FALSE.
 * Parameters:
 *  - Polynomial: 0x1021 (x¹⁶ + x¹² + x⁵ + 1)
 *  - Initial value: 0xFFFF
 *  - RefIn: false
 *  - RefOut: false
 *  - XorOut: 0x0000
 * Check value for ASCII "123456789": 0x29B1
 */
class Crc16CcittFalseAccumulator(override val algorithm: ChecksumAlgorithm = ChecksumAlgorithm.CRC16_CCITT_FALSE) : HashAccumulator {
    private var crc: Int = 0xFFFF

    companion object {
        val TABLE = IntArray(256) { i ->
            var curr = i shl 8
            for (bit in 0 until 8) {
                curr = if ((curr and 0x8000) != 0) {
                    ((curr shl 1) xor 0x1021) and 0xFFFF
                } else {
                    (curr shl 1) and 0xFFFF
                }
            }
            curr
        }
    }

    override fun update(buffer: ByteArray, offset: Int, length: Int) {
        for (i in offset until (offset + length)) {
            val byteVal = buffer[i].toInt() and 0xFF
            val index = ((crc ushr 8) xor byteVal) and 0xFF
            crc = ((crc shl 8) xor TABLE[index]) and 0xFFFF
        }
    }

    override fun finish(uppercase: Boolean): String {
        val formatted = "%04x".format(Locale.ROOT, crc and 0xFFFF)
        return if (uppercase) formatted.uppercase(Locale.ROOT) else formatted
    }

    override fun reset() {
        crc = 0xFFFF
    }
}

/**
 * Implementation of CRC-32 (IEEE 802.3).
 * Uses standard java.util.zip.CRC32.
 * Check value for ASCII "123456789": 0xCBF43926
 */
class Crc32Accumulator(override val algorithm: ChecksumAlgorithm = ChecksumAlgorithm.CRC32) : HashAccumulator {
    private val crc32 = CRC32()

    override fun update(buffer: ByteArray, offset: Int, length: Int) {
        crc32.update(buffer, offset, length)
    }

    override fun finish(uppercase: Boolean): String {
        val formatted = "%08x".format(Locale.ROOT, crc32.value)
        return if (uppercase) formatted.uppercase(Locale.ROOT) else formatted
    }

    override fun reset() {
        crc32.reset()
    }
}

/**
 * Table-driven implementation of CRC-32C (Castagnoli / RFC 3720).
 * Parameters:
 *  - Polynomial: 0x1EDC6F41 (reversed bit-order: 0x82F63B78)
 *  - Initial value: 0xFFFFFFFF
 *  - RefIn: true
 *  - RefOut: true
 *  - XorOut: 0xFFFFFFFF
 * Check value for ASCII "123456789": 0xE3069283
 */
class Crc32cAccumulator(override val algorithm: ChecksumAlgorithm = ChecksumAlgorithm.CRC32C) : HashAccumulator {
    private var crc: Long = 0xFFFFFFFFL

    companion object {
        val TABLE = LongArray(256) { i ->
            var curr = i.toLong()
            for (bit in 0 until 8) {
                curr = if ((curr and 1L) != 0L) {
                    (curr ushr 1) xor 0x82F63B78L
                } else {
                    curr ushr 1
                }
            }
            curr
        }
    }

    override fun update(buffer: ByteArray, offset: Int, length: Int) {
        for (i in offset until (offset + length)) {
            val byteVal = buffer[i].toLong() and 0xFFL
            val index = ((crc xor byteVal) and 0xFFL).toInt()
            crc = (crc ushr 8) xor TABLE[index]
        }
    }

    override fun finish(uppercase: Boolean): String {
        val finalVal = (crc xor 0xFFFFFFFFL) and 0xFFFFFFFFL
        val formatted = "%08x".format(Locale.ROOT, finalVal)
        return if (uppercase) formatted.uppercase(Locale.ROOT) else formatted
    }

    override fun reset() {
        crc = 0xFFFFFFFFL
    }
}

/**
 * Standard Java MessageDigest accumulator for MD5, SHA-1, SHA-256, SHA-384, and SHA-512.
 */
class MessageDigestAccumulator(override val algorithm: ChecksumAlgorithm) : HashAccumulator {
    private val digest: MessageDigest = MessageDigest.getInstance(algorithm.id)

    override fun update(buffer: ByteArray, offset: Int, length: Int) {
        digest.update(buffer, offset, length)
    }

    override fun finish(uppercase: Boolean): String {
        val bytes = digest.digest()
        val hexChars = CharArray(bytes.size * 2)
        val digits = if (uppercase) "0123456789ABCDEF" else "0123456789abcdef"
        var idx = 0
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            hexChars[idx++] = digits[v ushr 4]
            hexChars[idx++] = digits[v and 0x0F]
        }
        return String(hexChars)
    }

    override fun reset() {
        digest.reset()
    }
}

object ChecksumEngine {
    private const val BUFFER_SIZE = 64 * 1024 // 64 KB buffer for streaming

    fun createAccumulator(algorithm: ChecksumAlgorithm): HashAccumulator {
        return when (algorithm) {
            ChecksumAlgorithm.CRC8 -> Crc8Accumulator()
            ChecksumAlgorithm.CRC16_CCITT_FALSE -> Crc16CcittFalseAccumulator()
            ChecksumAlgorithm.CRC32 -> Crc32Accumulator()
            ChecksumAlgorithm.CRC32C -> Crc32cAccumulator()
            ChecksumAlgorithm.MD5,
            ChecksumAlgorithm.SHA1,
            ChecksumAlgorithm.SHA256,
            ChecksumAlgorithm.SHA384,
            ChecksumAlgorithm.SHA512 -> MessageDigestAccumulator(algorithm)
        }
    }

    /**
     * Compute checksum for UTF-8 encoded string.
     */
    fun calculateText(
        text: String,
        algorithm: ChecksumAlgorithm,
        uppercase: Boolean = false
    ): ChecksumResult {
        val startTime = System.currentTimeMillis()
        val bytes = text.toByteArray(Charsets.UTF_8)
        val accumulator = createAccumulator(algorithm)
        if (bytes.isNotEmpty()) {
            accumulator.update(bytes, 0, bytes.size)
        }
        val hex = accumulator.finish(uppercase)
        val duration = System.currentTimeMillis() - startTime
        return ChecksumResult(
            algorithm = algorithm,
            hexValue = hex,
            calculationTimeMs = duration,
            byteCount = bytes.size.toLong()
        )
    }

    /**
     * Compute all or multiple checksums in a single pass over UTF-8 text.
     */
    fun calculateTextBatch(
        text: String,
        algorithms: List<ChecksumAlgorithm> = ChecksumAlgorithm.entries,
        uppercase: Boolean = false
    ): Map<ChecksumAlgorithm, ChecksumResult> {
        val startTime = System.currentTimeMillis()
        val bytes = text.toByteArray(Charsets.UTF_8)
        val accumulators = algorithms.map { createAccumulator(it) }

        if (bytes.isNotEmpty()) {
            for (acc in accumulators) {
                acc.update(bytes, 0, bytes.size)
            }
        }

        val duration = System.currentTimeMillis() - startTime
        return accumulators.associate { acc ->
            val hex = acc.finish(uppercase)
            acc.algorithm to ChecksumResult(
                algorithm = acc.algorithm,
                hexValue = hex,
                calculationTimeMs = duration,
                byteCount = bytes.size.toLong()
            )
        }
    }

    /**
     * Stream an InputStream and compute checksums for multiple algorithms simultaneously.
     * Efficient single-pass buffered stream reading with cooperative cancellation support.
     */
    suspend fun calculateStreamBatch(
        inputStream: InputStream,
        algorithms: List<ChecksumAlgorithm>,
        uppercase: Boolean = false,
        onProgress: ((bytesRead: Long) -> Unit)? = null
    ): Map<ChecksumAlgorithm, ChecksumResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val accumulators = algorithms.map { createAccumulator(it) }
        val buffer = ByteArray(BUFFER_SIZE)
        var totalBytesRead = 0L

        inputStream.use { stream ->
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                coroutineContext.ensureActive()
                if (bytesRead > 0) {
                    for (acc in accumulators) {
                        acc.update(buffer, 0, bytesRead)
                    }
                    totalBytesRead += bytesRead
                    onProgress?.invoke(totalBytesRead)
                }
            }
        }

        val duration = System.currentTimeMillis() - startTime
        accumulators.associate { acc ->
            val hex = acc.finish(uppercase)
            acc.algorithm to ChecksumResult(
                algorithm = acc.algorithm,
                hexValue = hex,
                calculationTimeMs = duration,
                byteCount = totalBytesRead
            )
        }
    }

    /**
     * Stream an InputStream and compute a single algorithm checksum.
     */
    suspend fun calculateStream(
        inputStream: InputStream,
        algorithm: ChecksumAlgorithm,
        uppercase: Boolean = false,
        onProgress: ((bytesRead: Long) -> Unit)? = null
    ): ChecksumResult = withContext(Dispatchers.IO) {
        val batch = calculateStreamBatch(
            inputStream = inputStream,
            algorithms = listOf(algorithm),
            uppercase = uppercase,
            onProgress = onProgress
        )
        batch.getValue(algorithm)
    }

    /**
     * Clean and normalize a hex checksum string (strip whitespace/colons).
     */
    fun cleanChecksum(input: String): String {
        return input.trim()
            .replace(" ", "")
            .replace(":", "")
            .replace("-", "")
    }

    /**
     * Validate whether expected checksum has valid hex format and matches expected algorithm length.
     */
    fun validateChecksumFormat(expectedHex: String, algorithm: ChecksumAlgorithm): ChecksumValidation {
        val cleaned = cleanChecksum(expectedHex)
        if (cleaned.isEmpty()) {
            return ChecksumValidation(
                isValid = false,
                cleanedHex = "",
                errorMessage = "Expected checksum cannot be empty"
            )
        }

        val isHex = cleaned.all { c ->
            (c in '0'..'9') || (c in 'a'..'f') || (c in 'A'..'F')
        }

        if (!isHex) {
            return ChecksumValidation(
                isValid = false,
                cleanedHex = cleaned,
                errorMessage = "Expected checksum contains non-hexadecimal characters"
            )
        }

        if (cleaned.length != algorithm.hexLength) {
            return ChecksumValidation(
                isValid = false,
                cleanedHex = cleaned,
                errorMessage = "Expected ${algorithm.displayName} checksum must be ${algorithm.hexLength} hex characters (${cleaned.length} provided)"
            )
        }

        return ChecksumValidation(
            isValid = true,
            cleanedHex = cleaned,
            errorMessage = null
        )
    }

    /**
     * Case-insensitive comparison of calculated and expected checksum strings.
     */
    fun compareChecksums(calculated: String, expected: String): Boolean {
        val c = cleanChecksum(calculated)
        val e = cleanChecksum(expected)
        return c.equals(e, ignoreCase = true)
    }
}

data class ChecksumValidation(
    val isValid: Boolean,
    val cleanedHex: String,
    val errorMessage: String?
)
