package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CutoutProfile
import com.example.ui.theme.CarbonBackground
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelBorderSubtle
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TitaniumSilver

@Composable
fun ExclusionsScreen(
  profile: CutoutProfile,
  excludedApps: Set<String>,
  onToggleLandscape: (Boolean) -> Unit,
  onToggleLockScreen: (Boolean) -> Unit,
  onToggleAppExclusion: (String) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Text(
      text = "POWER & DISPLAY DISCIPLINE",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    // Power & Display Rules Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorderSubtle))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        SettingToggleRow(
          title = "Zero-Drain Screen Off",
          description = "Immediately halts all timers and view rendering when the device screen turns off.",
          icon = Icons.Default.PowerSettingsNew,
          checked = true,
          enabled = false // Always enabled per core battery discipline
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingToggleRow(
          title = "Hide in Landscape",
          description = "Prevents island from obstructing games and horizontal media playback.",
          icon = Icons.Default.ScreenRotation,
          checked = profile.hideInLandscape,
          onCheckedChange = onToggleLandscape
        )

        Spacer(modifier = Modifier.height(14.dp))

        SettingToggleRow(
          title = "Hide on Lock Screen",
          description = "Only render islands when the device is unlocked and in user control.",
          icon = Icons.Default.Security,
          checked = profile.hideOnLockScreen,
          onCheckedChange = onToggleLockScreen
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "APP EXCLUSION LIST",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = CarbonBackground),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorder))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "When excluded apps are foregrounded, Trisle automatically dismisses all overlays to guarantee an unobstructed screen:",
          color = TextMediumContrast,
          fontSize = 11.sp,
          lineHeight = 16.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        AppExclusionRow(
          name = "Camera Viewfinder",
          pkg = "com.google.android.GoogleCamera",
          icon = Icons.Default.CameraAlt,
          isExcluded = excludedApps.contains("com.google.android.GoogleCamera"),
          onToggle = { onToggleAppExclusion("com.google.android.GoogleCamera") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppExclusionRow(
          name = "YouTube & Fullscreen Video",
          pkg = "com.google.android.youtube",
          icon = Icons.Default.Videocam,
          isExcluded = excludedApps.contains("com.google.android.youtube"),
          onToggle = { onToggleAppExclusion("com.google.android.youtube") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppExclusionRow(
          name = "Netflix Media Client",
          pkg = "com.netflix.mediaclient",
          icon = Icons.Default.Videocam,
          isExcluded = excludedApps.contains("com.netflix.mediaclient"),
          onToggle = { onToggleAppExclusion("com.netflix.mediaclient") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppExclusionRow(
          name = "Mobile Games & High-FPS Titles",
          pkg = "com.android.games",
          icon = Icons.Default.Gamepad,
          isExcluded = excludedApps.contains("com.android.games"),
          onToggle = { onToggleAppExclusion("com.android.games") }
        )
      }
    }
  }
}

@Composable
private fun SettingToggleRow(
  title: String,
  description: String,
  icon: ImageVector,
  checked: Boolean,
  enabled: Boolean = true,
  onCheckedChange: ((Boolean) -> Unit)? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(CarbonBackground)
          .border(0.8.dp, SteelBorder, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = TitaniumSilver, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(text = title, color = TextHighContrast, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(text = description, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = if (enabled) onCheckedChange else null,
      enabled = enabled,
      colors = SwitchDefaults.colors(
        checkedThumbColor = ObsidianBlack,
        checkedTrackColor = PlatinumWhite,
        uncheckedThumbColor = TitaniumSilver,
        uncheckedTrackColor = CarbonBackground
      )
    )
  }
}

@Composable
private fun AppExclusionRow(
  name: String,
  pkg: String,
  icon: ImageVector,
  isExcluded: Boolean,
  onToggle: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(GraphiteSurface)
      .border(0.8.dp, SteelBorderSubtle, RoundedCornerShape(10.dp))
      .padding(horizontal = 12.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = TitaniumSilver, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(name, color = TextHighContrast, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(pkg, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
      }
    }

    Switch(
      checked = isExcluded,
      onCheckedChange = { onToggle() },
      colors = SwitchDefaults.colors(
        checkedThumbColor = ObsidianBlack,
        checkedTrackColor = PlatinumWhite,
        uncheckedThumbColor = TitaniumSilver,
        uncheckedTrackColor = CarbonBackground
      )
    )
  }
}
