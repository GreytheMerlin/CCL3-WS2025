package com.example.snorly.feature.challenges.qr

import androidx.compose.runtime.Immutable

@Immutable
data class QrChallengeUiState(
    val hasPermission: Boolean = false,
    val denied: Boolean = false,

    val expectedValue: String? = null,
    val scannedValue: String? = null,

    val success: Boolean = false,
    val showWrongQr: Boolean = false
)
