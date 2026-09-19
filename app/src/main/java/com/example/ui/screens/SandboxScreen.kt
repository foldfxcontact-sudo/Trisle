package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.IslandLayoutState
import com.example.model.ActivityType
import com.example.model.CutoutProfile
import com.example.model.IslandActivity
import com.example.model.LicenseState
import com.example.model.LicenseTier
import com.example.ui.components.TrisleIslandContainer
import com.example.ui.theme.CarbonBackground
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.GraphiteSurfaceVariant
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
fun SandboxScreen(
  layoutState: IslandLayoutState,
  cutoutProfile: CutoutProfile,
  licenseState: LicenseState,
  activeActivities: List<IslandActivity>,
  isSystemOverlayActive: Boolean,
  onToggleSystemOverlay: () -> Unit,
  onToggleMusic: () -> Unit,
  onToggleTimer: () -> Unit,
  onToggleCall: () -> Unit,
  onToggleMessage: () -> Unit,
  onTogglePlayback: () -> Unit,
  onSatelliteClick: (IslandActivity) -> Unit,
  onDismissSatellite: (String) -> Unit,
  onAdjustTimer: (Int) -> Unit,
  onDismissCall: () -> Unit,
  onClearAll: () -> Unit,
  onResetDefault: () -> Unit,
  onNavigateToPro: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  val isCallActive = activeActivities.any { it.type == ActivityType.CALL }
  val isTimerActive = activeActivities.any { it.type == ActivityType.TIMER }
  val isMusicActive = activeActivities.any { it.type == ActivityType.MEDIA }
  val isMsgActive = activeActivities.any { it.type == ActivityType.MESSAGE }

  val activeMusic = activeActivities.firstOrNull { it.type == ActivityType.MEDIA }
  val activeTimer = activeActivities.firstOrNull { it.type == ActivityType.TIMER }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // 1. Live Interactive Sandbox Canvas
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("island_preview_card"),
      colors = CardDefaults.cardColors(containerColor = CarbonBackground),
      shape = RoundedCornerShape(20.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorder))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (licenseState.tier == LicenseTier.PRO_LIFETIME) Color(0xFF4ADE80) else TitaniumSilver)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (licenseState.tier == LicenseTier.PRO_LIFETIME) "TRI-NODE MULTI-TASKING (PRO)" else "SINGLE ANCHOR PILL (FREE)",
              color = TextMediumContrast,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.SemiBold
            )
          }

          Text(
            text = "${layoutState.totalActiveCount} Active Activity${if (layoutState.totalActiveCount == 1) "" else "ies"}",
            color = TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Render the Interactive Island (Anchor + Satellites)
        TrisleIslandContainer(
          layoutState = layoutState,
          profile = cutoutProfile,
          licenseTier = licenseState.tier,
          onTogglePlayback = onTogglePlayback,
          onSatelliteClick = onSatelliteClick,
          onSatelliteDismiss = onDismissSatellite,
          onAdjustTimer = onAdjustTimer,
          onDismissCall = onDismissCall
        )

        Spacer(modifier = Modifier.height(18.dp))
        Text(
          text = "Tap island to expand controls • Tap satellite to focus",
          color = TextMuted,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Floating System Overlay Switch Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorderSubtle))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 12.dp),
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
            Icon(
              imageVector = Icons.Default.PowerSettingsNew,
              contentDescription = null,
              tint = if (isSystemOverlayActive) Color(0xFF4ADE80) else TitaniumSilver,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "System-Wide Floating Notch Overlay",
              color = TextHighContrast,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = if (isSystemOverlayActive) "Active over status bar & all apps" else "Draw notch over other apps",
              color = if (isSystemOverlayActive) Color(0xFF4ADE80) else TextMuted,
              fontSize = 11.sp
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!Settings.canDrawOverlays(context)) {
            OutlinedButton(
              onClick = {
                try {
                  val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                  )
                  context.startActivity(intent)
                } catch (_: Exception) {}
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(32.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = PlatinumWhite)
            ) {
              Text("Grant", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
          } else {
            Switch(
              checked = isSystemOverlayActive,
              onCheckedChange = { onToggleSystemOverlay() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = PlatinumWhite,
                uncheckedThumbColor = TitaniumSilver,
                uncheckedTrackColor = CarbonBackground
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Activity Event Deck Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "ACTIVITY STREAM DECK",
        color = TextMediumContrast,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp)
      )

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Tap to toggle state",
          color = TextMuted,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2x2 Activity Grid Cards
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        LiveActivityCard(
          title = "Call",
          subtitle = if (isCallActive) "In Call • Hotline" else "Tap to ring",
          tierLabel = "Tier 1 Urgent",
          icon = Icons.Default.Call,
          isActive = isCallActive,
          activeColor = Color(0xFF4ADE80),
          modifier = Modifier.weight(1f),
          onClick = onToggleCall
        )

        LiveActivityCard(
          title = "Timer",
          subtitle = if (isTimerActive) activeTimer?.title ?: "04:30" else "Tap to start",
          tierLabel = "Tier 2 Time",
          icon = Icons.Default.Timer,
          isActive = isTimerActive,
          activeColor = Color(0xFFFBBF24),
          modifier = Modifier.weight(1f),
          onClick = onToggleTimer
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        LiveActivityCard(
          title = "Music",
          subtitle = if (isMusicActive) if (activeMusic?.isPlaying == true) "Playing • M83" else "Paused" else "Tap to play",
          tierLabel = "Tier 3 Continuous",
          icon = Icons.Default.MusicNote,
          isActive = isMusicActive,
          activeColor = TitaniumSilver,
          modifier = Modifier.weight(1f),
          onClick = onToggleMusic
        )

        LiveActivityCard(
          title = "Message",
          subtitle = if (isMsgActive) "Alert • Alex Chen" else "Tap to ping",
          tierLabel = "Tier 4 Ephemeral",
          icon = Icons.Default.ChatBubble,
          isActive = isMsgActive,
          activeColor = Color(0xFF60A5FA),
          modifier = Modifier.weight(1f),
          onClick = onToggleMessage
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Deck Management Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedButton(
        onClick = onClearAll,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMediumContrast)
      ) {
        Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Clear All", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }

      OutlinedButton(
        onClick = onResetDefault,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMediumContrast)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Reset Deck", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Pro Tier Upsell Banner if on Free tier
    if (licenseState.tier == LicenseTier.FREE) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToPro() },
        colors = CardDefaults.cardColors(containerColor = GraphiteSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorder))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(CarbonBackground)
              .border(1.dp, SteelBorder, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Stars,
              contentDescription = null,
              tint = PlatinumWhite,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Unlock Tri-Node Satellites",
              color = TextHighContrast,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Detach 2 satellite bubbles simultaneously • $5.99 lifetime",
              color = TextMediumContrast,
              fontSize = 11.sp
            )
          }
          Text(
            text = "PRO →",
            color = PlatinumWhite,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun LiveActivityCard(
  title: String,
  subtitle: String,
  tierLabel: String,
  icon: ImageVector,
  isActive: Boolean,
  activeColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(if (isActive) GraphiteSurface else CarbonBackground)
      .border(
        width = if (isActive) 1.2.dp else 0.8.dp,
        color = if (isActive) activeColor else SteelBorderSubtle,
        shape = RoundedCornerShape(14.dp)
      )
      .clickable { onClick() }
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isActive) activeColor else TitaniumSilver,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = title,
            color = TextHighContrast,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else SteelBorder)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = subtitle,
        color = if (isActive) TextHighContrast else TextMuted,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = tierLabel,
        color = if (isActive) activeColor.copy(alpha = 0.8f) else TextMuted,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
