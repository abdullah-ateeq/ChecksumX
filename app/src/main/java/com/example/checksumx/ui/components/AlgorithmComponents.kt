package com.example.checksumx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.checksumx.data.model.AlgorithmCategory
import com.example.checksumx.data.model.ChecksumAlgorithm
import com.example.checksumx.ui.theme.LegacyAmberDark
import com.example.checksumx.ui.theme.LegacyAmberLight

@Composable
fun AlgorithmCategoryBadge(
    category: AlgorithmCategory,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (category) {
        AlgorithmCategory.CRC -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "CRC"
        )
        AlgorithmCategory.CRYPTOGRAPHIC_MODERN -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Secure Hash"
        )
        AlgorithmCategory.CRYPTOGRAPHIC_LEGACY -> Triple(
            LegacyAmberLight.copy(alpha = 0.2f),
            LegacyAmberDark,
            "Legacy"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AlgorithmSelector(
    selectedAlgorithm: ChecksumAlgorithm,
    onAlgorithmSelected: (ChecksumAlgorithm) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Algorithm",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Algorithm Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedAlgorithm.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AlgorithmCategoryBadge(category = selectedAlgorithm.category)
                        }
                        Text(
                            text = "${selectedAlgorithm.bitLength}-bit (${selectedAlgorithm.hexLength} hex chars)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand algorithm selector"
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                // Modern Cryptographic Hashes Section
                DropdownHeader("MODERN CRYPTOGRAPHIC HASHES")
                ChecksumAlgorithm.entries.filter { it.category == AlgorithmCategory.CRYPTOGRAPHIC_MODERN }.forEach { algo ->
                    AlgorithmDropdownRow(
                        algorithm = algo,
                        isSelected = algo == selectedAlgorithm,
                        onClick = {
                            onAlgorithmSelected(algo)
                            expanded = false
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // CRC Checksums Section
                DropdownHeader("CRC CHECKSUMS (ERROR DETECTION)")
                ChecksumAlgorithm.entries.filter { it.category == AlgorithmCategory.CRC }.forEach { algo ->
                    AlgorithmDropdownRow(
                        algorithm = algo,
                        isSelected = algo == selectedAlgorithm,
                        onClick = {
                            onAlgorithmSelected(algo)
                            expanded = false
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Legacy Hashes Section
                DropdownHeader("LEGACY HASHES (NOT FOR SECURITY)")
                ChecksumAlgorithm.entries.filter { it.category == AlgorithmCategory.CRYPTOGRAPHIC_LEGACY }.forEach { algo ->
                    AlgorithmDropdownRow(
                        algorithm = algo,
                        isSelected = algo == selectedAlgorithm,
                        onClick = {
                            onAlgorithmSelected(algo)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Show algorithm info dialog
        if (showInfoDialog) {
            AlgorithmInfoDialog(
                algorithm = selectedAlgorithm,
                onDismiss = { showInfoDialog = false }
            )
        }
    }
}

@Composable
private fun DropdownHeader(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun AlgorithmDropdownRow(
    algorithm: ChecksumAlgorithm,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = algorithm.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AlgorithmCategoryBadge(category = algorithm.category)
                    }
                    Text(
                        text = "${algorithm.hexLength} hex chars (${algorithm.bitLength} bits)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        onClick = onClick,
        modifier = Modifier.background(
            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else Color.Transparent
        )
    )
}

@Composable
fun AlgorithmInfoDialog(
    algorithm: ChecksumAlgorithm,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = algorithm.displayName)
                Spacer(modifier = Modifier.width(8.dp))
                AlgorithmCategoryBadge(category = algorithm.category)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Technical Specification:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = algorithm.technicalDetails,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                algorithm.securityWarning?.let { warning ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Security Note",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Text(
                    text = "Expected output length: ${algorithm.hexLength} hexadecimal digits (${algorithm.bitLength} bits)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
