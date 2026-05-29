package com.example.yamaviolin

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object NewEntry : NavKey
@Serializable data class EntryDetail(val sessionId: String) : NavKey
@Serializable data class ImportPreview(val uriString: String) : NavKey

