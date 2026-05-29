package com.example.yamaviolin.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.yamaviolin.NewEntry
import com.example.yamaviolin.data.RepositoryProvider
import com.example.yamaviolin.ui.about.AboutScreen
import com.example.yamaviolin.ui.dashboard.DashboardScreen
import com.example.yamaviolin.ui.history.HistoryScreen
import com.example.yamaviolin.ui.tools.ToolsScreen

@Composable
fun MainScreen(
  onNavigate: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(RepositoryProvider.repository) },
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var selectedTab by remember { mutableIntStateOf(0) }

  Scaffold(
    modifier = modifier,
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(Icons.Default.Home, contentDescription = "Übersicht") },
          label = { Text("Übersicht") }
        )
        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(Icons.Default.List, contentDescription = "Tagebuch") },
          label = { Text("Tagebuch") }
        )
        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = { Icon(Icons.Default.Build, contentDescription = "Werkzeuge") },
          label = { Text("Werkzeuge") }
        )
        NavigationBarItem(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          icon = { Icon(Icons.Default.Info, contentDescription = "Über YAMA") },
          label = { Text("Info") }
        )
      }
    },
    floatingActionButton = {
      if (selectedTab != 2 && selectedTab != 3) {
        FloatingActionButton(
          onClick = { onNavigate(NewEntry) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
          Icon(Icons.Default.Add, contentDescription = "Neuer Eintrag")
        }
      }
    }
  ) { innerPadding ->
    val contentModifier = Modifier
      .fillMaxSize()
      .padding(innerPadding)

    when (val state = uiState) {
      is MainScreenUiState.Loading -> {
        Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is MainScreenUiState.Success -> {
        when (selectedTab) {
          0 -> DashboardScreen(
            sessions = state.data,
            onNavigate = onNavigate,
            modifier = contentModifier
          )
          1 -> HistoryScreen(
            sessions = state.data,
            onNavigate = onNavigate,
            modifier = contentModifier
          )
          2 -> ToolsScreen(
            modifier = contentModifier
          )
          3 -> AboutScreen(
            modifier = contentModifier
          )
        }
      }
      is MainScreenUiState.Error -> {
        Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
          Text(text = "Fehler beim Laden: ${state.throwable.localizedMessage}")
        }
      }
    }
  }
}

