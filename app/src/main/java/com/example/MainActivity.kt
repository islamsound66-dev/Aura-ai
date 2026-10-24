package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DocuVoiceViewModel
import com.example.ui.screens.ExportPipelineScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DocuVoiceApp()
            }
        }
    }
}

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun DocuVoiceApp(
    viewModel: DocuVoiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val navItems = listOf(
        NavItem("Studio", Icons.Filled.Mic, Icons.Outlined.Mic, "nav_studio"),
        NavItem("Presets", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_presets"),
        NavItem("Pipeline", Icons.Filled.Terminal, Icons.Outlined.Terminal, "nav_pipeline"),
        NavItem("Guide", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "nav_guide"),
        NavItem("Library", Icons.Filled.History, Icons.Outlined.History, "nav_library")
    )

    // Handle back button on secondary tabs
    if (uiState.activeTab != 0) {
        BackHandler {
            viewModel.setActiveTab(0)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg),
        bottomBar = {
            NavigationBar(
                containerColor = StudioCardBg,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = uiState.activeTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setActiveTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) GoldAmber else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) GoldAmber else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GoldAmber.copy(alpha = 0.15f),
                            selectedIconColor = GoldAmber,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = GoldAmber,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                0 -> StudioScreen(viewModel = viewModel)
                1 -> PresetsScreen(viewModel = viewModel)
                2 -> ExportPipelineScreen(viewModel = viewModel)
                3 -> GuideScreen(viewModel = viewModel)
                4 -> HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
