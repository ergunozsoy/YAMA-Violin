package com.example.yamaviolin.data

import android.util.Log

interface AnalysisEngine {
  fun analyze(
    sessionId: String,
    audioUri: String,
    durationSeconds: Int,
    pieceName: String
  ): List<TimestampedFeedback>
}

object AudioPreAnalyzer : AnalysisEngine {
  private const val TAG = "AudioPreAnalyzer"

  override fun analyze(
    sessionId: String,
    audioUri: String,
    durationSeconds: Int,
    pieceName: String
  ): List<TimestampedFeedback> {
    if (durationSeconds <= 0) return emptyList()

    Log.i(TAG, "Starting audio analysis for session $sessionId, file: $audioUri")

    // Try real audio extraction
    val amplitudes = try {
      AudioFeatureExtractor.extractAmplitudes(audioUri)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to extract amplitudes, using fallback.", e)
      emptyList()
    }

    if (amplitudes.isNotEmpty()) {
      Log.i(TAG, "Audio decoded successfully. Running feature-based analysis.")
      return FeedbackGenerator.generateFeedback(sessionId, amplitudes, durationSeconds)
    } else {
      Log.w(TAG, "Audio decoding failed or returned no data. Generating simulated fallback suggestions.")
      return generateFallbackSuggestions(sessionId, durationSeconds)
    }
  }

