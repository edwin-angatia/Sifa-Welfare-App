package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.repository.WelfareRepository
import com.example.ui.WelfareMainScreen
import com.example.ui.WelfareViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.WelfareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WelfareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val repository = remember { WelfareRepository(context) }
    val activeMember by repository.activeMemberFlow.collectAsState(initial = repository.getActiveMember())
    val isAdmin = repository.isAdmin()

    val isAuthenticated = activeMember != null || isAdmin

    Crossfade(targetState = isAuthenticated, label = "AuthGateTransition") { authenticated ->
        if (!authenticated) {
            AuthScreen(
                repository = repository,
                onAuthSuccess = {
                    // State automatically triggers recomposition
                }
            )
        } else {
            val viewModel = remember(activeMember?.id ?: "admin") {
                WelfareViewModel(repository)
            }
            WelfareMainScreen(viewModel = viewModel)
        }
    }
}
