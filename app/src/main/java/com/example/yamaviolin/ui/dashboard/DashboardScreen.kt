package com.example.yamaviolin.ui.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.yamaviolin.EntryDetail
import com.example.yamaviolin.ImportPreview
import com.example.yamaviolin.NewEntry
import com.example.yamaviolin.data.PracticeSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  sessions: List<PracticeSession>,
  onNavigate: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val todayDate = SimpleDateFormat("EEEE, d. MMMM yyyy", Locale.GERMAN).format(Date())

  val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      onNavigate(ImportPreview(uri.toString()))
    }
  }

  // Calculate real active practice minutes per session
  fun getSessionMinutes(session: PracticeSession): Int {
    return session.durationMinutes ?: if (session.audioDurationSeconds > 0) {
      (session.audioDurationSeconds / 60).coerceAtLeast(1)
    } else {
      0
    }
  }

  val todayFormatted = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Date())
  val todayMinutes = sessions.filter { it.date == todayFormatted }.sumOf { getSessionMinutes(it) }
  val totalMinutes = sessions.sumOf { getSessionMinutes(it) }
  val lastSession = sessions.firstOrNull() // Sessions are ordered newest first

  val mainColor = MaterialTheme.colorScheme.primary
  val backgroundGradient = Brush.verticalGradient(
    colors = listOf(
      mainColor.copy(alpha = 0.12f),
      Color.Transparent
    )
  )

  Column(
    modifier = modifier
      .verticalScroll(scrollState)
      .background(backgroundGradient)
      .padding(horizontal = 20.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Header
    Column {
      Text(
        text = todayDate,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Hallo Geigerin & Geiger,",
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    // 1. Compact Practice Stats Section (Real Data, Reduced Dominance)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
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
          // Heute
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Heute geübt",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$todayMinutes Min.",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
          }

          // Vertical Divider
          Box(
            modifier = Modifier
              .height(36.dp)
              .width(1.dp)
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          )

          // Gesamt
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = "Gesamt geübt",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
              textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$totalMinutes Min.",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.secondary,
              textAlign = TextAlign.End
            )
          }
        }

        // Letzte Einheit (Optional - only if there are sessions)
        if (lastSession != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
              modifier = Modifier.size(16.dp)
            )
            val durationLabel = if (lastSession.durationMinutes != null && lastSession.durationMinutes > 0) {
              "${lastSession.durationMinutes} Min."
            } else if (lastSession.audioDurationSeconds > 0) {
              formatTime(lastSession.audioDurationSeconds)
            } else {
              "Dauer n.a."
            }
            Text(
              text = "Letzte Einheit: ${lastSession.piece} (${lastSession.date} • $durationLabel)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
              maxLines = 1
            )
          }
        }
      }
    }

    // 2. Action Buttons (Neue Aufnahme, Frühere Aufnahme importieren)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Neue Aufnahme Card
      Card(
        modifier = Modifier
          .weight(1f)
          .clickable { onNavigate(NewEntry) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Neue Aufnahme",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center
          )
        }
      }

      // Aufnahme importieren Card
      Card(
        modifier = Modifier
          .weight(1f)
          .clickable { importLauncher.launch("audio/*") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Aufnahme importieren",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    // Recent Sessions Title
    Text(
      text = "Letzte Übungseinheiten",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )

    // Recent Sessions List / Calm Empty State
    if (sessions.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Noch keine Übungseinheiten vorhanden.",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Starte eine neue Aufnahme oder importiere eine frühere Aufnahme.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 24.dp)
        )
      }
    } else {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        sessions.take(3).forEach { session ->
          RecentSessionItem(
            session = session,
            onClick = { onNavigate(EntryDetail(session.id)) }
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
fun RecentSessionItem(
  session: PracticeSession,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = session.date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
          // Mood Chip
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(MaterialTheme.colorScheme.secondaryContainer)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = session.mood,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = session.piece,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        val durationLabel = if (session.durationMinutes != null && session.durationMinutes > 0) {
          "${session.durationMinutes} Min. geübt"
        } else if (session.audioDurationSeconds > 0) {
          "Aufnahme: ${formatTime(session.audioDurationSeconds)}"
        } else {
          "Übungsdauer nicht angegeben"
        }
        val subtitleText = if (session.focusAreas.isNotEmpty()) {
          "$durationLabel • ${session.focusAreas.joinToString(", ")}"
        } else {
          durationLabel
        }
        Text(
          text = subtitleText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
      }
      Icon(
        imageVector = Icons.Default.ArrowForward,
        contentDescription = "Details",
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
      )
    }
  }
}
