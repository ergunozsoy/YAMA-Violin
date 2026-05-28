package com.example.yamaviolin.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.yamaviolin.EntryDetail
import com.example.yamaviolin.data.PracticeSession
import com.example.yamaviolin.ui.dashboard.RecentSessionItem

@Composable
fun HistoryScreen(
  sessions: List<PracticeSession>,
  onNavigate: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredSessions = remember(sessions, searchQuery) {
    if (searchQuery.isBlank()) {
      sessions
    } else {
      sessions.filter {
        it.piece.contains(searchQuery, ignoreCase = true) ||
          it.notes.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  Column(
    modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Title
    Text(
      text = "Übungstagebuch",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Nach Stücken oder Notizen suchen...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Suchen") },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    // List of Sessions
    if (filteredSessions.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (searchQuery.isBlank()) "Keine Übungseinheiten eingetragen." else "Keine Einträge gefunden.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredSessions) { session ->
          RecentSessionItem(
            session = session,
            onClick = { onNavigate(EntryDetail(session.id)) }
          )
        }
      }
    }
  }
}
