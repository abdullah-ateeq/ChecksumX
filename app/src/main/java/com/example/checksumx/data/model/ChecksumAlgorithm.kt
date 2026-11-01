package com.example.checksumx.data.model

/**
 * Supported checksum and cryptographic hash algorithms.
 * Each algorithm is documented with its technical parameters and security standing.
 */
enum class AlgorithmCategory {
    CRC,
    CRYPTOGRAPHIC_LEGACY,
    CRYPTOGRAPHIC_MODERN
}

enum class ChecksumAlgorithm(
    val id: String,
    val displayName: String,
    val category: AlgorithmCategory,
    val hexLength: Int,
    val bitLength: Int,
    val technicalDetails: String,
    val securityWarning: String? = null
) {
    CRC8(
        id = "CRC-8",
        displayName = "CRC-8",
        category = AlgorithmCategory.CRC,
        hexLength = 2,
        bitLength = 8,
        technicalDetails = "Polynomial: 0x07 (x⁸ + x² + x + 1), Init: 0x00, RefIn: false, RefOut: false, XorOut: 0x00 (Standard ATM / SMBus).",
        securityWarning = "CRC is an error-detection code, not a cryptographic hash. It provides no protection against intentional tampering or collision attacks."
    ),
    CRC16_CCITT_FALSE(
        id = "CRC-16/CCITT-FALSE",
        displayName = "CRC-16 / CCITT-FALSE",
        category = AlgorithmCategory.CRC,
        hexLength = 4,
        bitLength = 16,
        technicalDetails = "Polynomial: 0x1021 (x¹⁶ + x¹² + x⁵ + 1), Init: 0xFFFF, RefIn: false, RefOut: false, XorOut: 0x0000 (Telecommunications / Autosar).",
        securityWarning = "CRC is an error-detection code, not a cryptographic hash. It provides no protection against intentional tampering."
    ),
    CRC32(
        id = "CRC-32",
        displayName = "CRC-32 (IEEE 802.3)",
        category = AlgorithmCategory.CRC,
        hexLength = 8,
        bitLength = 32,
        technicalDetails = "Polynomial: 0x04C11DB7 (reversed: 0xEDB88320), Init: 0xFFFFFFFF, RefIn: true, RefOut: true, XorOut: 0xFFFFFFFF (Ethernet, ZIP, PNG).",
        securityWarning = "CRC is an error-detection code designed solely for detecting accidental data corruption, not malicious alteration."
    ),
    CRC32C(
        id = "CRC-32C",
        displayName = "CRC-32C (Castagnoli)",
        category = AlgorithmCategory.CRC,
        hexLength = 8,
        bitLength = 32,
        technicalDetails = "Polynomial: 0x1EDC6F41 (reversed: 0x82F63B78), Init: 0xFFFFFFFF, RefIn: true, RefOut: true, XorOut: 0xFFFFFFFF (iSCSI, SCTP, ext4, Btrfs).",
        securityWarning = "CRC is an error-detection code designed solely for detecting accidental data corruption, not malicious alteration."
    ),
    MD5(
        id = "MD5",
        displayName = "MD5",
        category = AlgorithmCategory.CRYPTOGRAPHIC_LEGACY,
        hexLength = 32,
        bitLength = 128,
        technicalDetails = "128-bit cryptographic message digest (RFC 1321).",
        securityWarning = "LEGACY ALGORITHM: MD5 is cryptographically broken and vulnerable to practical collision attacks. Unsuitable for security-sensitive integrity verification."
    ),
    SHA1(
        id = "SHA-1",
        displayName = "SHA-1",
        category = AlgorithmCategory.CRYPTOGRAPHIC_LEGACY,
        hexLength = 40,
        bitLength = 160,
        technicalDetails = "160-bit cryptographic hash function (FIPS PUB 180-4).",
        securityWarning = "LEGACY ALGORITHM: SHA-1 is cryptographically broken with demonstrated collision attacks. Deprecated for security-sensitive integrity verification."
    ),
    SHA256(
        id = "SHA-256",
        displayName = "SHA-256",
        category = AlgorithmCategory.CRYPTOGRAPHIC_MODERN,
        hexLength = 64,
        bitLength = 256,
        technicalDetails = "256-bit member of SHA-2 family (FIPS PUB 180-4). Standard for secure file and software distribution integrity verification."
    ),
    SHA384(
        id = "SHA-384",
        displayName = "SHA-384",
        category = AlgorithmCategory.CRYPTOGRAPHIC_MODERN,
        hexLength = 96,
        bitLength = 384,
        technicalDetails = "384-bit truncated variant of SHA-512 (FIPS PUB 180-4). High security margin for sensitive verification."
    ),
    SHA512(
        id = "SHA-512",
        displayName = "SHA-512",
        category = AlgorithmCategory.CRYPTOGRAPHIC_MODERN,
        hexLength = 128,
        bitLength = 512,
        technicalDetails = "512-bit member of SHA-2 family (FIPS PUB 180-4). Maximum strength message digest."
    );

    val isLegacy: Boolean get() = category == AlgorithmCategory.CRYPTOGRAPHIC_LEGACY
    val isCrc: Boolean get() = category == AlgorithmCategory.CRC
    val isCryptographic: Boolean get() = category != AlgorithmCategory.CRC
}
