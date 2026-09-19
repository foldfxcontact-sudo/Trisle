package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun PermissionsScreen(
  isAccessibilityActive: Boolean,
  isNotificationActive: Boolean
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Text(
      text = "SYSTEM PERMISSIONS & INTEGRATION",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    // 1. Accessibility Service Card
    PermissionRowCard(
      title = "Accessibility Overlay",
      description = "Required to anchor islands directly over the camera cutout and status bar. Trisle never reads passwords or screen contents.",
      icon = Icons.Default.Accessibility,
      isActive = isAccessibilityActive,
      actionLabel = if (isAccessibilityActive) "Active" else "Enable in Settings",
      onAction = {
        try {
          val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
          context.startActivity(intent)
        } catch (_: Exception) {}
      }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Notification Listener Card
    PermissionRowCard(
      title = "Notification Ingestion",
      description = "Allows Trisle's Priority Engine to detect active media playback, countdown timers, and incoming alert pings.",
      icon = Icons.Default.Notifications,
      isActive = isNotificationActive,
      actionLabel = if (isNotificationActive) "Active" else "Enable Access",
      onAction = {
        try {
          val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
          context.startActivity(intent)
        } catch (_: Exception) {}
      }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Battery Optimization Card
    PermissionRowCard(
      title = "Background Battery Priority",
      description = "Exempts Trisle from aggressive OEM task killing so the camera cutout remains responsive across app launches.",
      icon = Icons.Default.BatterySaver,
      isActive = true,
      actionLabel = "App Battery Info",
      onAction = {
        try {
          val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
          }
          context.startActivity(intent)
        } catch (_: Exception) {}
      }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // 4. Android 13-17 "Restricted Settings" Walkthrough
    Text(
      text = "SIDELOADED INSTALL HELPER (ANDROID 13+)",
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            tint = Color(0xFFFBBF24),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Are toggles greyed out or restricted?",
            color = TextHighContrast,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Starting with Android 13, apps installed directly via APK download have Accessibility toggles greyed out until manually unlocked in system App Info:",
          color = TextMediumContrast,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        RestrictedStepItem(step = "1", text = "Open device Settings → Apps → All Apps → Trisle")
        RestrictedStepItem(step = "2", text = "Tap the 3-dot overflow menu (⋮) in the top-right corner")
        RestrictedStepItem(step = "3", text = "Select \"Allow restricted settings\"")
        RestrictedStepItem(step = "4", text = "Confirm your device biometric/PIN and return here")

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
          onClick = {
            try {
              val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
              }
              context.startActivity(intent)
            } catch (_: Exception) {}
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PlatinumWhite)
        ) {
          Text("Open Trisle App Info Settings", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 5. Zero Telemetry & Privacy Disclosure
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorderSubtle))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalAlignment = Alignment.Top
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = TitaniumSilver,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Privacy & Data Architecture",
            color = TextHighContrast,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Trisle includes zero third-party telemetry SDKs, zero advertising trackers, and zero remote analytics. Free users trigger zero network requests. Pro users only contact api.polar.sh to validate their license key.",
            color = TextMediumContrast,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }
  }
}

@Composable
private fun PermissionRowCard(
  title: String,
  description: String,
  icon: ImageVector,
  isActive: Boolean,
  actionLabel: String,
  onAction: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
    shape = RoundedCornerShape(16.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorderSubtle))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(CarbonBackground)
              .border(0.8.dp, SteelBorder, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(icon, contentDescription = null, tint = PlatinumWhite, modifier = Modifier.size(16.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = title, color = TextHighContrast, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        // Status Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) Color(0xFF142918) else Color(0xFF261D12))
            .border(0.8.dp, if (isActive) Color(0xFF4ADE80) else Color(0xFFFBBF24), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = if (isActive) "ACTIVE" else "PENDING",
            color = if (isActive) Color(0xFF4ADE80) else Color(0xFFFBBF24),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(text = description, color = TextMediumContrast, fontSize = 11.sp, lineHeight = 16.sp)
      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = onAction,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isActive) GraphiteSurfaceVariant else CarbonBackground,
          contentColor = PlatinumWhite
        )
      ) {
        Text(actionLabel, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }
    }
  }
}

@Composable
private fun RestrictedStepItem(step: String, text: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(18.dp)
        .clip(CircleShape)
        .background(GraphiteSurfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Text(step, color = TitaniumSilver, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.width(10.dp))
    Text(text, color = TextMediumContrast, fontSize = 12.sp, lineHeight = 16.sp)
  }
}
