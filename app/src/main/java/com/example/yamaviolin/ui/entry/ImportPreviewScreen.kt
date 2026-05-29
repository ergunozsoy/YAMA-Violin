package com.example.yamaviolin.ui.entry

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.yamaviolin.data.PracticeSession
import com.example.yamaviolin.data.RepositoryProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImportPreviewScreen(
  uriString: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val uri = remember(uriString) { Uri.parse(uriString) }

  // Extracted Metadata States
  var fileName by remember { mutableStateOf("Unbekannt") }
  var fileType by remember { mutableStateOf("Audio") }
  var durationMs by remember { mutableLongStateOf(0L) }
  var importDate by remember { mutableStateOf("") }

  // Form States
  var pieceTitle by remember { mutableStateOf("") }
  var composer by remember { mutableStateOf("") }
  val focusOptions = listOf("Intonation", "Bogenführung", "Tempo", "Fingersatz", "Rhythmus", "Dynamik")
  val selectedFocus = remember { mutableStateListOf<String>() }
  var notes by remember { mutableStateOf("") }

  val recordTypes = listOf("Eigenaufnahme", "Unterricht", "Konzert / Aufführung", "Probe", "Sonstiges")
  var typeExpanded by remember { mutableStateOf(false) }
  var selectedType by remember { mutableStateOf(recordTypes[0]) }

  val categories = listOf("Sonstiges", "Etüde", "Tonleiter", "Konzertstück")
  var categoryExpanded by remember { mutableStateOf(false) }
  var selectedCategory by remember { mutableStateOf(categories[0]) }

  // Auto-suggestion indicators
  var suggestedComposer by remember { mutableStateOf<String?>(null) }
  var suggestedPiece by remember { mutableStateOf<String?>(null) }
  var suggestedType by remember { mutableStateOf<String?>(null) }
  var suggestedCategory by remember { mutableStateOf<String?>(null) }
  var durationHint by remember { mutableStateOf<String?>(null) }

  // Extract Metadata using ContentResolver & MediaMetadataRetriever
  LaunchedEffect(uri) {
    try {
      context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIdx != -1) {
          fileName = cursor.getString(nameIdx)
        }
      }
    } catch (e: Exception) {
      fileName = "aufnahme_${System.currentTimeMillis()}"
    }

    try {
      val mime = context.contentResolver.getType(uri)
      if (mime != null) {
        fileType = mime
      }
    } catch (e: Exception) {
      fileType = "audio/*"
    }

    try {
      val retriever = android.media.MediaMetadataRetriever()
      retriever.setDataSource(context, uri)
      val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
      durationMs = durStr?.toLongOrNull() ?: 0L
      retriever.release()
    } catch (e: Exception) {
      durationMs = 0L
    }

    importDate = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(Date())
  }

  // Process rules based on name and duration
  LaunchedEffect(fileName, durationMs) {
    if (fileName == "Unbekannt" || fileName.isBlank()) return@LaunchedEffect

    // Reset suggestions
    suggestedComposer = null
    suggestedPiece = null
    suggestedType = null
    suggestedCategory = null
    durationHint = null

    // Piece & Composer suggestions
    if (fileName.contains("Bach", ignoreCase = true)) {
      suggestedComposer = "Bach"
      composer = "Bach"
    }
    if (fileName.contains("Allemande", ignoreCase = true)) {
      suggestedPiece = "Allemande"
      pieceTitle = "Allemande"
    } else {
      val dotIdx = fileName.lastIndexOf('.')
      pieceTitle = if (dotIdx != -1) fileName.substring(0, dotIdx) else fileName
    }

    // Category suggestions
    if (fileName.contains("Kreutzer", ignoreCase = true)) {
      suggestedCategory = "Etüde"
      selectedCategory = "Etüde"
    }

    // Record type suggestions
    if (fileName.contains("Unterricht", ignoreCase = true) ||
      fileName.contains("lesson", ignoreCase = true) ||
      fileName.contains("Stunde", ignoreCase = true)) {
      suggestedType = "Unterricht"
      selectedType = "Unterricht"
    } else if (fileName.contains("concert", ignoreCase = true) ||
      fileName.contains("Konzert", ignoreCase = true) ||
      fileName.contains("Aufführung", ignoreCase = true)) {
      suggestedType = "Konzert / Aufführung"
      selectedType = "Konzert / Aufführung"
    }

    // Duration suggestions
    if (durationMs > 0) {
      if (durationMs < 120_000) {
        durationHint = "kurzer Übungsausschnitt"
      } else if (durationMs > 600_000) {
        durationHint = "längere Aufnahme / Unterricht / Probe"
      }
    }
  }

  val hasAnySuggestions = suggestedComposer != null || suggestedPiece != null ||
    suggestedType != null || suggestedCategory != null || durationHint != null

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text("Aufnahme importieren", fontWeight = FontWeight.Bold) },
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. File Metadata Preview Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Datei-Metadaten",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          
          MetadataRow(label = "Dateiname", value = fileName)
          MetadataRow(label = "Dateityp", value = fileType)
          MetadataRow(
            label = "Dauer",
            value = if (durationMs > 0) formatDuration(durationMs) else "Nicht verfügbar"
          )
          MetadataRow(label = "Importdatum", value = importDate)
        }
      }

      // 2. Automatic Suggestions Card
      if (hasAnySuggestions) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
          ),
          border = CardDefaults.outlinedCardBorder()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                text = "Automatische Zuordnung (Vorschlag)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
              )
            }
            Text(
              text = "Vorschlag – bitte überprüfen. Erkennung basiert auf Dateiname und Dateilänge.",
              style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            suggestedComposer?.let {
              SuggestionItem(label = "Möglicher Komponist", value = it)
            }
            suggestedPiece?.let {
              SuggestionItem(label = "Mögliches Musikstück", value = it)
            }
            suggestedType?.let {
              SuggestionItem(label = "Vorgeschlagener Aufnahmetyp", value = it)
            }
            suggestedCategory?.let {
              SuggestionItem(label = "Vorgeschlagene Kategorie", value = it)
            }
            durationHint?.let {
              SuggestionItem(label = "Dauer-Hinweis", value = it)
            }
          }
        }
      }

      // 3. Edit Form Title
      Text(
        text = "Eigenschaften zuordnen",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )

      // Piece Title
      OutlinedTextField(
        value = pieceTitle,
        onValueChange = { pieceTitle = it },
        label = { Text("Stücktitel") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      // Composer
      OutlinedTextField(
        value = composer,
        onValueChange = { composer = it },
        label = { Text("Komponist (optional)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      // Record Type Dropdown
      ExposedDropdownMenuBox(
        expanded = typeExpanded,
        onExpandedChange = { typeExpanded = !typeExpanded }
      ) {
        OutlinedTextField(
          value = selectedType,
          onValueChange = {},
          readOnly = true,
          label = { Text("Aufnahmetyp") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
          modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
          expanded = typeExpanded,
          onDismissRequest = { typeExpanded = false }
        ) {
          recordTypes.forEach { type ->
            DropdownMenuItem(
              text = { Text(type) },
              onClick = {
                selectedType = type
                typeExpanded = false
              }
            )
          }
        }
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

      // Focus Areas
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

      // Notes
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notiz / Beschreibung") },
        placeholder = { Text("Füge Gedanken, Notizen oder Ziele für diese Aufnahme hinzu...") },
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        maxLines = 4
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Save Button
        Button(
          onClick = {
            if (pieceTitle.isNotBlank()) {
              val localAudioPath = copyUriToPrivateStorage(context, uri, fileName)
              val formattedTitle = if (composer.isNotBlank()) "$pieceTitle (comp. $composer)" else pieceTitle
              val newSession = PracticeSession(
                date = importDate,
                piece = formattedTitle,
                durationMinutes = null,
                mood = "Ok",
                focusAreas = selectedFocus.toList(),
                notes = if (notes.isBlank()) "Importierte Aufnahme aus Datei: $fileName" else notes,
                audioDurationSeconds = if (durationMs > 0) (durationMs / 1000).toInt() else 60,
                isImported = true,
                audioUri = localAudioPath,
                audioSource = "Importierte Aufnahme",
                originalFileName = fileName,
                recordingDate = importDate,
                importDate = importDate,
                autoHints = listOf(
                  "Diese Aufnahme wurde erfolgreich importiert. (Vorschlag – bitte überprüfen)",
                  "Die Zuordnung basiert auf Dateiname und Metadaten. (Automatische Zuordnung)",
                  "Manuelle Bewertung empfohlen – Bitte kontrolliere die detaillierten Zeitmarken."
                )
              )
              RepositoryProvider.repository.addSession(newSession)
              onBack()
            }
          },
          enabled = pieceTitle.isNotBlank(),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("In Archiv übernehmen", fontWeight = FontWeight.Bold)
        }

        // Save Later (Defaults) Button
        TextButton(
          onClick = {
            val localAudioPath = copyUriToPrivateStorage(context, uri, fileName)
            val fallbackTitle = if (fileName != "Unbekannt" && fileName.isNotBlank()) {
              val dotIdx = fileName.lastIndexOf('.')
              if (dotIdx != -1) fileName.substring(0, dotIdx) else fileName
            } else {
              "Importierte Aufnahme"
            }
            val newSession = PracticeSession(
              date = importDate,
              piece = fallbackTitle,
              durationMinutes = null,
              mood = "Ok",
              focusAreas = emptyList(),
              notes = "Schnellimportiert am $importDate. Datei: $fileName",
              audioDurationSeconds = if (durationMs > 0) (durationMs / 1000).toInt() else 60,
              isImported = true,
              audioUri = localAudioPath,
              audioSource = "Importierte Aufnahme",
              originalFileName = fileName,
              recordingDate = importDate,
              importDate = importDate,
              autoHints = listOf(
                "Diese Aufnahme wurde erfolgreich importiert. (Vorschlag – bitte überprüfen)",
                "Die Zuordnung basiert auf Dateiname und Metadaten. (Automatische Zuordnung)",
                "Manuelle Bewertung empfohlen."
              )
            )
            RepositoryProvider.repository.addSession(newSession)
            onBack()
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Später zuordnen", color = MaterialTheme.colorScheme.secondary)
        }

        // Cancel Button
        TextButton(
          onClick = onBack,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Abbrechen", color = MaterialTheme.colorScheme.error)
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun MetadataRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.End,
      modifier = Modifier.width(180.dp)
    )
  }
}

@Composable
fun SuggestionItem(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "$label:",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.secondary,
      textAlign = TextAlign.End
    )
  }
}

private fun formatDuration(ms: Long): String {
  val totalSec = ms / 1000
  val min = totalSec / 60
  val sec = totalSec % 60
  return String.format(Locale.GERMAN, "%02d:%02d", min, sec)
}

private fun copyUriToPrivateStorage(context: android.content.Context, uri: Uri, fileName: String): String? {
  return try {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val destFile = java.io.File(context.filesDir, "imported_${System.currentTimeMillis()}_$fileName")
    destFile.outputStream().use { output ->
      inputStream.use { input ->
        input.copyTo(output)
      }
    }
    destFile.absolutePath
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}
