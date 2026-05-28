package com.example.yamaviolin.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class PracticeSession(
  val id: String = UUID.randomUUID().toString(),
  val date: String, // e.g. "28.05.2026"
  val piece: String,
  val durationMinutes: Int,
  val mood: String, // "Hervorragend", "Gut", "Ok", "Schwer"
  val focusAreas: List<String>,
  val notes: String,
  val audioDurationSeconds: Int = 0 // 0 means no audio recording
)

interface DataRepository {
  val sessions: Flow<List<PracticeSession>>
  fun addSession(session: PracticeSession)
}

class DefaultDataRepository : DataRepository {
  private val _sessions = MutableStateFlow<List<PracticeSession>>(
    listOf(
      PracticeSession(
        date = "28.05.2026",
        piece = "Mendelssohn Violinkonzert in e-Moll, Op. 64",
        durationMinutes = 45,
        mood = "Gut",
        focusAreas = listOf("Intonation", "Bogenführung", "Tempo"),
        notes = "Heute lag der Fokus auf den schnellen Passagen im ersten Satz. Die Intonation ab Takt 120 wird sauberer. Der Bogenarm fühlt sich entspannter an.",
        audioDurationSeconds = 62
      ),
      PracticeSession(
        date = "27.05.2026",
        piece = "Kreutzer Etüde Nr. 2",
        durationMinutes = 20,
        mood = "Ok",
        focusAreas = listOf("Bogenführung", "Fingersatz"),
        notes = "Fokus auf die Legato-Bogenstriche. Einige Übergänge sind noch holprig. Muss morgen langsamer geübt werden.",
        audioDurationSeconds = 0
      ),
      PracticeSession(
        date = "25.05.2026",
        piece = "Schradieck Tonleiterstudien",
        durationMinutes = 15,
        mood = "Hervorragend",
        focusAreas = listOf("Intonation", "Tempo"),
        notes = "Aufwärmübungen in G-Dur und g-Moll. Das Tempo langsam gesteigert. Sehr gleichmäßiger Klang heute.",
        audioDurationSeconds = 35
      )
    )
  )

  override val sessions: Flow<List<PracticeSession>> = _sessions.asStateFlow()

  override fun addSession(session: PracticeSession) {
    _sessions.update { current ->
      listOf(session) + current
    }
  }
}

object RepositoryProvider {
  val repository: DataRepository = DefaultDataRepository()
}

