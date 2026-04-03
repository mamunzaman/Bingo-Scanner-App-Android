package com.example.mamunbingoapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mamunbingoapp.history.HistoryOcrSource
import com.example.mamunbingoapp.scanner.BingoNumberAnalyzer
import com.example.mamunbingoapp.scanner.ImportTicketImageOcr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ScanResultUiState {
    data object Idle : ScanResultUiState()
    data object Loading : ScanResultUiState()
    /** Prefill from live-scan route args or on-device ML Kit OCR. */
    data class Success(
        val numbers: List<Int>,
        val losNumber: String? = null,
        val serialNumber: String? = null,
        val ocrSource: HistoryOcrSource? = null,
    ) : ScanResultUiState()
    data class Error(val message: String) : ScanResultUiState()
}

/** URI and scan state for `historyPhotoImport` and scan-tab import. */
class ImportTicketViewModel : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResultUiState>(ScanResultUiState.Idle)
    val scanResult: StateFlow<ScanResultUiState> = _scanResult.asStateFlow()

    private val _candidateNumbers = MutableStateFlow<List<Int>>(emptyList())
    val candidateNumbers: StateFlow<List<Int>> = _candidateNumbers.asStateFlow()

    private val _candidateBuckets = MutableStateFlow<Map<String, List<Int>>>(emptyMap())
    val candidateBuckets: StateFlow<Map<String, List<Int>>> = _candidateBuckets.asStateFlow()

    private val _prefilledSource = MutableStateFlow<String?>(null)
    val prefilledSource: StateFlow<String?> = _prefilledSource.asStateFlow()

    private val _prefilledConfidence = MutableStateFlow<Float?>(null)
    val prefilledConfidence: StateFlow<Float?> = _prefilledConfidence.asStateFlow()

    private var analysisJob: Job? = null

    fun onPhotoTaken(uri: Uri) {
        _selectedImageUri.value = uri
        _candidateNumbers.value = emptyList()
        _candidateBuckets.value = emptyMap()
        _scanResult.value = ScanResultUiState.Idle
    }

    /**
     * Runs on-device ML Kit text recognition and maps results into [ScanResultUiState.Success]
     * (row-major numbers for manual entry when [HistoryPhotoImport] uses row-major prefill).
     */
    fun analyzeTicketFromUri(context: Context, uri: Uri) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _scanResult.value = ScanResultUiState.Loading
            val result = withContext(Dispatchers.IO) {
                runCatching { ImportTicketImageOcr.analyzeUri(context.applicationContext, uri) }
            }
            if (uri != _selectedImageUri.value) return@launch
            result.fold(
                onSuccess = { outcome ->
                    val nums = outcome.numbersRowMajor.filter { it != 0 }
                    if (nums.isEmpty()) {
                        _scanResult.value = ScanResultUiState.Error("No bingo numbers detected")
                    } else {
                        setScanResult(
                            ScanResultUiState.Success(
                                numbers = outcome.numbersRowMajor,
                                losNumber = outcome.losNumber?.takeIf { it.isNotBlank() },
                                serialNumber = outcome.serialNumber?.takeIf { it.isNotBlank() },
                                ocrSource = HistoryOcrSource.ML_KIT,
                            ),
                        )
                    }
                },
                onFailure = { e ->
                    _scanResult.value = ScanResultUiState.Error(e.message ?: "OCR failed")
                },
            )
        }
    }

    fun setScanResult(state: ScanResultUiState) {
        if (state is ScanResultUiState.Success) {
            setCandidateNumbers(state.numbers.filter { it != 0 })
        }
        _scanResult.value = state
    }

    fun setCandidateNumbers(values: List<Int>) {
        val buckets = BingoNumberAnalyzer.bucketizeCandidateNumbers(values)
        _candidateNumbers.value = buckets.flat
        _candidateBuckets.value = mapOf(
            "B" to buckets.b,
            "I" to buckets.i,
            "N" to buckets.n,
            "G" to buckets.g,
            "O" to buckets.o,
        )
    }

    fun prefillFromLiveScan(
        numbers: List<Int>,
        source: String?,
        confidence: Float?,
        losNumber: String? = null,
        serialNumber: String? = null,
    ) {
        if (numbers.isEmpty()) return
        _selectedImageUri.value = null
        _scanResult.value = ScanResultUiState.Success(
            numbers.take(25),
            losNumber = losNumber?.takeIf { it.isNotBlank() },
            serialNumber = serialNumber?.takeIf { it.isNotBlank() },
            ocrSource = null,
        )
        setCandidateNumbers(numbers)
        _prefilledSource.value = source
        _prefilledConfidence.value = confidence
    }

    fun clear() {
        analysisJob?.cancel()
        analysisJob = null
        _selectedImageUri.value = null
        _scanResult.value = ScanResultUiState.Idle
        _candidateNumbers.value = emptyList()
        _candidateBuckets.value = emptyMap()
        _prefilledSource.value = null
        _prefilledConfidence.value = null
    }

    override fun onCleared() {
        super.onCleared()
        analysisJob?.cancel()
    }
}
