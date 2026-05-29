package com.example.yamaviolin.ui.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.DisposableEffect
import com.example.yamaviolin.data.RepositoryProvider
import com.example.yamaviolin.data.TimestampedFeedback
import com.example.yamaviolin.data.MusicalFeedback
import com.example.yamaviolin.data.MusicalFeedbackGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
  sessionId: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  // Load session
  val sessionFlow = remember(sessionId) {
    RepositoryProvider.repository.sessions.map { list ->
      list.firstOrNull { it.id == sessionId }
    }
  }
  val session by sessionFlow.collectAsState(initial = null)

  val context = LocalContext.current
  val audioPath = session?.audioUri
  val isRealAudio = !audioPath.isNullOrBlank()

  // Playback state
  var isPlaying by remember { mutableStateOf(false) }
  var playProgressSeconds by remember { mutableIntStateOf(0) }
  var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
  var activeSegment by remember { mutableStateOf<TimestampedFeedback?>(null) }
  var isContextPlayback by remember { mutableStateOf(false) }

  val audioDuration = session?.audioDurationSeconds ?: 0

  // Target playback boundaries
  val segmentStartSeconds = activeSegment?.let {
    if (isContextPlayback) maxOf(0, it.startTimeSeconds - 3) else it.startTimeSeconds
  }
  val segmentEndSeconds = activeSegment?.let {
    if (isContextPlayback) minOf(audioDuration, it.endTimeSeconds + 3) else it.endTimeSeconds
  }

  // Real MediaPlayer initialization
  LaunchedEffect(audioPath) {
    if (!audioPath.isNullOrBlank()) {
      try {
        val mp = MediaPlayer().apply {
          if (audioPath.startsWith("content://")) {
            setDataSource(context, Uri.parse(audioPath))
          } else {
            setDataSource(audioPath)
          }
          prepare()
        }
        mediaPlayer = mp
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  // Real MediaPlayer completion listener
  LaunchedEffect(mediaPlayer) {
    mediaPlayer?.setOnCompletionListener {
      isPlaying = false
      playProgressSeconds = 0
      activeSegment = null
      isContextPlayback = false
    }
  }

  // Handle media playback animation (real polling or simulated timer fallback)
  LaunchedEffect(isPlaying, mediaPlayer, isRealAudio, audioDuration, activeSegment, isContextPlayback) {
    if (isPlaying) {
      val start = segmentStartSeconds
      val end = segmentEndSeconds
      if (isRealAudio) {
        val mp = mediaPlayer
        if (mp != null) {
          while (isPlaying && mp.isPlaying) {
            val currentPos = mp.currentPosition / 1000
            if (start != null && end != null) {
              if (currentPos >= end) {
                mp.pause()
                isPlaying = false
                mp.seekTo(start * 1000)
                playProgressSeconds = start
              } else if (currentPos < start) {
                mp.seekTo(start * 1000)
                playProgressSeconds = start
              } else {
                playProgressSeconds = currentPos
              }
            } else {
              playProgressSeconds = currentPos
            }
            delay(250)
          }
        }
      } else {
        if (start != null && end != null) {
          while (isPlaying && playProgressSeconds < end) {
            delay(1000)
            playProgressSeconds++
          }
          if (playProgressSeconds >= end) {
            isPlaying = false
            playProgressSeconds = start
          }
        } else {
          while (isPlaying && playProgressSeconds < audioDuration) {
            delay(1000)
            playProgressSeconds++
          }
          if (playProgressSeconds >= audioDuration) {
            isPlaying = false
            playProgressSeconds = 0
          }
        }
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      try {
        mediaPlayer?.stop()
        mediaPlayer?.release()
      } catch (e: Exception) {
        // ignore
      }
    }
  }

  val playSegment: (TimestampedFeedback, Boolean) -> Unit = { feedbackItem, contextMode ->
    if (activeSegment?.id == feedbackItem.id && isContextPlayback == contextMode) {
      if (isPlaying) {
        if (isRealAudio) mediaPlayer?.pause()
        isPlaying = false
      } else {
        if (isRealAudio) mediaPlayer?.start()
        isPlaying = true
      }
    } else {
      activeSegment = feedbackItem
      isContextPlayback = contextMode
      val start = if (contextMode) maxOf(0, feedbackItem.startTimeSeconds - 3) else feedbackItem.startTimeSeconds
      playProgressSeconds = start
      if (isRealAudio) {
        try {
          mediaPlayer?.seekTo(start * 1000)
          if (!isPlaying) {
            mediaPlayer?.start()
          }
          isPlaying = true
        } catch (e: Exception) {
          e.printStackTrace()
        }
      } else {
        isPlaying = true
      }
    }
  }

  // Dialog and collapsible states
  var showAddFeedbackDialog by remember { mutableStateOf(false) }
  var showIgnoredSuggestions by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text("Details zur Einheit", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Zurück")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    }
  ) { innerPadding ->
    val currentSession = session
    if (currentSession == null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
        contentAlignment = Alignment.Center
      ) {
        Text("Einheit wurde nicht gefunden.", style = MaterialTheme.typography.bodyLarge)
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        // Piece Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = currentSession.piece,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f, fill = false)
          )
          if (currentSession.isImported) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Importierte Aufnahme",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onTertiaryContainer
              )
            }
          }
        }

        // Date, Duration, Mood Layout
        val hasPractice = currentSession.durationMinutes != null && currentSession.durationMinutes > 0
        val hasAudio = currentSession.audioDurationSeconds > 0

        if (hasPractice && hasAudio) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              InfoItem(
                label = "Datum",
                value = currentSession.date,
                modifier = Modifier.weight(1f)
              )
              InfoItem(
                label = "Stimmung",
                value = currentSession.mood,
                modifier = Modifier.weight(1f)
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              InfoItem(
                label = "Übungsdauer",
                value = "${currentSession.durationMinutes} Min.",
                modifier = Modifier.weight(1f)
              )
              InfoItem(
                label = "Aufnahmedauer",
                value = formatTime(currentSession.audioDurationSeconds),
                modifier = Modifier.weight(1f)
              )
            }
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            InfoItem(
              label = "Datum",
              value = currentSession.date,
              modifier = Modifier.weight(1f)
            )
            if (hasPractice) {
              InfoItem(
                label = "Übungsdauer",
                value = "${currentSession.durationMinutes} Min.",
                modifier = Modifier.weight(1f)
              )
            } else if (hasAudio) {
              InfoItem(
                label = "Aufnahmedauer",
                value = formatTime(currentSession.audioDurationSeconds),
                modifier = Modifier.weight(1f)
              )
            } else {
              InfoItem(
                label = "Übungsdauer",
                value = "nicht angegeben",
                modifier = Modifier.weight(1f)
              )
            }
            InfoItem(
              label = "Stimmung",
              value = currentSession.mood,
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Focus Areas
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Fokusbereiche",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
          )
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            currentSession.focusAreas.forEach { focus ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(MaterialTheme.colorScheme.secondaryContainer)
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = focus,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
            }
          }
        }

        // Notes section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Notizen & Reflexion",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
          )
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder()
          ) {
            Text(
              text = if (currentSession.notes.isBlank()) "Keine Notizen hinzugefügt." else currentSession.notes,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(16.dp),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Audio Playback section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Audioaufnahme",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
          )
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
          ) {
            if (audioDuration > 0) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  IconButton(
                    onClick = {
                      if (isRealAudio) {
                        val mp = mediaPlayer
                        if (mp != null) {
                          if (isPlaying) {
                            mp.pause()
                            isPlaying = false
                          } else {
                            if (playProgressSeconds >= audioDuration) {
                              mp.seekTo(0)
                              playProgressSeconds = 0
                            }
                            mp.start()
                            isPlaying = true
                          }
                        }
                      } else {
                        isPlaying = !isPlaying
                      }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(48.dp)
                  ) {
                    Icon(
                      imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                      contentDescription = if (isPlaying) "Stoppen" else "Abspielen"
                    )
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = if (isPlaying) "Spiele Aufnahme..." else "Aufnahme bereit",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                      value = playProgressSeconds.toFloat(),
                      onValueChange = {
                        val seekSec = it.toInt()
                        playProgressSeconds = seekSec
                        if (isRealAudio) {
                          mediaPlayer?.seekTo((seekSec * 1000).toInt())
                        }
                        val start = segmentStartSeconds
                        val end = segmentEndSeconds
                        if (start != null && end != null) {
                          if (seekSec < start || seekSec > end) {
                            activeSegment = null
                            isContextPlayback = false
                          }
                        }
                      },
                      valueRange = 0f..audioDuration.toFloat(),
                      modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = formatTime(playProgressSeconds),
                        style = MaterialTheme.typography.labelSmall
                      )
                      Text(
                        text = formatTime(audioDuration),
                        style = MaterialTheme.typography.labelSmall
                      )
                    }
                  }
                }

                // Local recording details
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "Diese Aufnahme wurde lokal in der App gespeichert.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                  )
                  if (currentSession.isImported) {
                    Text(
                      text = "Quelle: Importierte Aufnahme",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    currentSession.originalFileName?.let { fname ->
                      Text(
                        text = "Original-Datei: $fname",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    currentSession.importDate?.let { idate ->
                      Text(
                        text = "Importiert am: $idate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  } else {
                    Text(
                      text = "Quelle: Neue In-App Aufnahme",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    currentSession.recordingDate?.let { rdate ->
                      Text(
                        text = "Aufgenommen am: $rdate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }
            } else {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Keine Audioaufnahme für diese Einheit gespeichert.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
              }
            }
          }
        }

        // Feature 2: Automatische Vorerkennung (Simulierte Analyse im Prototyp)
        if (currentSession.autoHints.isNotEmpty()) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Automatische Vorerkennung",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground
            )
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                  )
                  Text(
                    text = "Simulierte Analyse im Prototyp",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                  )
                }

                currentSession.autoHints.forEach { hint ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = "•",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                      text = hint,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }

        // Musikalische Rückmeldung Section
        val musicalFeedback = remember(currentSession) {
          MusicalFeedbackGenerator.generateFeedback(currentSession)
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Header
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "Musikalische Rückmeldung",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }

            // 1. Eindruck
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "Eindruck",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = musicalFeedback.impression,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
              )
            }

            // 2. Beobachtung
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "Beobachtung",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = musicalFeedback.observation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
              )
            }

            // 3. Nächster Übeschritt
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "Nächster Übeschritt",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = musicalFeedback.nextPracticeStep,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
              )
            }
          }
        }

        // Feature 1: Analyse mit Zeitmarken
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Analyse mit Zeitmarken",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground
            )

            if (audioDuration > 0) {
              TextButton(
                onClick = { showAddFeedbackDialog = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Zeitmarke hinzufügen")
              }
            }
          }

          // Short explanation text and disclaimer
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "YAMA Violin markiert auffällige Stellen in der Aufnahme automatisch. Die Hinweise sind Vorschläge und sollten musikalisch überprüft werden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              )
              Text(
                text = "Diese Analyse ersetzt keine musikalische Beurteilung.",
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          // Filter feedbackItems
          val feedbackItems = currentSession.feedbackItems
          val automaticSuggestions = feedbackItems.filter { it.isAutomatic && !it.isAccepted && !it.isIgnored }
          val manualOrAcceptedItems = feedbackItems.filter { !it.isAutomatic || (it.isAutomatic && it.isAccepted) }
          val markedPracticePoints = feedbackItems.filter { it.isPracticePoint }
          val ignoredSuggestions = feedbackItems.filter { it.isAutomatic && it.isIgnored && !it.isAccepted }

          // Section 1: Übungsnotizen (übernommen)
          Text(
            text = "Übungsnotizen (übernommen)",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
          )

          if (manualOrAcceptedItems.isEmpty()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Noch keine gespeicherten Übungsnotizen vorhanden.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              manualOrAcceptedItems.sortedBy { it.startTimeSeconds }.forEach { feedbackItem ->
                FeedbackCard(
                  item = feedbackItem,
                  isActive = (activeSegment?.id == feedbackItem.id),
                  isActiveContext = (activeSegment?.id == feedbackItem.id && isContextPlayback),
                  onListenClick = { playSegment(feedbackItem, false) },
                  onListenContextClick = { playSegment(feedbackItem, true) },
                  onBookmarkClick = {
                    RepositoryProvider.repository.togglePracticePoint(currentSession.id, feedbackItem.id)
                  },
                  onUndoClick = if (feedbackItem.isAutomatic) {
                    { RepositoryProvider.repository.undoAcceptFeedback(currentSession.id, feedbackItem.id) }
                  } else {
                    null
                  }
                )
              }
            }
          }

          // Section 2: Automatische Vorschläge
          Text(
            text = "Automatische Vorschläge",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp)
          )

          if (automaticSuggestions.isEmpty()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Keine weiteren Analysevorschläge vorhanden.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              automaticSuggestions.sortedBy { it.startTimeSeconds }.forEach { feedbackItem ->
                AutomaticSuggestionCard(
                  item = feedbackItem,
                  isActive = (activeSegment?.id == feedbackItem.id),
                  isActiveContext = (activeSegment?.id == feedbackItem.id && isContextPlayback),
                  onListenClick = { playSegment(feedbackItem, false) },
                  onListenContextClick = { playSegment(feedbackItem, true) },
                  onAcceptClick = {
                    RepositoryProvider.repository.acceptFeedback(currentSession.id, feedbackItem.id)
                  },
                  onIgnoreClick = {
                    RepositoryProvider.repository.ignoreFeedback(currentSession.id, feedbackItem.id)
                  },
                  onBookmarkClick = {
                    RepositoryProvider.repository.togglePracticePoint(currentSession.id, feedbackItem.id)
                  }
                )
              }
            }
          }

          // Section 3: Markierte Übungsstellen
          Text(
            text = "Markierte Übungsstellen",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 12.dp)
          )

          if (markedPracticePoints.isEmpty()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Keine markierten Übungsstellen vorhanden.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              markedPracticePoints.sortedBy { it.startTimeSeconds }.forEach { feedbackItem ->
                FeedbackCard(
                  item = feedbackItem,
                  isActive = (activeSegment?.id == feedbackItem.id),
                  isActiveContext = (activeSegment?.id == feedbackItem.id && isContextPlayback),
                  onListenClick = { playSegment(feedbackItem, false) },
                  onListenContextClick = { playSegment(feedbackItem, true) },
                  onBookmarkClick = {
                    RepositoryProvider.repository.togglePracticePoint(currentSession.id, feedbackItem.id)
                  },
                  onUndoClick = null,
                  isMarkedSection = true
                )
              }
            }
          }

          // Section 4: Ignorierte Vorschläge (Collapsible)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { showIgnoredSuggestions = !showIgnoredSuggestions }
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Ignorierte Vorschläge (${ignoredSuggestions.size})",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Text(
              text = if (showIgnoredSuggestions) "▲" else "▼",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
          }

          if (showIgnoredSuggestions) {
            if (ignoredSuggestions.isEmpty()) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                border = CardDefaults.outlinedCardBorder()
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "Keine ignorierten Vorschläge vorhanden.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                  )
                }
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ignoredSuggestions.sortedBy { it.startTimeSeconds }.forEach { feedbackItem ->
                  IgnoredSuggestionCard(
                    item = feedbackItem,
                    isActive = (activeSegment?.id == feedbackItem.id),
                    isActiveContext = (activeSegment?.id == feedbackItem.id && isContextPlayback),
                    onListenClick = { playSegment(feedbackItem, false) },
                    onListenContextClick = { playSegment(feedbackItem, true) },
                    onRestoreClick = {
                      RepositoryProvider.repository.restoreFeedback(currentSession.id, feedbackItem.id)
                    }
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }

      if (activeSegment != null) {
        val segment = activeSegment!!
        val start = if (isContextPlayback) maxOf(0, segment.startTimeSeconds - 3) else segment.startTimeSeconds
        val end = if (isContextPlayback) minOf(audioDuration, segment.endTimeSeconds + 3) else segment.endTimeSeconds
        StickyMiniPlayer(
          segment = segment,
          isPlaying = isPlaying,
          isContextMode = isContextPlayback,
          playProgressSeconds = playProgressSeconds,
          audioDuration = audioDuration,
          onPlayPause = {
            if (isRealAudio) {
              val mp = mediaPlayer
              if (mp != null) {
                if (isPlaying) {
                  mp.pause()
                  isPlaying = false
                } else {
                  if (playProgressSeconds >= end) {
                    mp.seekTo(start * 1000)
                    playProgressSeconds = start
                  }
                  mp.start()
                  isPlaying = true
                }
              }
            } else {
              isPlaying = !isPlaying
            }
          },
          onClose = {
            if (isRealAudio) mediaPlayer?.pause()
            isPlaying = false
            activeSegment = null
            isContextPlayback = false
          },
          onSeek = { seekSec ->
            playProgressSeconds = seekSec
            if (isRealAudio) mediaPlayer?.seekTo(seekSec * 1000)
          },
          onPlayFull = {
            activeSegment = null
            isContextPlayback = false
          },
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp, vertical = 16.dp)
        )
      }
    }
  }
}

  // Dialog to Add Manual Feedback Item
  if (showAddFeedbackDialog && session != null) {
    var startStr by remember { mutableStateOf(playProgressSeconds.toString()) }
    var endStr by remember { mutableStateOf(if (audioDuration > 0) (playProgressSeconds + 5).coerceAtMost(audioDuration).toString() else "5") }
    var comment by remember { mutableStateOf("") }
    var suggestion by remember { mutableStateOf("") }

    val categories = listOf("Intonation", "Bogenführung", "Lagenwechsel", "Klangqualität", "Vibrato", "Rhythmus/Tempo", "Musikalischer Ausdruck", "Aufnahmequalität")
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val types = listOf("Stärke", "Hinweis", "Problem", "Übungsziel")
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(types[0]) }

    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddFeedbackDialog = false },
      title = { Text("Zeitmarke hinzufügen", fontWeight = FontWeight.Bold) },
      confirmButton = {
        Button(
          onClick = {
            val startSec = startStr.toIntOrNull()
            val endSec = endStr.toIntOrNull()

            if (startSec == null || endSec == null) {
              errorMessage = "Bitte gültige Sekundenwerte eintragen."
              return@Button
            }
            if (startSec < 0 || endSec < 0) {
              errorMessage = "Zeiten müssen positiv sein."
              return@Button
            }
            if (startSec > endSec) {
              errorMessage = "Startzeit muss vor/gleich Endzeit sein."
              return@Button
            }
            if (endSec > audioDuration) {
              errorMessage = "Zeiten dürfen nicht die Aufnahmedauer ($audioDuration Sek.) überschreiten."
              return@Button
            }
            if (comment.isBlank()) {
              errorMessage = "Bitte einen Kommentar eintragen."
              return@Button
            }

            // Save manual feedback
            val newFeedback = TimestampedFeedback(
              sessionId = sessionId,
              startTimeSeconds = startSec,
              endTimeSeconds = endSec,
              category = selectedCategory,
              feedbackType = selectedType,
              comment = comment,
              practiceSuggestion = suggestion
            )
            RepositoryProvider.repository.addFeedbackToSession(sessionId, newFeedback)
            showAddFeedbackDialog = false
          }
        ) {
          Text("Speichern")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddFeedbackDialog = false }) {
          Text("Abbrechen")
        }
      },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          if (errorMessage.isNotBlank()) {
            Text(text = errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = startStr,
              onValueChange = { startStr = it },
              label = { Text("Startzeit (Sek.)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = endStr,
              onValueChange = { endStr = it },
              label = { Text("Endzeit (Sek.)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f)
            )
          }

          // Category Dropdown
          ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
          ) {
            OutlinedTextField(
              value = selectedCategory,
              onValueChange = {},
              readOnly = true,
              label = { Text("Kategorie") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = categoryExpanded,
              onDismissRequest = { categoryExpanded = false }
            ) {
              categories.forEach { category ->
                DropdownMenuItem(
                  text = { Text(category) },
                  onClick = {
                    selectedCategory = category
                    categoryExpanded = false
                  }
                )
              }
            }
          }

          // Feedback-Type Dropdown
          ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
          ) {
            OutlinedTextField(
              value = selectedType,
              onValueChange = {},
              readOnly = true,
              label = { Text("Feedback-Typ") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = typeExpanded,
              onDismissRequest = { typeExpanded = false }
            ) {
              types.forEach { t ->
                DropdownMenuItem(
                  text = { Text(t) },
                  onClick = {
                    selectedType = t
                    typeExpanded = false
                  }
                )
              }
            }
          }

          OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Kommentar (Hinweis-Tonfall)") },
            placeholder = { Text("z.B. Die Intonation wirkt möglicherweise...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
          )

          OutlinedTextField(
            value = suggestion,
            onValueChange = { suggestion = it },
            label = { Text("Übungsvorschlag (optional)") },
            placeholder = { Text("z.B. Langsames Üben empfohlen...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
          )
        }
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackCard(
  item: TimestampedFeedback,
  isActive: Boolean = false,
  isActiveContext: Boolean = false,
  onListenClick: () -> Unit,
  onListenContextClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onUndoClick: (() -> Unit)? = null,
  isMarkedSection: Boolean = false
) {
  val typeColor = when (item.feedbackType) {
    "Stärke" -> Color(0xFF27AE60)
    "Hinweis" -> MaterialTheme.colorScheme.secondary
    "Problem" -> Color(0xFFC0392B)
    "Übungsziel" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    ),
    border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Time Stamp
        Text(
          text = "${formatTime(item.startTimeSeconds)} – ${formatTime(item.endTimeSeconds)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )

        // Badges Row
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          // Practice Point Badge
          if (item.isPracticePoint) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF27AE60).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFF27AE60), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "Markiert",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF27AE60)
              )
            }
          }

          // Source Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (item.isAutomatic) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.source,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (item.isAutomatic) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Category Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.category,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          // Type Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .border(1.dp, typeColor, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.feedbackType,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = typeColor
            )
          }
        }
      }

      // Comment
      Text(
        text = item.comment,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )

      // Suggestion
      if (item.practiceSuggestion.isNotBlank()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          )
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Übungsvorschlag:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = item.practiceSuggestion,
              style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Actions Column
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Stelle anhören
        val isSegmentPlaying = isActive && !isActiveContext
        Button(
          onClick = onListenClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSegmentPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSegmentPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (isSegmentPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Stelle anhören", style = MaterialTheme.typography.labelMedium)
        }

        // Mit Kontext anhören
        val isContextPlaying = isActive && isActiveContext
        Button(
          onClick = onListenContextClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isContextPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isContextPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (isContextPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Mit Kontext anhören", style = MaterialTheme.typography.labelMedium)
        }

        // Bookmark button
        Button(
          onClick = onBookmarkClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (item.isPracticePoint) Color(0xFF27AE60) else MaterialTheme.colorScheme.secondary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isMarkedSection) "Markierung entfernen" else (if (item.isPracticePoint) "Markiert!" else "Als Übungsstelle markieren"),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
          )
        }

        if (onUndoClick != null) {
          Button(
            onClick = onUndoClick,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Rückgängig", style = MaterialTheme.typography.labelMedium, maxLines = 1)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutomaticSuggestionCard(
  item: TimestampedFeedback,
  isActive: Boolean = false,
  isActiveContext: Boolean = false,
  onListenClick: () -> Unit,
  onListenContextClick: () -> Unit,
  onAcceptClick: () -> Unit,
  onIgnoreClick: () -> Unit,
  onBookmarkClick: () -> Unit
) {
  val typeColor = when (item.feedbackType) {
    "Stärke" -> Color(0xFF27AE60)
    "Hinweis" -> MaterialTheme.colorScheme.secondary
    "Problem" -> Color(0xFFC0392B)
    "Übungsziel" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    ),
    border = if (isActive) BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Time Stamp
        Text(
          text = "${formatTime(item.startTimeSeconds)} – ${formatTime(item.endTimeSeconds)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )

        // Badges FlowRow
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          // State Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (item.isPracticePoint) Color(0xFF27AE60).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer)
              .border(1.dp, if (item.isPracticePoint) Color(0xFF27AE60) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (item.isPracticePoint) "Markiert" else "Neu",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (item.isPracticePoint) Color(0xFF27AE60) else MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          // Source badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.tertiaryContainer)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Automatisch erkannt",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onTertiaryContainer
            )
          }

          // Category Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.category,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          // Type Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .border(1.dp, typeColor, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.feedbackType,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = typeColor
            )
          }
        }
      }

      // Comment
      Text(
        text = item.comment,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )

      // Suggestion
      if (item.practiceSuggestion.isNotBlank()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Übungsvorschlag:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = item.practiceSuggestion,
              style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Actions Column
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1 (Anhören & Kontext)
        val isSegmentPlaying = isActive && !isActiveContext
        Button(
          onClick = onListenClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSegmentPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSegmentPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (isSegmentPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Stelle anhören", style = MaterialTheme.typography.labelMedium)
        }

        val isContextPlaying = isActive && isActiveContext
        Button(
          onClick = onListenContextClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isContextPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isContextPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (isContextPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Mit Kontext anhören", style = MaterialTheme.typography.labelMedium)
        }

        // Row 2 (Übernehmen & Ignorieren)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onAcceptClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Übernehmen", style = MaterialTheme.typography.labelMedium, maxLines = 1)
          }

          Button(
            onClick = onIgnoreClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ignorieren", style = MaterialTheme.typography.labelMedium, maxLines = 1)
          }
        }

        // Row 3 (Als Übungsstelle markieren)
        Button(
          onClick = onBookmarkClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (item.isPracticePoint) Color(0xFF27AE60) else MaterialTheme.colorScheme.secondary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (item.isPracticePoint) "Markiert!" else "Als Übungsstelle markieren",
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IgnoredSuggestionCard(
  item: TimestampedFeedback,
  isActive: Boolean = false,
  isActiveContext: Boolean = false,
  onListenClick: () -> Unit,
  onListenContextClick: () -> Unit,
  onRestoreClick: () -> Unit
) {
  val typeColor = when (item.feedbackType) {
    "Stärke" -> Color(0xFF27AE60).copy(alpha = 0.6f)
    "Hinweis" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
    "Problem" -> Color(0xFFC0392B).copy(alpha = 0.6f)
    "Übungsziel" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ),
    border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Time Stamp
        Text(
          text = "${formatTime(item.startTimeSeconds)} – ${formatTime(item.endTimeSeconds)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        // Badges FlowRow
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          // Ignored State Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
              .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Ignoriert",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
          }

          // Category Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.category,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
          }

          // Type Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .border(1.dp, typeColor, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.feedbackType,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = typeColor
            )
          }
        }
      }

      // Comment
      Text(
        text = item.comment,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      )

      // Suggestion
      if (item.practiceSuggestion.isNotBlank()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
          )
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Übungsvorschlag:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = item.practiceSuggestion,
              style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
          }
        }
      }

      // Actions: Row (Anhören & Kontext & Wiederherstellen)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val isSegmentPlaying = isActive && !isActiveContext
        Button(
          onClick = onListenClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSegmentPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            contentColor = if (isSegmentPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (isSegmentPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Stelle anhören", style = MaterialTheme.typography.labelMedium)
        }

        val isContextPlaying = isActive && isActiveContext
        Button(
          onClick = onListenContextClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isContextPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            contentColor = if (isContextPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (isContextPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Mit Kontext anhören", style = MaterialTheme.typography.labelMedium)
        }

        // Wiederherstellen
        Button(
          onClick = onRestoreClick,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Wiederherstellen", style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
      }
    }
  }
}

private fun formatTime(seconds: Int): String {
  val mins = seconds / 60
  val secs = seconds % 60
  return String.format(Locale.GERMAN, "%02d:%02d", mins, secs)
}

@Composable
fun InfoItem(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder()
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickyMiniPlayer(
  segment: TimestampedFeedback,
  isPlaying: Boolean,
  isContextMode: Boolean,
  playProgressSeconds: Int,
  audioDuration: Int,
  onPlayPause: () -> Unit,
  onClose: () -> Unit,
  onSeek: (Int) -> Unit,
  onPlayFull: () -> Unit,
  modifier: Modifier = Modifier
) {
  val start = if (isContextMode) maxOf(0, segment.startTimeSeconds - 3) else segment.startTimeSeconds
  val end = if (isContextMode) minOf(audioDuration, segment.endTimeSeconds + 3) else segment.endTimeSeconds
  val progress = playProgressSeconds.coerceIn(start, end)

  val typeColor = when (segment.feedbackType) {
    "Stärke" -> Color(0xFF27AE60)
    "Hinweis" -> MaterialTheme.colorScheme.secondary
    "Problem" -> Color(0xFFC0392B)
    "Übungsziel" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp)),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = if (isContextMode) "Kontext wird abgespielt" else "Stelle wird abgespielt",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${segment.category} • ${segment.feedbackType}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = onPlayPause,
            colors = IconButtonDefaults.iconButtonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors(
              containerColor = Color.Transparent,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Schließen",
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = formatTime(progress),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Slider(
          value = progress.toFloat(),
          onValueChange = { onSeek(it.toInt()) },
          valueRange = start.toFloat()..end.toFloat(),
          modifier = Modifier.weight(1f),
          thumb = {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(percent = 50))
            )
          }
        )

        Text(
          text = formatTime(end),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        TextButton(
          onClick = onPlayFull,
          modifier = Modifier.height(24.dp)
        ) {
          Text(
            text = "Gesamte Aufnahme",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}
