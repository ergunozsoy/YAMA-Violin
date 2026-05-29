package com.example.yamaviolin.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
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
    // Page Title
    Text(
      text = "Über YAMA Violin",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onBackground
    )

    // 1. Philosophie Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Unsere Philosophie",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
        Text(
          text = "YAMA Violin versteht Üben nicht nur als Wiederholung, sondern als bewussten Prozess des Hörens, Reflektierens und Wachsens. Die App hilft Geigerinnen und Geigern, eigene Aufnahmen systematisch zu dokumentieren, gezielt zu analysieren und die musikalische Entwicklung über längere Zeit sichtbar zu machen.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Justify
        )
      }
    }

    // 2. So funktioniert es Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.List,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "So funktioniert es",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
        
        val steps = listOf(
          "Nehmen Sie eine Übungseinheit auf oder importieren Sie eine frühere Aufnahme.",
          "Hören Sie die Aufnahme bewusst an.",
          "Markieren Sie wichtige Stellen mit Zeitangaben.",
          "Notieren Sie technische, musikalische und klangliche Beobachtungen.",
          "Formulieren Sie ein konkretes nächstes Übungsziel.",
          "Vergleichen Sie spätere Aufnahmen mit früheren Einträgen."
        )

        steps.forEachIndexed { index, step ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "${index + 1}.",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.secondary
            )
            Text(
              text = step,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // 3. Feedback Card
    val context = LocalContext.current
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Feedback zur Testversion",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
        
        Text(
          text = "Dies ist eine frühe Testversion von YAMA Violin.\n\nDeine Rückmeldung hilft uns sehr, die App musikalisch sinnvoller, stabiler und benutzerfreundlicher zu machen.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = "Bitte achte beim Testen besonders auf:",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        val feedbackQuestions = listOf(
          "Funktioniert die Aufnahme zuverlässig?",
          "Sind die Analysehinweise musikalisch hilfreich?",
          "Sind „Stelle anhören“ und „Mit Kontext anhören“ praktisch?",
          "Gibt es unklare Begriffe oder verwirrende Stellen?",
          "Was fehlt dir beim Üben?"
        )

        feedbackQuestions.forEach { question ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "•",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = question,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Feedback bitte an: feedback@yamaviolin.de",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedButton(
          onClick = {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
              data = Uri.parse("mailto:feedback@yamaviolin.de")
              putExtra(Intent.EXTRA_SUBJECT, "Feedback zu YAMA Violin Testversion")
            }
            try {
              context.startActivity(emailIntent)
            } catch (e: Exception) {
              // Fallback if no email app
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = "Feedback senden",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    // 4. Urheberrecht Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Urheberrecht & Nutzungsbedingungen",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
        Text(
          text = "© 2026 YAMA Violin. Alle Rechte vorbehalten. Diese App ist als persönliches Übungs- und Reflexionswerkzeug konzipiert. Hochgeladene oder importierte Aufnahmen bleiben persönliche Übungsdaten der Nutzerinnen und Nutzer. Bei der Verwendung urheberrechtlich geschützter Musikstücke sind die jeweils geltenden Rechte und Lizenzbedingungen zu beachten.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Justify
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
