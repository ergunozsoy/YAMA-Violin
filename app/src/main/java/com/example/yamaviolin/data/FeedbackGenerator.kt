package com.example.yamaviolin.data

import android.util.Log
import kotlin.math.sqrt

object FeedbackGenerator {
  private const val TAG = "FeedbackGenerator"

  fun generateFeedback(
    sessionId: String,
    amplitudes: List<Double>,
    durationSeconds: Int
  ): List<TimestampedFeedback> {
    if (amplitudes.isEmpty() || durationSeconds <= 0) {
      return emptyList()
    }

    val candidates = mutableListOf<TimestampedFeedback>()
    val size = amplitudes.size

    val overallMean = amplitudes.average()
    val isOverallVolumeLow = overallMean < 2500.0

    // 1. Detect Beginning Silence (first few seconds of preparation)
    var begSilenceDuration = 0
    for (amp in amplitudes) {
      if (amp < 500.0) begSilenceDuration++ else break
    }

    var hasBeginningSilence = false
    if (begSilenceDuration > 0) {
      // Rule 6: If overall volume is low, suppress beginning silence unless it is clearly longer than 2 seconds
      if (!isOverallVolumeLow || begSilenceDuration > 2) {
        hasBeginningSilence = true
        candidates.add(
          TimestampedFeedback(
            sessionId = sessionId,
            startTimeSeconds = 0,
            endTimeSeconds = begSilenceDuration.coerceAtMost(durationSeconds),
            category = "Einsatz",
            feedbackType = "Hinweis",
            comment = "Ruhiger Beginn erkannt:\nAm Anfang der Aufnahme ist ein kurzer ruhiger Abschnitt zu hören. Prüfe, ob dies zur Vorbereitung gehört oder ob der Einsatz gezielter beginnen soll.",
            practiceSuggestion = "Höre den Beginn noch einmal mit Kontext an und achte auf einen bewussten, ruhigen Toneinsatz.",
            isAutomatic = true,
            canBeIgnored = true,
            canBeAccepted = true,
            source = "Automatisch erkannt"
          )
        )
      }
    }

    // 2. Detect General Silence / Interruption inside the recording
    // Runs of amplitude < 400.0 for >= 2 seconds (excluding beginning silence if already handled)
    val silenceStartIndex = if (hasBeginningSilence) begSilenceDuration else 0
    var silenceStart = -1
    for (i in silenceStartIndex until size) {
      if (amplitudes[i] < 400.0) {
        if (silenceStart == -1) {
          silenceStart = i
        }
      } else {
        if (silenceStart != -1) {
          val silenceDuration = i - silenceStart
          if (silenceDuration >= 2) {
            candidates.add(
              TimestampedFeedback(
                sessionId = sessionId,
                startTimeSeconds = silenceStart,
                endTimeSeconds = i.coerceAtMost(durationSeconds),
                category = "Übergang",
                feedbackType = "Hinweis",
                comment = "Ruhiger Abschnitt:\nEine kurze Pause oder ein Absinken der Lautstärke scheint an dieser Stelle vorzuliegen. Höre die Stelle noch einmal.",
                practiceSuggestion = "Überprüfe, ob an dieser Übergangsstelle ein bewusster Toneinsatz, Tonanschluss oder ein Ausklingen hilfreich sein könnte (Vorschlag – bitte überprüfen).",
                isAutomatic = true,
                canBeIgnored = true,
                canBeAccepted = true,
                source = "Automatisch erkannt"
              )
            )
          }
          silenceStart = -1
        }
      }
    }
    // Check if silence continues to the end
    if (silenceStart != -1) {
      val silenceDuration = size - silenceStart
      if (silenceDuration >= 2) {
        candidates.add(
          TimestampedFeedback(
            sessionId = sessionId,
            startTimeSeconds = silenceStart,
            endTimeSeconds = durationSeconds,
            category = "Übergang",
            feedbackType = "Hinweis",
            comment = "Ruhiger Abschnitt:\nEine kurze Pause oder ein Absinken der Lautstärke scheint an dieser Stelle vorzuliegen. Höre die Stelle noch einmal.",
            practiceSuggestion = "Überprüfe, ob an dieser Übergangsstelle ein bewusster Toneinsatz, Tonanschluss oder ein Ausklingen hilfreich sein könnte (Vorschlag – bitte überprüfen).",
            isAutomatic = true,
            canBeIgnored = true,
            canBeAccepted = true,
            source = "Automatisch erkannt"
          )
        )
      }
    }

    // 3. Detect Sudden Loudness Changes (excl. silence)
    for (i in 1 until size) {
      val prev = amplitudes[i - 1]
      val curr = amplitudes[i]
      if (prev > 400.0 && curr > 400.0) {
        val diff = Math.abs(curr - prev)
        if (diff > 5500.0) {
          candidates.add(
            TimestampedFeedback(
              sessionId = sessionId,
              startTimeSeconds = (i - 1).coerceAtLeast(0),
              endTimeSeconds = i.coerceAtMost(durationSeconds),
              category = "Dynamik",
              feedbackType = "Hinweis",
              comment = "Auffällige Dynamikveränderung:\nDie Lautstärke verändert sich hier deutlich. Prüfe, ob diese Veränderung musikalisch beabsichtigt ist.",
              practiceSuggestion = "Wiederhole die Stelle einmal bewusst leise und einmal mit mehr Klang, um den Unterschied zu kontrollieren.",
              isAutomatic = true,
              canBeIgnored = true,
              canBeAccepted = true,
              source = "Automatisch erkannt"
            )
          )
        }
      }
    }

    // 4. Detect Unstable Sound (variation is high in a 3s window)
    for (i in 0 until size - 2) {
      val w1 = amplitudes[i]
      val w2 = amplitudes[i + 1]
      val w3 = amplitudes[i + 2]
      val mean = (w1 + w2 + w3) / 3.0
      if (mean in 1500.0..15000.0) {
        val variance = ((w1 - mean) * (w1 - mean) + (w2 - mean) * (w2 - mean) + (w3 - mean) * (w3 - mean)) / 3.0
        val stdDev = sqrt(variance)
        val coefVar = stdDev / mean
        if (coefVar > 0.45) {
          candidates.add(
            TimestampedFeedback(
              sessionId = sessionId,
              startTimeSeconds = i,
              endTimeSeconds = (i + 2).coerceAtMost(durationSeconds),
              category = "Klangqualität",
              feedbackType = "Übungsziel",
              comment = "Mögliche Klangschwankung:\nDer Klang wirkt in diesem Abschnitt möglicherweise etwas unruhig. Prüfe, ob Bogenkontakt und Bogengeschwindigkeit gleichmäßig bleiben.",
              practiceSuggestion = "Spiele die Stelle langsam mit ruhigem Bogen und höre danach die Aufnahme erneut mit Kontext an.",
              isAutomatic = true,
              canBeIgnored = true,
              canBeAccepted = true,
              source = "Automatisch erkannt"
            )
          )
        }
      }
    }

    // 5. Detect Strong Attack
    for (i in 1 until size - 1) {
      val prev = amplitudes[i - 1]
      val curr = amplitudes[i]
      val next = amplitudes[i + 1]
      if (prev > 100.0 && curr > 6000.0 && curr > prev * 2.5 && curr > next * 2.0) {
        candidates.add(
          TimestampedFeedback(
            sessionId = sessionId,
            startTimeSeconds = (i - 1).coerceAtLeast(0),
            endTimeSeconds = (i + 1).coerceAtMost(durationSeconds),
            category = "Toneinsatz",
            feedbackType = "Vorschlag",
            comment = "Starker Toneinsatz:\nEine deutliche Lautstärkespitze scheint hier vorzuliegen. Höre die Stelle noch einmal.",
            practiceSuggestion = "Prüfe, ob der Akzent oder Bogenwechsel an dieser Stelle musikalisch so beabsichtigt ist (Vorschlag – bitte überprüfen).",
            isAutomatic = true,
            canBeIgnored = true,
            canBeAccepted = true,
            source = "Automatisch erkannt"
          )
        )
      }
    }

    // 6. Low overall volume suggestion
    if (isOverallVolumeLow) {
      candidates.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = 0,
          endTimeSeconds = 3.coerceAtMost(durationSeconds),
          category = "Aufnahmequalität",
          feedbackType = "Hinweis",
          comment = "Geringe Aufnahmelautstärke:\nDie Aufnahme scheint insgesamt eher leise zu sein. Das kann an der Mikrofonposition oder an einem sehr zurückhaltenden Spiel liegen.",
          practiceSuggestion = "Prüfe beim nächsten Mal die Position des Geräts und achte darauf, dass der Klang klar aufgenommen wird.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    // 7. End of recording suggestion (only if recording is at least 20 seconds long)
    if (durationSeconds >= 20) {
      val endStart = (durationSeconds - 3).coerceAtLeast(0)
      candidates.add(
        TimestampedFeedback(
          sessionId = sessionId,
          startTimeSeconds = endStart,
          endTimeSeconds = durationSeconds,
          category = "Selbstkontrolle",
          feedbackType = "Vorschlag",
          comment = "Ende der Einspielung:\nDas Ende der Einspielung eignet sich möglicherweise gut für eine kurze Selbstkontrolle.",
          practiceSuggestion = "Höre die letzten Sekunden noch einmal an und prüfe, ob der Schluss bewusst gestaltet ist.",
          isAutomatic = true,
          canBeIgnored = true,
          canBeAccepted = true,
          source = "Automatisch erkannt"
        )
      )
    }

    // De-duplicate candidates that start close to each other or overlap
    val spaced = candidates.toMutableList()
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
            // Keep higher priority (higher score). If p1 >= p2, s1 has higher/equal priority, so remove s2.
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

    // Sort chronologically
    spaced.sortBy { it.startTimeSeconds }

    // Enforce limits based on recording duration
    val limit = when {
      durationSeconds < 20 -> 2
      durationSeconds <= 60 -> 4
      else -> 6
    }

    val finalSuggestions = spaced.take(limit)
    Log.d(TAG, "Generated ${finalSuggestions.size} suggestions (limit: $limit) from ${candidates.size} original candidates.")
    return finalSuggestions
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
