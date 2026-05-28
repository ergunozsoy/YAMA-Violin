package com.example.yamaviolin.ui.tools

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ToolsScreen(
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // Title
    Text(
      text = "Werkzeuge",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )

    // Metronome Section
    MetronomeCard()

    // Tuner Section
    TunerCard()
  }
}

@Composable
fun MetronomeCard() {
  var bpm by remember { mutableIntStateOf(100) }
  var isPlaying by remember { mutableStateOf(false) }
  var isSoundEnabled by remember { mutableStateOf(true) }
  var flashActive by remember { mutableStateOf(false) }

  // Tick generator
  LaunchedEffect(isPlaying, bpm, isSoundEnabled) {
    if (isPlaying) {
      val intervalMs = (60000 / bpm).toLong()
      val toneGen = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 70)
      } catch (e: Exception) {
        null
      }
      while (isPlaying) {
        flashActive = true
        if (isSoundEnabled) {
          toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        }
        delay(60)
        flashActive = false
        delay(intervalMs - 60)
      }
      toneGen?.release()
    }
  }

  val flashColor by animateColorAsState(
    targetValue = if (flashActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
    animationSpec = tween(durationMillis = 50),
    label = "FlashColor"
  )

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Metronom",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
      )

      // Visual Flasher
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(flashColor),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = bpm.toString(),
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = if (flashActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
        )
      }

      Text(
        text = "BPM (Beats per Minute)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      )

      // BPM Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        IconButton(
          onClick = { if (bpm > 40) bpm-- },
          enabled = bpm > 40
        ) {
          Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Slider(
          value = bpm.toFloat(),
          onValueChange = { bpm = it.toInt() },
          valueRange = 40f..220f,
          modifier = Modifier.weight(1f)
        )

        IconButton(
          onClick = { if (bpm < 220) bpm++ },
          enabled = bpm < 220
        ) {
          Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
      }

      // Audio Toggle & Play Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(text = "Ton", style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = isSoundEnabled,
            onCheckedChange = { isSoundEnabled = it }
          )
        }

        Button(
          onClick = { isPlaying = !isPlaying },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isPlaying) Color(0xFFC0392B) else MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(if (isPlaying) "Stop" else "Start", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun TunerCard() {
  val strings = listOf(
    "G" to "196 Hz",
    "D" to "294 Hz",
    "A" to "440 Hz",
    "E" to "659 Hz"
  )
  var selectedStringIdx by remember { mutableIntStateOf(2) } // default A (440Hz)
  var isTuning by remember { mutableStateOf(false) }
  var centDeviation by remember { mutableFloatStateOf(0f) }

  // Simulated cent pointer oscillation
  LaunchedEffect(isTuning) {
    if (isTuning) {
      while (isTuning) {
        delay(150)
        // Keep it oscillating near center to show it's "mostly in tune"
        centDeviation = (-15..15).random().toFloat()
      }
    } else {
      centDeviation = 0f
    }
  }

  val needleAngle by animateFloatAsState(
    targetValue = centDeviation,
    animationSpec = tween(durationMillis = 150),
    label = "NeedleAngle"
  )

  val needleColor = when {
    !isTuning -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    needleAngle in -3f..3f -> Color(0xFF27AE60) // Green when in-tune
    else -> MaterialTheme.colorScheme.secondary // Amber when flat/sharp
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Stimmgerät",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
      )

      // Selected String Details
      Text(
        text = if (isTuning) "${strings[selectedStringIdx].first}-Saite (${strings[selectedStringIdx].second})" else "Stimmgerät inaktiv",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (isTuning) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
      )

      // Needle Cent Dial UI
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp),
        contentAlignment = Alignment.Center
      ) {
        val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        val fontLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        Canvas(modifier = Modifier.fillMaxSize()) {
          val width = size.width
          val height = size.height
          val center = Offset(width / 2f, height)

          // Draw dial arc bounds
          val radius = height - 10.dp.toPx()

          // Draw grid ticks
          for (deg in -45..45 step 15) {
            val rad = (deg - 90) * PI / 180f
            val startX = center.x + (radius - 12.dp.toPx()) * cos(rad).toFloat()
            val startY = center.y + (radius - 12.dp.toPx()) * sin(rad).toFloat()
            val endX = center.x + radius * cos(rad).toFloat()
            val endY = center.y + radius * sin(rad).toFloat()

            drawLine(
              color = lineColor,
              start = Offset(startX, startY),
              end = Offset(endX, endY),
              strokeWidth = 2.dp.toPx()
            )
          }

          // Draw needle
          // Maps -50 cent to -45 deg, +50 cent to +45 deg
          val needleDeg = (needleAngle * 0.9f) - 90f
          val needleRad = needleDeg * PI / 180f
          val needleLength = radius - 4.dp.toPx()
          val needleX = center.x + needleLength * cos(needleRad).toFloat()
          val needleY = center.y + needleLength * sin(needleRad).toFloat()

          drawLine(
            color = needleColor,
            start = center,
            end = Offset(needleX, needleY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
          )

          // Draw pivot center
          drawCircle(
            color = needleColor,
            radius = 6.dp.toPx(),
            center = center
          )
        }

        // Flat/Sharp Labels
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 24.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "b (tief)", style = MaterialTheme.typography.labelSmall, color = fontLabelColor)
          if (isTuning && needleAngle in -3f..3f) {
            Text(
              text = "IN TUNE",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF27AE60)
            )
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }
          Text(text = "# (hoch)", style = MaterialTheme.typography.labelSmall, color = fontLabelColor)
        }
      }

      // String selector buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        strings.forEachIndexed { idx, (stringName, _) ->
          val isSelected = selectedStringIdx == idx
          Card(
            modifier = Modifier
              .weight(1f)
              .clickable { selectedStringIdx = idx },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = if (isSelected) null else CardDefaults.outlinedCardBorder()
          ) {
            Box(
              modifier = Modifier.padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = stringName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
      }

      // Tuner Start Toggle Button
      Button(
        onClick = { isTuning = !isTuning },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isTuning) Color(0xFFC0392B) else MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(if (isTuning) "Stimmgerät stoppen" else "Stimmgerät aktivieren", fontWeight = FontWeight.Bold)
      }
    }
  }
}
