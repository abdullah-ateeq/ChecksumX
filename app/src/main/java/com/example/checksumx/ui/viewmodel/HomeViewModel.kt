package com.example.checksumx.ui.viewmodel

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checksumx.crypto.ChecksumEngine
import com.example.checksumx.data.model.ChecksumAlgorithm
import com.example.checksumx.data.model.ChecksumResult
import com.example.checksumx.data.model.FileMetadata
import com.example.checksumx.data.model.InputMode
import com.example.checksumx.data.model.OperationMode
import com.example.checksumx.data.model.VerificationResult
import com.example.checksumx.data.preferences.PreferencesRepository
import com.example.checksumx.data.preferences.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val operationMode: OperationMode = OperationMode.GENERATE,
    val inputMode: InputMode = InputMode.TEXT,
    val inputText: String = "",
    val selectedFile: FileMetadata? = null,
    val selectedAlgorithm: ChecksumAlgorithm = ChecksumAlgorithm.SHA256,
    val expectedChecksum: String = "",
    val isCalculating: Boolean = false,
    val bytesProcessed: Long = 0L,
    val generatedResult: ChecksumResult? = null,
    val batchResults: Map<ChecksumAlgorithm, ChecksumResult>? = null,
    val verificationResult: VerificationResult? = null,
    val expectedChecksumError: String? = null,
    val generalError: String? = null,
    val infoMessage: String? = null
) {
    val charCount: Int get() = inputText.length
    val byteCount: Int get() = inputText.toByteArray(Charsets.UTF_8).size
}

class HomeViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private var calculationJob: Job? = null
    private var hasInitializedDefaults = false

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collectLatest { prefs ->
                _preferences.value = prefs
                if (!hasInitializedDefaults) {
                    _uiState.update { current ->
                        current.copy(
                            operationMode = prefs.defaultOperation,
                            selectedAlgorithm = prefs.defaultAlgorithm
                        )
                    }
                    hasInitializedDefaults = true
                }
            }
        }
    }

    fun setOperationMode(mode: OperationMode) {
        _uiState.update { it.copy(operationMode = mode, generalError = null) }
    }

    fun setInputMode(mode: InputMode) {
        _uiState.update { it.copy(inputMode = mode, generalError = null) }
    }

    fun setInputText(text: String) {
        _uiState.update { it.copy(inputText = text, generalError = null) }
    }

    fun setSelectedFile(metadata: FileMetadata?) {
        _uiState.update {
            it.copy(
                selectedFile = metadata,
                generatedResult = null,
                batchResults = null,
                verificationResult = null,
                generalError = null
            )
        }
    }

    fun setSelectedAlgorithm(algorithm: ChecksumAlgorithm) {
        _uiState.update { current ->
            val updated = current.copy(selectedAlgorithm = algorithm, generalError = null)
            // Re-validate expected checksum length if user entered one
            if (current.expectedChecksum.isNotBlank()) {
                val validation = ChecksumEngine.validateChecksumFormat(current.expectedChecksum, algorithm)
                updated.copy(expectedChecksumError = validation.errorMessage)
            } else {
                updated.copy(expectedChecksumError = null)
            }
        }
    }

    fun setExpectedChecksum(checksum: String) {
        _uiState.update { current ->
            val validation = if (checksum.isNotBlank()) {
                ChecksumEngine.validateChecksumFormat(checksum, current.selectedAlgorithm)
            } else null

            current.copy(
                expectedChecksum = checksum,
                expectedChecksumError = validation?.errorMessage,
                generalError = null
            )
        }
    }

    fun generateChecksum(contentResolver: ContentResolver) {
        val currentState = _uiState.value
        val uppercase = _preferences.value.uppercaseHex
        val algorithm = currentState.selectedAlgorithm

        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculating = true,
                    bytesProcessed = 0L,
                    generatedResult = null,
                    generalError = null
                )
            }

            try {
                when (currentState.inputMode) {
                    InputMode.TEXT -> {
                        val result = ChecksumEngine.calculateText(
                            text = currentState.inputText,
                            algorithm = algorithm,
                            uppercase = uppercase
                        )
                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                generatedResult = result
                            )
                        }
                    }
                    InputMode.FILE -> {
                        val file = currentState.selectedFile
                        if (file == null) {
                            _uiState.update {
                                it.copy(
                                    isCalculating = false,
                                    generalError = "Please select a file first"
                                )
                            }
                            return@launch
                        }

                        val stream = runCatching {
                            contentResolver.openInputStream(file.uri)
                        }.getOrNull()

                        if (stream == null) {
                            _uiState.update {
                                it.copy(
                                    isCalculating = false,
                                    generalError = "Cannot access selected file: permission denied or file not found"
                                )
                            }
                            return@launch
                        }

                        val result = ChecksumEngine.calculateStream(
                            inputStream = stream,
                            algorithm = algorithm,
                            uppercase = uppercase,
                            onProgress = { bytesRead ->
                                _uiState.update { it.copy(bytesProcessed = bytesRead) }
                            }
                        )

                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                generatedResult = result
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        generalError = "Checksum calculation failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun generateAllChecksums(contentResolver: ContentResolver) {
        val currentState = _uiState.value
        val uppercase = _preferences.value.uppercaseHex
        val allAlgorithms = ChecksumAlgorithm.entries

        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculating = true,
                    bytesProcessed = 0L,
                    batchResults = null,
                    generalError = null
                )
            }

            try {
                when (currentState.inputMode) {
                    InputMode.TEXT -> {
                        val batch = ChecksumEngine.calculateTextBatch(
                            text = currentState.inputText,
                            algorithms = allAlgorithms,
                            uppercase = uppercase
                        )
                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                batchResults = batch,
                                generatedResult = batch[it.selectedAlgorithm]
                            )
                        }
                    }
                    InputMode.FILE -> {
                        val file = currentState.selectedFile
                        if (file == null) {
                            _uiState.update {
                                it.copy(
                                    isCalculating = false,
                                    generalError = "Please select a file first"
                                )
                            }
                            return@launch
                        }

                        val stream = runCatching {
                            contentResolver.openInputStream(file.uri)
                        }.getOrNull()

                        if (stream == null) {
                            _uiState.update {
                                it.copy(
                                    isCalculating = false,
                                    generalError = "Cannot access selected file: permission denied or file not found"
                                )
                            }
                            return@launch
                        }

                        val batch = ChecksumEngine.calculateStreamBatch(
                            inputStream = stream,
                            algorithms = allAlgorithms,
                            uppercase = uppercase,
                            onProgress = { bytesRead ->
                                _uiState.update { it.copy(bytesProcessed = bytesRead) }
                            }
                        )

                        _uiState.update {
                            it.copy(
                                isCalculating = false,
                                batchResults = batch,
                                generatedResult = batch[it.selectedAlgorithm]
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        generalError = "Multi-pass checksum calculation failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun verifyChecksum(contentResolver: ContentResolver) {
        val currentState = _uiState.value
        val uppercase = _preferences.value.uppercaseHex
        val algorithm = currentState.selectedAlgorithm

        // Pre-validate expected checksum
        val validation = ChecksumEngine.validateChecksumFormat(currentState.expectedChecksum, algorithm)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    expectedChecksumError = validation.errorMessage,
                    verificationResult = null
                )
            }
            return
        }

        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCalculating = true,
                    bytesProcessed = 0L,
                    verificationResult = null,
                    generalError = null
                )
            }

            try {
                val calculatedHex: String = when (currentState.inputMode) {
                    InputMode.TEXT -> {
                        ChecksumEngine.calculateText(
                            text = currentState.inputText,
                            algorithm = algorithm,
                            uppercase = uppercase
                        ).hexValue
                    }
                    InputMode.FILE -> {
                        val file = currentState.selectedFile
                        if (file == null) {
                            _uiState.update {
                                it.copy(
                                    isCalculating = false,
                                    generalError = "Please select a file to verify"
                                )
                            }
                            return@launch
                        }

                        val stream = runCatching {
                            contentResolver.openInputStream(file.uri)
                        }.getOrNull()

                        if (stream == null) {
                            _uiState.update {
                                it.copy(
                                    isCalculating = false,
                                    generalError = "Cannot open file: file inaccessible or removed"
                                )
                            }
                            return@launch
                        }

                        ChecksumEngine.calculateStream(
                            inputStream = stream,
                            algorithm = algorithm,
                            uppercase = uppercase,
                            onProgress = { bytesRead ->
                                _uiState.update { it.copy(bytesProcessed = bytesRead) }
                            }
                        ).hexValue
                    }
                }

                val isMatch = ChecksumEngine.compareChecksums(calculatedHex, validation.cleanedHex)

                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        verificationResult = VerificationResult(
                            isMatch = isMatch,
                            algorithm = algorithm,
                            calculatedChecksum = if (uppercase) calculatedHex.uppercase() else calculatedHex.lowercase(),
                            expectedChecksum = if (uppercase) validation.cleanedHex.uppercase() else validation.cleanedHex.lowercase(),
                            inputFormatValid = true,
                            errorMessage = null
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        generalError = "Verification failed: ${e.localizedMessage ?: "Unknown error"}",
                        verificationResult = null // Never report a match when calculation fails!
                    )
                }
            }
        }
    }

    fun clearInputAndResults() {
        calculationJob?.cancel()
        _uiState.update {
            it.copy(
                inputText = "",
                selectedFile = null,
                expectedChecksum = "",
                isCalculating = false,
                bytesProcessed = 0L,
                generatedResult = null,
                batchResults = null,
                verificationResult = null,
                expectedChecksumError = null,
                generalError = null,
                infoMessage = "Input and results cleared"
            )
        }
    }

    fun clearResultsOnly() {
        _uiState.update {
            it.copy(
                generatedResult = null,
                batchResults = null,
                verificationResult = null,
                generalError = null
            )
        }
    }

    fun dismissInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun dismissGeneralError() {
        _uiState.update { it.copy(generalError = null) }
    }

    /**
     * Triggered by activity lifecycle onStop/onPause when clear-on-exit is enabled.
     */
    fun onAppLeaving() {
        if (_preferences.value.clearOnExit) {
            clearInputAndResults()
        }
    }

    class Factory(private val preferencesRepository: PreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
