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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

  // Calculate statistics
  val totalMinutesThisWeek = sessions.sumOf { it.durationMinutes }
  val todayFormatted = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Date())
  val todayMinutes = sessions.filter { it.date == todayFormatted }.sumOf { it.durationMinutes }

  val mainColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
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

    // Slogan Banner Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Spielen. Hören. Reflektieren. Wachsen.",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
          ),
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Dein digitales Übungstagebuch zur täglichen musikalischen Entwicklung.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
          textAlign = TextAlign.Center
        )
      }
    }

    // Stats Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      StatCard(
        title = "Heute geübt",
        value = "$todayMinutes Min.",
        subtitle = "Tagesziel: 45 Min.",
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Gesamt geübt",
        value = "${totalMinutesThisWeek} Min.",
        subtitle = "Wochenziel: 240 Min.",
        modifier = Modifier.weight(1f)
      )
    }

    // Daily Tip Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
      )
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = "Tipp des Tages",
          tint = MaterialTheme.colorScheme.tertiary
        )
        Column {
          Text(
            text = "Tipp des Tages",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.tertiary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Achte heute besonders auf eine gerade Bogenführung parallel zum Steg. Verwende einen Spiegel zur Selbstkontrolle.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
          )
        }
      }
    }

    // Import Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { importLauncher.launch("audio/*") },
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Column {
          Text(
            text = "Frühere Aufnahme importieren",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Wähle eine bestehende Audiodatei aus deinem Gerätespeicher aus.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
        }
      }
    }

    // Recent Sessions Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Letzte Übungseinheiten",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    // Recent Sessions List
    if (sessions.isEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Noch keine Einträge vorhanden.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = { onNavigate(NewEntry) },
            colors = ButtonDefaults.buttonColors(containerColor = mainColor)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Erste Einheit eintragen")
          }
        }
      }
    } else {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        sessions.take(2).forEach { session ->
          RecentSessionItem(
            session = session,
            onClick = { onNavigate(EntryDetail(session.id)) }
          )
        }
      }
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.Start
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
      )
    }
  }
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
        Text(
          text = "${session.durationMinutes} Min. geübt • ${session.focusAreas.joinToString(", ")}",
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
