package com.example.yamaviolin.data

data class MusicalFeedback(
  val impression: String,
  val observation: String,
  val nextPracticeStep: String
)

object MusicalFeedbackGenerator {
  fun generateFeedback(session: PracticeSession): MusicalFeedback {
    val feedbackItems = session.feedbackItems
    val categories = feedbackItems.map { it.category }
    val totalFeedbackCount = feedbackItems.size

    val recordingQualityCount = categories.count { it == "Aufnahmequalität" }
    val toneQualityCount = categories.count { it == "Klangqualität" }
    val dynamicsCount = categories.count { it == "Dynamik" }
    val transitionCount = categories.count { it == "Übergang" || it == "Einsatz" }
    val expressionCount = categories.count { it == "Musikalischer Ausdruck" }
    val bowingCount = categories.count { it == "Bogenführung" }
    val intonationCount = categories.count { it == "Intonation" }

    val durationSeconds = session.audioDurationSeconds

    return when {
      // 1. Very short recording
      durationSeconds in 1..14 -> {
        MusicalFeedback(
          impression = "Diese kurze Aufnahme eignet sich gut für eine erste Selbstkontrolle.",
          observation = "Der Klang ist hörbar, aber für eine genauere musikalische Rückmeldung wäre eine etwas längere zusammenhängende Passage hilfreich.",
          nextPracticeStep = "Nimm eine Passage von 30–60 Sekunden auf und achte dabei bewusst auf Klang, Linie und ruhigen Toneinsatz."
        )
      }

      // 2. Recording Quality Issues
      recordingQualityCount > 0 && recordingQualityCount >= totalFeedbackCount / 2 -> {
        MusicalFeedback(
          impression = "Die Aufnahme gibt einen ersten Eindruck, bleibt klanglich aber noch etwas zurückhaltend.",
          observation = "Das kann an der Mikrofonposition oder an einem sehr leisen Spiel liegen.",
          nextPracticeStep = "Lege das Gerät beim nächsten Mal etwas näher und spiele eine kurze Passage mit ruhigem, klar geführtem Ton."
        )
      }

      // 3. No feedback items
      totalFeedbackCount == 0 -> {
        MusicalFeedback(
          impression = "Die Aufnahme hinterlässt einen ruhigen, ausgeglichenen Eindruck.",
          observation = "Es sind keine unmittelbaren technischen oder klanglichen Unterbrechungen registriert.",
          nextPracticeStep = "Nutze diese Stabilität, um beim nächsten Durchgang ganz bewusst den Gestaltungsbogen und die Phrasierung zu formen."
        )
      }

      // 4. Interruption / Transition / Einsatz
      transitionCount > 0 && transitionCount >= maxOf(1, totalFeedbackCount / 3) -> {
        MusicalFeedback(
          impression = "Die musikalische Bewegung wirkt an dieser Stelle kurz unterbrochen.",
          observation = "Prüfe, ob diese Pause bewusst gestaltet ist oder ob der Übergang flüssiger vorbereitet werden kann.",
          nextPracticeStep = "Höre die Stelle mit Kontext an und spiele den Übergang langsam noch einmal."
        )
      }

      // 5. Tone quality / Klangqualität
      toneQualityCount > 0 && toneQualityCount >= maxOf(1, totalFeedbackCount / 3) -> {
        MusicalFeedback(
          impression = "Der Klang wirkt hier möglicherweise etwas unruhig.",
          observation = "Achte darauf, ob Bogenkontakt und Bogengeschwindigkeit gleichmäßig bleiben.",
          nextPracticeStep = "Spiele die Stelle langsam und höre danach dieselbe Stelle mit Kontext an."
        )
      }

      // 6. Bowing / Bogenführung
      bowingCount > 0 && bowingCount >= maxOf(1, totalFeedbackCount / 3) -> {
        MusicalFeedback(
          impression = "Der Bogenstrich scheint zeitweise etwas an Kontakt zu verlieren.",
          observation = "Prüfe den Kontaktpunkt zur Saite und achte auf ein gleichmäßiges Bogengewicht.",
          nextPracticeStep = "Spiele leere Saiten oder einfache Tonleitern und achte weniger auf Perfektion, mehr auf Richtung und Klang."
        )
      }

      // 7. Dynamics / Dynamik
      dynamicsCount > 0 && dynamicsCount >= maxOf(1, totalFeedbackCount / 3) -> {
        MusicalFeedback(
          impression = "Die Dynamik verändert sich an einigen Stellen sehr deutlich.",
          observation = "Prüfe, ob diese Kontraste so beabsichtigt sind und ob der Bogenkontakt auch im Piano stabil bleibt.",
          nextPracticeStep = "Höre diese Stellen noch einmal in Ruhe mit Kontext an und achte auf die Ausgewogenheit des Klangs."
        )
      }

      // 8. Expression / Ausdruck
      expressionCount > 0 && expressionCount >= maxOf(1, totalFeedbackCount / 3) -> {
        MusicalFeedback(
          impression = "Die musikalische Linie verliert an einzelnen Punkten etwas an Führung.",
          observation = "Prüfe, ob die Phrase ein klares Ziel hat und ob der Bogen diese Richtung unterstützt.",
          nextPracticeStep = "Versuche, den Klang bewusst zu führen, und richte deine Aufmerksamkeit ganz auf den Bogenkontakt."
        )
      }

      // 9. Intonation
      intonationCount > 0 -> {
        MusicalFeedback(
          impression = "Das Tonzentrum wirkt an bestimmten Übergängen noch etwas unentschlossen.",
          observation = "Prüfe das Greifgefühl der linken Hand, besonders beim Lagenwechsel oder in höheren Positionen.",
          nextPracticeStep = "Nimm dir hier einen Moment Zeit, spiele die Passage langsam ohne Vibrato und vergleiche den Ton mit einer leeren Saite."
        )
      }

      // 10. Default fallback
      else -> {
        MusicalFeedback(
          impression = "Die Aufnahme bietet eine schöne Grundlage, um Details weiter zu verfeineren.",
          observation = "Vereinzelt scheinen Bogenführung und Lagenübergänge noch etwas Aufmerksamkeit zu benötigen.",
          nextPracticeStep = "Wähle eine Passage von wenigen Takten aus, spiele sie langsam und achte weniger auf Perfektion, mehr auf Richtung und Klang."
        )
      }
    }
  }
}
