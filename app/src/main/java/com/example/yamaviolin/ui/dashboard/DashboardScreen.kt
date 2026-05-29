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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.yamaviolin.EntryDetail
import com.example.yamaviolin.ImportPreview
import com.example.yamaviolin.NewEntry
import com.example.yamaviolin.R
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
    // 1. Do not use old simulated/demo/default duration values (mock sessions)
    if (session.piece == "Mendelssohn Violinkonzert in e-Moll, Op. 64" ||
        session.piece == "Kreutzer Etüde Nr. 2" ||
        session.piece == "Schradieck Tonleiterstudien") {
      return 0
    }

    // 2. Do not count legacy default "30 Min" values unless user explicitly entered them.
    // If durationMinutes is exactly 30, and notes are empty/blank and there is no recording, it's likely a legacy default.
    if (session.durationMinutes == 30) {
      val isLikelyDefault = session.notes.isBlank() && session.audioDurationSeconds == 0
      if (isLikelyDefault) {
        return 0
      }
    }

    // 3. If explicit practice duration exists and was truly entered by the user, use it.
    if (session.durationMinutes != null && session.durationMinutes > 0) {
      return session.durationMinutes
    }

    // 4. For recordings/imports, prefer audioDurationSeconds as the reliable fallback.
    if (session.audioDurationSeconds > 0) {
      return (session.audioDurationSeconds / 60).coerceAtLeast(1)
    }

    // 5. Fallback to 0 if no reliable duration exists
    return 0
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
      .fillMaxSize()
      .verticalScroll(scrollState)
      .background(MaterialTheme.colorScheme.background)
  ) {
    // 1. Hero Image Header (Edge-to-Edge)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
      Image(
        painter = painterResource(id = R.drawable.violin_hero),
        contentDescription = "Violin Hero Header",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
      // Dark warm amber/brown gradient overlay (strengthened for maximum readability)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0x6629160A), // Stronger warm amber/brown tint at the top
                Color(0xB31F0E05), // Deeper warm brown in the middle
                Color(0xF2140702)  // Solid deep dark amber/brown at the bottom
              )
            )
          )
      )
      // Text overlay
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(horizontal = 24.dp, vertical = 20.dp)
      ) {
        Text(
          text = todayDate.uppercase(Locale.GERMAN),
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.0.sp,
            fontSize = 11.sp
          ),
          color = Color(0xFFF7F2EA).copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Hallo, Geigerin & Geiger",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp
          ),
          color = Color.White
        )
      }
    }

    // 2. Dashboard Content (Padded)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 3. Compact Practice Stats Section (Real Data, Reduced Dominance)
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

      // 4. Action Buttons (Neue Aufnahme, Frühere Aufnahme importieren)
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
