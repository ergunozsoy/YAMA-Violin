package com.example.yamaviolin.ui.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.yamaviolin.data.RepositoryProvider
import com.example.yamaviolin.data.TimestampedFeedback
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

  // Simulated playback state
  var isPlaying by remember { mutableStateOf(false) }
  var playProgressSeconds by remember { mutableIntStateOf(0) }

  val audioDuration = session?.audioDurationSeconds ?: 0

  // Handle media playback animation
  LaunchedEffect(isPlaying, audioDuration) {
    if (isPlaying && audioDuration > 0) {
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

  // Dialog and bookmark states
  var showAddFeedbackDialog by remember { mutableStateOf(false) }
  var bookmarkedItems by remember { mutableStateOf(setOf<String>()) }

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
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .verticalScroll(scrollState)
          .padding(horizontal = 20.dp, vertical = 16.dp),
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

        // Date, Duration, Mood Row
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
            label = "Dauer",
            value = "${currentSession.durationMinutes} Min.",
            modifier = Modifier.weight(1f)
          )
          InfoItem(
            label = "Stimmung",
            value = currentSession.mood,
            modifier = Modifier.weight(1f)
          )
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
                    onClick = { isPlaying = !isPlaying },
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
                      onValueChange = { playProgressSeconds = it.toInt() },
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

        // Feature 1: Zeitmarkierte Analyse
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Zeitmarkierte Analyse",
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

          if (currentSession.feedbackItems.isEmpty()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Noch keine zeitmarkierten Analysen vorhanden.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              currentSession.feedbackItems.sortedBy { it.startTimeSeconds }.forEach { feedbackItem ->
                FeedbackCard(
                  item = feedbackItem,
                  isBookmarked = bookmarkedItems.contains(feedbackItem.id),
                  onListenClick = {
                    playProgressSeconds = feedbackItem.startTimeSeconds
                    isPlaying = true
                  },
                  onBookmarkClick = {
                    bookmarkedItems = if (bookmarkedItems.contains(feedbackItem.id)) {
                      bookmarkedItems - feedbackItem.id
                    } else {
                      bookmarkedItems + feedbackItem.id
                    }
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Dialog to Add Manual Feedback Item
  if (showAddFeedbackDialog && session != null) {
    var startStr by remember { mutableStateOf("") }
    var endStr by remember { mutableStateOf("") }
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

@Composable
fun FeedbackCard(
  item: TimestampedFeedback,
  isBookmarked: Boolean,
  onListenClick: () -> Unit,
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
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Time Stamp
        Text(
          text = "${formatTime(item.startTimeSeconds)} – ${formatTime(item.endTimeSeconds)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )

        // Badges Row
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
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

      // Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onListenClick,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Anhören", style = MaterialTheme.typography.labelMedium)
        }

        Button(
          onClick = onBookmarkClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isBookmarked) Color(0xFF27AE60) else MaterialTheme.colorScheme.secondary
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1.3f)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isBookmarked) "Markiert!" else "Als Übungsstelle markieren",
            style = MaterialTheme.typography.labelMedium
          )
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
