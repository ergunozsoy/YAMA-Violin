package com.example.yamaviolin.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.yamaviolin.data.PracticeSession
import com.example.yamaviolin.data.RepositoryProvider
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewEntryScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  // Form states
  var pieceTitle by remember { mutableStateOf("") }
  var durationStr by remember { mutableStateOf("") }
  var selectedMood by remember { mutableStateOf("Gut") }
  val focusOptions = listOf("Intonation", "Bogenführung", "Tempo", "Fingersatz", "Rhythmus", "Dynamik")
  val selectedFocus = remember { mutableStateListOf<String>() }
  var notes by remember { mutableStateOf("") }

  // Simulated recording states
  var isRecording by remember { mutableStateOf(false) }
  var recordedSeconds by remember { mutableIntStateOf(0) }
  var audioSavedDuration by remember { mutableIntStateOf(0) }

  // Waveform animation
  val waveBars = remember { mutableStateListOf(10.dp, 20.dp, 15.dp, 30.dp, 8.dp, 25.dp, 12.dp, 18.dp, 22.dp, 14.dp, 10.dp) }

  LaunchedEffect(isRecording) {
    if (isRecording) {
      while (isRecording) {
        delay(1000)
        recordedSeconds++
      }
    }
  }

  LaunchedEffect(isRecording) {
    if (isRecording) {
      while (isRecording) {
        delay(120)
        for (i in waveBars.indices) {
          waveBars[i] = (6..36).random().dp
        }
      }
    }
  }

  val moods = listOf(
    "Hervorragend" to "😃",
    "Gut" to "🙂",
    "Ok" to "😐",
    "Schwer" to "☹️"
  )

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text("Neue Übungseinheit", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Schließen")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Piece Title Input
      OutlinedTextField(
        value = pieceTitle,
        onValueChange = { pieceTitle = it },
        label = { Text("Stück / Übung") },
        placeholder = { Text("z.B. Mendelssohn Violinkonzert") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      // Duration Input
      OutlinedTextField(
        value = durationStr,
        onValueChange = { durationStr = it },
        label = { Text("Dauer (Minuten)") },
        placeholder = { Text("z.B. 45") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      // Mood Selector
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Wie hat es sich angefühlt?",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          moods.forEach { (moodName, emoji) ->
            val isSelected = selectedMood == moodName
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedMood = moodName },
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
              ),
              border = if (isSelected) null else CardDefaults.outlinedCardBorder()
            ) {
              Column(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = moodName,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
              }
            }
          }
        }
      }

      // Focus Areas Selector
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Fokusbereiche",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          focusOptions.forEach { focus ->
            val isSelected = selectedFocus.contains(focus)
            FilterChip(
              selected = isSelected,
              onClick = {
                if (isSelected) selectedFocus.remove(focus) else selectedFocus.add(focus)
              },
              label = { Text(focus) },
              shape = RoundedCornerShape(20.dp)
            )
          }
        }
      }

      // Simulated Audio Recorder Widget
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Audioaufnahme (Prototyp)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(12.dp))

          // Waveform visualizer
          Row(
            modifier = Modifier
              .height(40.dp)
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            waveBars.forEach { height ->
              val barColor = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
              Box(
                modifier = Modifier
                  .padding(horizontal = 2.dp)
                  .width(4.dp)
                  .height(if (isRecording) height else 6.dp)
                  .clip(CircleShape)
                  .background(barColor)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Time details & status
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            if (isRecording) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(Color.Red)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = String.format(Locale.GERMAN, "Aufnahme läuft... %02d:%02d", recordedSeconds / 60, recordedSeconds % 60),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Red
              )
            } else if (audioSavedDuration > 0) {
              Text(
                text = String.format(Locale.GERMAN, "Mock-Aufnahme gespeichert (%02d:%02d)", audioSavedDuration / 60, audioSavedDuration % 60),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            } else {
              Text(
                text = "Bereit zur Aufnahme",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Control Button
          Button(
            onClick = {
              if (isRecording) {
                isRecording = false
                audioSavedDuration = recordedSeconds
              } else {
                isRecording = true
                recordedSeconds = 0
                audioSavedDuration = 0
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
            )
          ) {
            Text(if (isRecording) "Aufnahme stoppen" else "Aufnahme starten")
          }
        }
      }

      // Notes Input
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notizen & Reflexion") },
        placeholder = { Text("Wie lief das Üben? Was klappte gut, was erfordert noch Arbeit?") },
        modifier = Modifier
          .fillMaxWidth()
          .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        maxLines = 5
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Save Button
      Button(
        onClick = {
          if (pieceTitle.isNotBlank()) {
            val duration = durationStr.toIntOrNull() ?: 30
            val todayFormatted = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Date())
            val newSession = PracticeSession(
              date = todayFormatted,
              piece = pieceTitle,
              durationMinutes = duration,
              mood = selectedMood,
              focusAreas = selectedFocus.toList(),
              notes = notes,
              audioDurationSeconds = audioSavedDuration
            )
            RepositoryProvider.repository.addSession(newSession)
            onBack()
          }
        },
        enabled = pieceTitle.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = "Speichern")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Übungseinheit speichern", fontWeight = FontWeight.Bold)
      }
    }
  }
}
