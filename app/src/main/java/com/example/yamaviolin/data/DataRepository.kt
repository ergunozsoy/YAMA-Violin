package com.example.yamaviolin.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class TimestampedFeedback(
  val id: String = UUID.randomUUID().toString(),
  val sessionId: String,
  val startTimeSeconds: Int,
  val endTimeSeconds: Int,
  val category: String, // Intonation, Bogenführung, Lagenwechsel, Klangqualität, Vibrato, Rhythmus/Tempo, Musikalischer Ausdruck, Aufnahmequalität
  val feedbackType: String, // Stärke, Hinweis, Problem, Übungsziel
  val comment: String,
  val practiceSuggestion: String
)

data class PracticeSession(
  val id: String = UUID.randomUUID().toString(),
  val date: String, // e.g. "28.05.2026"
  val piece: String,
  val durationMinutes: Int,
  val mood: String, // "Hervorragend", "Gut", "Ok", "Schwer"
  val focusAreas: List<String>,
  val notes: String,
  val audioDurationSeconds: Int = 0, // 0 means no audio recording
  val feedbackItems: List<TimestampedFeedback> = emptyList(),
  val autoHints: List<String> = emptyList(),
  val isImported: Boolean = false
)

interface DataRepository {
  val sessions: Flow<List<PracticeSession>>
  fun addSession(session: PracticeSession)
  fun addFeedbackToSession(sessionId: String, feedback: TimestampedFeedback)
}

class DefaultDataRepository : DataRepository {
  private val _sessions = MutableStateFlow<List<PracticeSession>>(emptyList())

  init {
    val mendelssohnId = UUID.randomUUID().toString()
    val schradieckId = UUID.randomUUID().toString()
    val kreutzerId = UUID.randomUUID().toString()

    _sessions.value = listOf(
      PracticeSession(
        id = mendelssohnId,
        date = "28.05.2026",
        piece = "Mendelssohn Violinkonzert in e-Moll, Op. 64",
        durationMinutes = 45,
        mood = "Gut",
        focusAreas = listOf("Intonation", "Bogenführung", "Tempo"),
        notes = "Heute lag der Fokus auf den schnellen Passagen im ersten Satz. Die Intonation ab Takt 120 wird sauberer. Der Bogenarm fühlt sich entspannter an.",
        audioDurationSeconds = 62,
        feedbackItems = listOf(
          TimestampedFeedback(
            sessionId = mendelssohnId,
            startTimeSeconds = 12,
            endTimeSeconds = 18,
            category = "Intonation",
            feedbackType = "Hinweis",
            comment = "Die Intonation bei den Quartsprüngen im Hauptthema wirkt an dieser Stelle möglicherweise etwas ungenau.",
            practiceSuggestion = "Es wird empfohlen, diese Intervalle langsam und mit leerer Saite als Referenz zu üben."
          ),
          TimestampedFeedback(
            sessionId = mendelssohnId,
            startTimeSeconds = 37,
            endTimeSeconds = 42,
            category = "Bogenführung",
            feedbackType = "Problem",
            comment = "Der Bogenstrich verliert beim schnellen Saitenwechsel möglicherweise etwas an Kontakt und Stabilität.",
            practiceSuggestion = "Ein langsames Üben nahe am Steg mit minimalem Bogengewicht könnte überprüft werden."
          ),
          TimestampedFeedback(
            sessionId = mendelssohnId,
            startTimeSeconds = 50,
            endTimeSeconds = 58,
            category = "Musikalischer Ausdruck",
            feedbackType = "Stärke",
            comment = "Das Phrasierungsgefühl und die Dynamik wirken an dieser Stelle sehr überzeugend gestaltet.",
            practiceSuggestion = "Diese musikalische Linie beibehalten und den Ton weiterhin mit kontrolliertem Vibrato stützen."
          )
        ),
        autoHints = listOf(
          "Automatische Vorerkennung: Die Aufnahme enthält mehrere kurze Pausen. (Vorschlag – bitte überprüfen)",
          "Automatische Vorerkennung: Das Tempo wirkt möglicherweise stellenweise unregelmäßig. (Vorschlag – bitte überprüfen)",
          "Automatische Vorerkennung: Die Lautstärke könnte an einzelnen Stellen überprüft werden. (Vorschlag – bitte überprüfen)",
          "Manuelle Bewertung empfohlen – Bitte kontrolliere die detaillierten Zeitmarken."
        )
      ),
      PracticeSession(
        id = kreutzerId,
        date = "27.05.2026",
        piece = "Kreutzer Etüde Nr. 2",
        durationMinutes = 20,
        mood = "Ok",
        focusAreas = listOf("Bogenführung", "Fingersatz"),
        notes = "Fokus auf die Legato-Bogenstriche. Einige Übergänge sind noch holprig. Muss morgen langsamer geübt werden.",
        audioDurationSeconds = 0,
        feedbackItems = emptyList(),
        autoHints = listOf(
          "Keine Audioaufnahme für diese Einheit vorhanden. Manuelle Bewertung empfohlen."
        )
      ),
      PracticeSession(
        id = schradieckId,
        date = "25.05.2026",
        piece = "Schradieck Tonleiterstudien",
        durationMinutes = 15,
        mood = "Hervorragend",
        focusAreas = listOf("Intonation", "Tempo"),
        notes = "Aufwärmübungen in G-Dur und g-Moll. Das Tempo langsam gesteigert. Sehr gleichmäßiger Klang heute.",
        audioDurationSeconds = 35,
        feedbackItems = listOf(
          TimestampedFeedback(
            sessionId = schradieckId,
            startTimeSeconds = 5,
            endTimeSeconds = 12,
            category = "Rhythmus/Tempo",
            feedbackType = "Stärke",
            comment = "Die Gleichmäßigkeit der Sechzehntelnoten wirkt an dieser Stelle bereits sehr stabil.",
            practiceSuggestion = "Das gleichmäßige Timing beibehalten. Ein metronomgesteuertes langsames Steigern des Tempos ist empfohlen."
          ),
          TimestampedFeedback(
            sessionId = schradieckId,
            startTimeSeconds = 20,
            endTimeSeconds = 28,
            category = "Klangqualität",
            feedbackType = "Hinweis",
            comment = "Der Ton wirkt in den höheren Lagen möglicherweise etwas gepresst.",
            practiceSuggestion = "Achte auf eine entspannte Schulterhaltung und verringere das Bogengewicht bei schnellen Noten."
          )
        ),
        autoHints = listOf(
          "Automatische Vorerkennung: Keine kritischen Auffälligkeiten erkannt.",
          "Automatische Vorerkennung: Diese Aufnahme eignet sich gut für eine manuelle Selbstbewertung."
        )
      )
    )
  }

  override val sessions: Flow<List<PracticeSession>> = _sessions.asStateFlow()

  override fun addSession(session: PracticeSession) {
    _sessions.update { current ->
      listOf(session) + current
    }
  }

  override fun addFeedbackToSession(sessionId: String, feedback: TimestampedFeedback) {
    _sessions.update { current ->
      current.map { session ->
        if (session.id == sessionId) {
          session.copy(feedbackItems = session.feedbackItems + feedback)
        } else {
          session
        }
      }
    }
  }
}

object RepositoryProvider {
  val repository: DataRepository = DefaultDataRepository()
}
