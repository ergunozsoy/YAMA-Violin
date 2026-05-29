package com.example.yamaviolin.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class TimestampedFeedback(
  val id: String = UUID.randomUUID().toString(),
  val sessionId: String,
  val startTimeSeconds: Int,
  val endTimeSeconds: Int,
  val category: String, // Intonation, Bogenführung, Lagenwechsel, Klangqualität, Vibrato, Rhythmus/Tempo, Musikalischer Ausdruck, Aufnahmequalität
  val feedbackType: String, // Stärke, Hinweis, Problem, Übungsziel
  val comment: String,
  val practiceSuggestion: String,
  val isAutomatic: Boolean = false,
  val canBeIgnored: Boolean = true,
  val canBeAccepted: Boolean = true,
  val isIgnored: Boolean = false,
  val isAccepted: Boolean = false,
  val source: String = "Manuell hinzugefügt"
)

@Serializable
data class PracticeSession(
  val id: String = UUID.randomUUID().toString(),
  val date: String, // e.g. "28.05.2026"
  val piece: String,
  val durationMinutes: Int? = null,
  val mood: String, // "Hervorragend", "Gut", "Ok", "Schwer"
  val focusAreas: List<String>,
  val notes: String,
  val audioDurationSeconds: Int = 0, // 0 means no audio recording
  val feedbackItems: List<TimestampedFeedback> = emptyList(),
  val autoHints: List<String> = emptyList(),
  val isImported: Boolean = false,
  val audioUri: String? = null,
  val audioSource: String? = null, // "Neue Aufnahme" or "Importierte Aufnahme"
  val originalFileName: String? = null,
  val recordingDate: String? = null,
  val importDate: String? = null
)

interface DataRepository {
  val sessions: Flow<List<PracticeSession>>
  fun addSession(session: PracticeSession)
  fun addFeedbackToSession(sessionId: String, feedback: TimestampedFeedback)
  fun acceptFeedback(sessionId: String, feedbackId: String)
  fun ignoreFeedback(sessionId: String, feedbackId: String)
}

class DefaultDataRepository(private val context: android.content.Context) : DataRepository {
  private val file = java.io.File(context.filesDir, "yama_sessions.json")
  private val jsonParser = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    prettyPrint = true
  }

  private val _sessions = MutableStateFlow<List<PracticeSession>>(emptyList())

  init {
    loadSessions()
  }

  private fun loadSessions() {
    try {
      if (file.exists()) {
        val jsonStr = file.readText()
        val list = jsonParser.decodeFromString<List<PracticeSession>>(jsonStr)
        _sessions.value = list
      } else {
        // Load initial mock data
        val initialList = createMockSessions()
        _sessions.value = initialList
        saveSessions(initialList)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      // Fallback to mock data if load fails
      val initialList = createMockSessions()
      _sessions.value = initialList
    }
  }

  private fun saveSessions(list: List<PracticeSession>) {
    try {
      val jsonStr = jsonParser.encodeToString(list)
      file.writeText(jsonStr)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun createMockSessions(): List<PracticeSession> {
    val mendelssohnId = UUID.randomUUID().toString()
    val schradieckId = UUID.randomUUID().toString()
    val kreutzerId = UUID.randomUUID().toString()

    return listOf(
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
    // Generate automatic pre-analysis hints if there is an audio file and no feedback items yet
    val updatedSession = if (!session.audioUri.isNullOrBlank() && session.feedbackItems.isEmpty()) {
      val generatedHints = AudioPreAnalyzer.analyze(session.id, session.audioUri, session.audioDurationSeconds, session.piece)
      session.copy(feedbackItems = generatedHints)
    } else {
      session
    }

    _sessions.update { current ->
      val updatedList = listOf(updatedSession) + current
      saveSessions(updatedList)
      updatedList
    }
  }

  override fun addFeedbackToSession(sessionId: String, feedback: TimestampedFeedback) {
    _sessions.update { current ->
      val updatedList = current.map { session ->
        if (session.id == sessionId) {
          session.copy(feedbackItems = session.feedbackItems + feedback)
        } else {
          session
        }
      }
      saveSessions(updatedList)
      updatedList
    }
  }

  override fun acceptFeedback(sessionId: String, feedbackId: String) {
    _sessions.update { current ->
      val updatedList = current.map { session ->
        if (session.id == sessionId) {
          session.copy(feedbackItems = session.feedbackItems.map { item ->
            if (item.id == feedbackId) {
              item.copy(isAccepted = true, source = "Übernommen aus Analyse")
            } else {
              item
            }
          })
        } else {
          session
        }
      }
      saveSessions(updatedList)
      updatedList
    }
  }

  override fun ignoreFeedback(sessionId: String, feedbackId: String) {
    _sessions.update { current ->
      val updatedList = current.map { session ->
        if (session.id == sessionId) {
          session.copy(feedbackItems = session.feedbackItems.map { item ->
            if (item.id == feedbackId) {
              item.copy(isIgnored = true)
            } else {
              item
            }
          })
        } else {
          session
        }
      }
      saveSessions(updatedList)
      updatedList
    }
  }
}

object RepositoryProvider {
  private var _repository: DataRepository? = null

  val repository: DataRepository
    get() = _repository ?: throw IllegalStateException("RepositoryProvider not initialized! Call initialize(context) in MainActivity.")

  fun initialize(context: android.content.Context) {
    if (_repository == null) {
      _repository = DefaultDataRepository(context.applicationContext)
    }
  }
}