  private fun generateFallbackSuggestions(
    sessionId: String,
    durationSeconds: Int
  ): List<TimestampedFeedback> {
    val hints = mutableListOf<TimestampedFeedback>()
    val sourceLabel = "Vorerkennung (simuliert)"

    // 1. Beginning Silence
    if (durationSeconds >= 5) {
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = 0,
          endTimeSeconds = 2.coerceAtMost(durationSeconds),
          category = "Einsatz",
          feedbackType = "Hinweis",
          comment = "Ruhiger Beginn erkannt:\nAm Anfang der Aufnahme ist ein kurzer ruhiger Abschnitt zu hören. Prüfe, ob dies zur Vorbereitung gehört oder ob der Einsatz gezielter beginnen soll.",
          practiceSuggestion = "Höre den Beginn noch einmal mit Kontext an und achte auf einen bewussten, ruhigen Toneinsatz.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = sourceLabel
        )
      )
    }

    // 2. Silence / Pauses: if duration is at least 15s
    if (durationSeconds >= 15) {
      val start = (durationSeconds * 0.15).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 3).coerceAtMost(durationSeconds),
          category = "Übergang",
          feedbackType = "Hinweis",
          comment = "Ruhiger Abschnitt:\nEine kurze Pause oder ein Absinken der Lautstärke scheint an dieser Stelle vorzuliegen. Höre die Stelle noch einmal.",
          practiceSuggestion = "Überprüfe, ob an dieser Übergangsstelle ein bewusster Toneinsatz, Tonanschluss oder ein Ausklingen hilfreich sein könnte (Vorschlag – bitte überprüfen).",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = sourceLabel
        )
      )
    }

    // 3. Sudden volume drop: if duration is at least 20s
    if (durationSeconds >= 20) {
      val start = (durationSeconds * 0.35).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 2).coerceAtMost(durationSeconds),
          category = "Dynamik",
          feedbackType = "Hinweis",
          comment = "Auffällige Dynamikveränderung:\nDie Lautstärke verändert sich hier deutlich. Prüfe, ob diese Veränderung musikalisch beabsichtigt ist.",
          practiceSuggestion = "Wiederhole die Stelle einmal bewusst leise und einmal mit mehr Klang, um den Unterschied zu kontrollieren (Vorschlag – bitte überprüfen).",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = sourceLabel
        )
      )
    }

    // 4. Sudden loud peak: if duration is at least 25s
    if (durationSeconds >= 25) {
      val start = (durationSeconds * 0.55).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 2).coerceAtMost(durationSeconds),
          category = "Toneinsatz",
          feedbackType = "Vorschlag",
          comment = "Starker Toneinsatz:\nEine deutliche Lautstärkespitze scheint hier vorzuliegen. Höre die Stelle noch einmal.",
          practiceSuggestion = "Prüfe, ob der Akzent oder Bogenwechsel an dieser Stelle musikalisch so beabsichtigt ist (Vorschlag – bitte überprüfen).",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = sourceLabel
        )
      )
    }

    // 5. Uneven sound energy: if duration is at least 30s
    if (durationSeconds >= 30) {
      val start = (durationSeconds * 0.75).toInt()
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = (start + 4).coerceAtMost(durationSeconds),
          category = "Klangqualität",
          feedbackType = "Übungsziel",
          comment = "Mögliche Klangschwankung:\nDer Klang wirkt in diesem Abschnitt möglicherweise etwas unruhig. Prüfe, ob Bogenkontakt und Bogengeschwindigkeit gleichmäßig bleiben.",
          practiceSuggestion = "Spiele die Stelle langsam mit ruhigem Bogen und höre danach die Aufnahme erneut mit Kontext an (Vorschlag – bitte überprüfen).",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = sourceLabel
        )
      )
    }

    // 6. End of performance
    if (durationSeconds >= 20) {
      val start = (durationSeconds * 0.90).toInt().coerceAtMost(durationSeconds - 2)
      hints.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = start,
          endTimeSeconds = durationSeconds,
          category = "Selbstkontrolle",
          feedbackType = "Vorschlag",
          comment = "Ende der Einspielung:\nDas Ende der Einspielung eignet sich möglicherweise gut für eine kurze Selbstkontrolle.",
          practiceSuggestion = "Höre die letzten Sekunden noch einmal an und prüfe, ob der Schluss bewusst gestaltet ist (Vorschlag – bitte überprüfen).",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = sourceLabel
        )
      )
    }

    // De-duplicate candidates that start close to each other or overlap
    val spaced = hints.toMutableList()
    var changed = true
    while (changed) {
      changed = false
      var toRemove: TimestampedFeedback? = null
      for (i in 0 until spaced.size) {
        for (j in i + 1 until spaced.size) {
          val s1 = spaced[i]
          val s2 = spaced[j]

          val overlap = Math.max(s1.startTimeSeconds, s2.startTimeSeconds) < Math.min(s1.endTimeSeconds, s2.endTimeSeconds)
          val tooClose = Math.abs(s1.startTimeSeconds - s2.startTimeSeconds) <= 4

          val shouldDeDup = if (durationSeconds < 20) {
            overlap || tooClose
          } else {
            tooClose
          }

          if (shouldDeDup) {
            val p1 = getPriority(s1)
            val p2 = getPriority(s2)
            toRemove = if (p1 >= p2) s2 else s1
            changed = true
            break
          }
        }
        if (changed) break
      }
      if (toRemove != null) {
        spaced.remove(toRemove)
      }
    }

    // Sort and limit
    spaced.sortBy { it.startTimeSeconds }
    val limit = when {
      durationSeconds < 20 -> 2
      durationSeconds <= 60 -> 4
      else -> 6
    }

    val finalFallback = spaced.take(limit)
    Log.d(TAG, "Generated ${finalFallback.size} fallback suggestions.")
    return finalFallback
  }

  private fun getPriority(item: TimestampedFeedback): Int {
    val comment = item.comment
    return when {
      comment.startsWith("Geringe Aufnahmelautstärke") -> 6
      comment.startsWith("Ruhiger Beginn erkannt") -> 5
      comment.startsWith("Ende der Einspielung") -> 4
      comment.startsWith("Auffällige Dynamikveränderung") -> 3
      comment.startsWith("Mögliche Klangschwankung") -> 2
      comment.startsWith("Starker Toneinsatz") -> 2
      else -> 1
    }
  }
}
