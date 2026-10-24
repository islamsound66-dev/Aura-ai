package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.storage.PublicFolderType
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun ExportAudioDialog(
    audioFile: File,
    title: String,
    onExportToPublicFolder: (PublicFolderType) -> Unit,
    onExportToSafUri: (Uri) -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/wav")
    ) { uri: Uri? ->
        if (uri != null) {
            onExportToSafUri(uri)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = GoldAmber,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Export Audio File (.wav)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldAmber
                    )
                    Text(
                        text = "Select public device destination",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Option 1: Music Folder
                ExportOptionCard(
                    icon = Icons.Default.LibraryMusic,
                    title = "Public Music Folder",
                    subtitle = "Saves to /Music/DocuVoice/ directory",
                    tag = "export_music_folder",
                    iconColor = GoldAmber,
                    iconBg = GoldContainer,
                    onClick = {
                        onExportToPublicFolder(PublicFolderType.MUSIC)
                        onDismiss()
                    }
                )

                // Option 2: Documents Folder
                ExportOptionCard(
                    icon = Icons.Default.Description,
                    title = "Public Documents Folder",
                    subtitle = "Saves to /Documents/DocuVoice/ directory",
                    tag = "export_documents_folder",
                    iconColor = CyanAccent,
                    iconBg = CyanContainer,
                    onClick = {
                        onExportToPublicFolder(PublicFolderType.DOCUMENTS)
                        onDismiss()
                    }
                )

                // Option 3: System File Picker (Custom Location / SD / Downloads)
                ExportOptionCard(
                    icon = Icons.AutoMirrored.Filled.DriveFileMove,
                    title = "Choose Custom Location",
                    subtitle = "Select exact folder, USB drive, or cloud drive",
                    tag = "export_saf_picker",
                    iconColor = Color(0xFFA78BFA),
                    iconBg = Color(0xFF2E1065),
                    onClick = {
                        val sanitized = title.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(24)
                        createDocumentLauncher.launch("DocuVoice_${sanitized}.wav")
                    }
                )

                // Option 4: Share with Apps
                ExportOptionCard(
                    icon = Icons.Default.Share,
                    title = "Share via Android",
                    subtitle = "Send via Drive, WhatsApp, Telegram, Email",
                    tag = "export_share_sheet",
                    iconColor = TextPrimary,
                    iconBg = StudioSurfaceVariant,
                    onClick = {
                        onShare()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ExportOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    iconColor: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
        border = BorderStroke(1.dp, StudioBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
