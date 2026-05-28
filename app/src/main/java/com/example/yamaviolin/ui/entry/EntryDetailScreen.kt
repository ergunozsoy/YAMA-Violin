package com.example.yamaviolin.ui.entry

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yamaviolin.data.RepositoryProvider
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
        Text(
          text = currentSession.piece,
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )

        // Date and Duration row
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
                        text = String.format(Locale.GERMAN, "%02d:%02d", playProgressSeconds / 60, playProgressSeconds % 60),
                        style = MaterialTheme.typography.labelSmall
                      )
                      Text(
                        text = String.format(Locale.GERMAN, "%02d:%02d", audioDuration / 60, audioDuration % 60),
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
      }
    }
  }
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
