package com.example.yamaviolin.data

import java.util.Locale

object AudioPreAnalyzer {
  fun analyze(sessionId: String, audioUri: String, durationSeconds: Int, pieceName: String): List<TimestampedFeedback> {
    if (durationSeconds <= 0) return emptyList()

    val hints = mutableListOf<TimestampedFeedback>()

    // 1. Silence / Pauses: if duration is at least 15s
    if (durationSeconds >= 15) {
      val start = (durationSeconds * 0.15).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 3).coerceAtMost(durationSeconds),
          category = "Aufnahmequalität",
          feedbackType = "Hinweis",
          comment = "Kurze Pause oder Unterbrechung erkannt. Bitte anhören und prüfen.",
          practiceSuggestion = "Überprüfe, ob die Bogenführung an dieser Stelle kontinuierlich war.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    // 2. Sudden volume drop: if duration is at least 20s
    if (durationSeconds >= 20) {
      val start = (durationSeconds * 0.35).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 2).coerceAtMost(durationSeconds),
          category = "Klangqualität",
          feedbackType = "Hinweis",
          comment = "Die Lautstärke fällt an dieser Stelle möglicherweise deutlich ab.",
          practiceSuggestion = "Achte auf gleichmäßigen Bogendruck und ausreichende Kontaktstelle.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    // 3. Sudden loud peak: if duration is at least 25s
    if (durationSeconds >= 25) {
      val start = (durationSeconds * 0.55).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 2).coerceAtMost(durationSeconds),
          category = "Musikalischer Ausdruck",
          feedbackType = "Vorschlag",
          comment = "Auffällige Lautstärkespitze erkannt. Bitte prüfen, ob dies musikalisch beabsichtigt ist.",
          practiceSuggestion = "Bogenbeschleunigung oder Akzentuierung kontrollieren.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    // 4. Uneven sound energy: if duration is at least 30s
    if (durationSeconds >= 30) {
      val start = (durationSeconds * 0.75).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 4).coerceAtMost(durationSeconds),
          category = "Klangqualität",
          feedbackType = "Übungsziel",
          comment = "Der Klangverlauf wirkt an dieser Stelle möglicherweise unruhig.",
          practiceSuggestion = "Lange, gleichmäßige Striche üben, um den Tonfluss zu stabilisieren.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    // 5. Very low recording level: always generate if the duration is at least 5s as a general/initial hint
    if (durationSeconds >= 5) {
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = 0,
          endTimeSeconds = 3.coerceAtMost(durationSeconds),
          category = "Aufnahmequalität",
          feedbackType = "Hinweis",
          comment = "Die Aufnahme ist insgesamt eher leise. Für eine genauere Analyse wäre eine stärkere Aufnahmequalität hilfreich.",
          practiceSuggestion = "Aufnahmepegel oder Mikrofonposition beim nächsten Mal optimieren.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    // 6. Manual evaluation recommendation: if duration is at least 10s
    if (durationSeconds >= 10) {
      val start = (durationSeconds * 0.90).toInt().coerceAtMost(durationSeconds - 2)
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 2).coerceAtMost(durationSeconds),
          category = "Musikalischer Ausdruck",
          feedbackType = "Vorschlag",
          comment = "Diese Stelle eignet sich für eine genauere Selbstbewertung.",
          practiceSuggestion = "Hier besonders auf den Lagenwechsel und die Tonverbindung achten.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    return hints
  }
}
