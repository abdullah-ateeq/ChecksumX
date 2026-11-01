package com.example.checksumx

import com.example.checksumx.crypto.ChecksumEngine
import com.example.checksumx.data.model.ChecksumAlgorithm
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Random

class ChecksumEngineTest {

    private val standardTestString = "123456789"

    // 1. CRC-8 known test vector
    @Test
    fun testCrc8StandardTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.CRC8)
        // CRC-8 ATM check value for "123456789" is 0xF4
        assertEquals("f4", result.hexValue.lowercase())
    }

    // 2. CRC-16/CCITT-FALSE known test vector
    @Test
    fun testCrc16CcittFalseStandardTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.CRC16_CCITT_FALSE)
        // CRC-16/CCITT-FALSE check value for "123456789" is 0x29B1
        assertEquals("29b1", result.hexValue.lowercase())
    }

    // 3. CRC-32 and CRC-32C test vectors
    @Test
    fun testCrc32StandardTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.CRC32)
        // IEEE 802.3 CRC-32 check value for "123456789" is 0xCBF43926
        assertEquals("cbf43926", result.hexValue.lowercase())
    }

    @Test
    fun testCrc32cStandardTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.CRC32C)
        // CRC-32C (Castagnoli) check value for "123456789" is 0xE3069283
        assertEquals("e3069283", result.hexValue.lowercase())
    }

    // 4. Known MD5, SHA-1, and SHA-256 test vectors
    @Test
    fun testMd5KnownTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.MD5)
        assertEquals("25f9e794323b453885f5181f1b624d0b", result.hexValue.lowercase())
    }

    @Test
    fun testSha1KnownTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.SHA1)
        assertEquals("f7c3bc1d808e04732adf679965ccc34ca7ae3441", result.hexValue.lowercase())
    }

    @Test
    fun testSha256KnownTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.SHA256)
        assertEquals("15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225", result.hexValue.lowercase())
    }

    // 5. SHA-384 and SHA-512 correctness
    @Test
    fun testSha384KnownTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.SHA384)
        assertEquals(
            "eb455d56d2c1a69de64e832011f3393d45f3fa31d6842f21af92d2fe469c499da5e3179847334a18479c8d1dedea1be3",
            result.hexValue.lowercase()
        )
    }

    @Test
    fun testSha512KnownTestVector() {
        val result = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.SHA512)
        assertEquals(
            "d9e6762dd1c8eaf6d61b3c6192fc408d4d6d5f1176d0c29169bc24e71c3f274ad27fcd5811b313d681f7e55ec02d73d499c95455b6b5bb503acf574fba8ffe85",
            result.hexValue.lowercase()
        )
    }

    // 6. Text UTF-8 encoding (multibyte characters, accents, emojis)
    @Test
    fun testUtf8MultiByteEncoding() {
        val utf8Text = "ChecksumX 🚀 安全 verification — été"
        val bytes = utf8Text.toByteArray(Charsets.UTF_8)
        assertTrue(bytes.size > utf8Text.length) // Multi-byte characters expand in UTF-8

        val sha256Text = ChecksumEngine.calculateText(utf8Text, ChecksumAlgorithm.SHA256)
        val streamResult = runBlocking {
            ChecksumEngine.calculateStream(ByteArrayInputStream(bytes), ChecksumAlgorithm.SHA256)
        }
        assertEquals(sha256Text.hexValue, streamResult.hexValue)
    }

    // 7. Empty input handling
    @Test
    fun testEmptyInputHandling() {
        val emptyText = ""
        val crc8 = ChecksumEngine.calculateText(emptyText, ChecksumAlgorithm.CRC8)
        assertEquals("00", crc8.hexValue.lowercase())

        val crc16 = ChecksumEngine.calculateText(emptyText, ChecksumAlgorithm.CRC16_CCITT_FALSE)
        assertEquals("ffff", crc16.hexValue.lowercase())

        val crc32 = ChecksumEngine.calculateText(emptyText, ChecksumAlgorithm.CRC32)
        assertEquals("00000000", crc32.hexValue.lowercase())

        val crc32c = ChecksumEngine.calculateText(emptyText, ChecksumAlgorithm.CRC32C)
        assertEquals("00000000", crc32c.hexValue.lowercase())

        val md5 = ChecksumEngine.calculateText(emptyText, ChecksumAlgorithm.MD5)
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5.hexValue.lowercase())

        val sha1 = ChecksumEngine.calculateText(emptyText, ChecksumAlgorithm.SHA1)
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", sha1.hexValue.lowercase())

        val sha256 = ChecksumEngine.calculateText(emptyText, ChecksumAlgorithm.SHA256)
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha256.hexValue.lowercase())
    }

    // 8. Large-file streaming & consistency
    @Test
    fun testLargeStreamConsistency() = runBlocking {
        // Generate pseudo-random 1 MB payload
        val size = 1024 * 1024
        val data = ByteArray(size)
        Random(42).nextBytes(data)

        // Compare single pass vs batch calculation
        val allAlgorithms = ChecksumAlgorithm.entries
        val batchResults = ChecksumEngine.calculateStreamBatch(
            ByteArrayInputStream(data),
            allAlgorithms
        )

        for (algo in allAlgorithms) {
            val singleResult = ChecksumEngine.calculateStream(
                ByteArrayInputStream(data),
                algo
            )
            assertEquals("Mismatch in stream batch for ${algo.name}", singleResult.hexValue, batchResults[algo]?.hexValue)
            assertEquals(size.toLong(), batchResults[algo]?.byteCount)
        }
    }

    // 9. Case-insensitive hexadecimal comparison
    @Test
    fun testCaseInsensitiveHexComparison() {
        val lower = "15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225"
        val upper = "15E2B0D3C33891EBB0F1EF609EC419420C20E320CE94C65FBC8C3312448EB225"
        val withSpaces = " 15e2b0d3 c33891eb b0f1ef60 9ec41942 0c20e320 ce94c65f bc8c3312 448eb225 "
        val withColons = "15:E2:B0:D3:C3:38:91:EB:B0:F1:EF:60:9E:C4:19:42:0C:20:E3:20:CE:94:C6:5F:BC:8C:33:12:44:8E:B2:25"

        assertTrue(ChecksumEngine.compareChecksums(lower, upper))
        assertTrue(ChecksumEngine.compareChecksums(lower, withSpaces))
        assertTrue(ChecksumEngine.compareChecksums(lower, withColons))
    }

    // 10. Incorrect checksum detection
    @Test
    fun testIncorrectChecksumDetection() {
        val calculated = "15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225"
        val wrong = "15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb220" // last digit 0 instead of 5
        assertFalse(ChecksumEngine.compareChecksums(calculated, wrong))
    }

    // 11. Invalid checksum formats
    @Test
    fun testInvalidChecksumFormats() {
        // Empty
        val emptyVal = ChecksumEngine.validateChecksumFormat("", ChecksumAlgorithm.SHA256)
        assertFalse(emptyVal.isValid)
        assertNotNull(emptyVal.errorMessage)

        // Non-hex characters
        val nonHexVal = ChecksumEngine.validateChecksumFormat(
            "15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb22Z", // 'Z' is not hex
            ChecksumAlgorithm.SHA256
        )
        assertFalse(nonHexVal.isValid)
        assertTrue(nonHexVal.errorMessage?.contains("non-hexadecimal") == true)

        // Invalid length for algorithm (SHA-256 expects 64, provided 32)
        val wrongLengthVal = ChecksumEngine.validateChecksumFormat(
            "25f9e794323b453885f5181f1b624d0b",
            ChecksumAlgorithm.SHA256
        )
        assertFalse(wrongLengthVal.isValid)
        assertTrue(wrongLengthVal.errorMessage?.contains("must be 64 hex characters") == true)

        // Valid SHA-256
        val validVal = ChecksumEngine.validateChecksumFormat(
            "15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225",
            ChecksumAlgorithm.SHA256
        )
        assertTrue(validVal.isValid)
    }

    // 12. Uppercase output setting
    @Test
    fun testUppercaseFormatting() {
        val resultLower = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.CRC32, uppercase = false)
        assertEquals("cbf43926", resultLower.hexValue)

        val resultUpper = ChecksumEngine.calculateText(standardTestString, ChecksumAlgorithm.CRC32, uppercase = true)
        assertEquals("CBF43926", resultUpper.hexValue)
    }

    // 13. Stream error handling
    @Test(expected = IOException::class)
    fun testStreamReadError() = runBlocking {
        val faultyStream = object : InputStream() {
            override fun read(): Int = throw IOException("Simulated I/O disk failure")
        }
        ChecksumEngine.calculateStream(faultyStream, ChecksumAlgorithm.SHA256)
        Unit
    }
}
