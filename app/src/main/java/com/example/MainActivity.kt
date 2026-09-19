package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LicenseTier
import com.example.ui.MainViewModel
import com.example.ui.screens.CalibrationScreen
import com.example.ui.screens.ExclusionsScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.ProLicenseScreen
import com.example.ui.screens.SandboxScreen
import com.example.ui.theme.CarbonBackground
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PlatinumWhite
import com.example.ui.theme.SteelBorder
import com.example.ui.theme.SteelBorderSubtle
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TitaniumSilver
import com.example.ui.theme.TrisleTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      TrisleTheme {
        TrisleApp(viewModel = viewModel)
      }
    }
  }
}

enum class TrisleTab(val title: String, val icon: ImageVector) {
  SANDBOX("Sandbox", Icons.Default.Layers),
  CALIBRATION("Cutout", Icons.Default.CropLandscape),
  PERMISSIONS("Access", Icons.Default.Accessibility),
  EXCLUSIONS("Rules", Icons.Default.PowerSettingsNew),
  PRO("Pro $5.99", Icons.Default.Stars)
}

@Composable
fun TrisleApp(viewModel: MainViewModel) {
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  val layoutState by viewModel.layoutState.collectAsState()
  val cutoutProfile by viewModel.cutoutProfile.collectAsState()
  val licenseState by viewModel.licenseState.collectAsState()
  val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
  val isNotificationActive by viewModel.isNotificationActive.collectAsState()
  val isSystemOverlayActive by viewModel.isSystemOverlayActive.collectAsState()
  val excludedApps by viewModel.excludedApps.collectAsState()
  val isActivating by viewModel.isActivating.collectAsState()
  val statusMessage by viewModel.activationStatusMessage.collectAsState()
  val context = androidx.compose.ui.platform.LocalContext.current

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = ObsidianBlack
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Sleek Minimalist Header
      TrisleHeader(
        isPro = licenseState.tier == LicenseTier.PRO_LIFETIME,
        isAccessibilityActive = isAccessibilityActive,
        isNotificationActive = isNotificationActive
      )

      // 2. Minimalist Tab Bar
      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = ObsidianBlack,
        contentColor = PlatinumWhite,
        edgePadding = 16.dp,
        divider = {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(SteelBorderSubtle)
          )
        }
      ) {
        TrisleTab.values().forEachIndexed { index, tab ->
          val selected = selectedTabIndex == index
          Tab(
            selected = selected,
            onClick = { selectedTabIndex = index },
            modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = if (selected) PlatinumWhite else TextMuted
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = tab.title,
                  color = if (selected) PlatinumWhite else TextMuted,
                  fontSize = 11.sp,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          )
        }
      }

      // 3. Tab Body
      Box(modifier = Modifier.fillMaxSize()) {
        when (TrisleTab.values()[selectedTabIndex]) {
          TrisleTab.SANDBOX -> SandboxScreen(
            layoutState = layoutState,
            cutoutProfile = cutoutProfile,
            licenseState = licenseState,
            isSystemOverlayActive = isSystemOverlayActive,
            onToggleSystemOverlay = { viewModel.toggleSystemOverlay(context) },
            onToggleMusic = { viewModel.toggleSimulatedMusic() },
            onToggleTimer = { viewModel.toggleSimulatedTimer() },
            onToggleCall = { viewModel.toggleSimulatedCall() },
            onToggleMessage = { viewModel.toggleSimulatedMessage() },
            onTogglePlayback = { viewModel.toggleCurrentMediaPlayback() },
            onDismissSatellite = { viewModel.dismissSatellite(it) },
            onClearAll = { viewModel.clearAllActivities() },
            onResetDefault = { viewModel.resetDefaultActivities() },
            onNavigateToPro = { selectedTabIndex = TrisleTab.PRO.ordinal }
          )

          TrisleTab.CALIBRATION -> CalibrationScreen(
            profile = cutoutProfile,
            onPositionChange = { viewModel.setCutoutPosition(it) },
            onCalibrationChange = { ox, oy, w, h, r ->
              viewModel.updateCalibration(ox, oy, w, h, r)
            },
            onReset = {
              viewModel.updateCalibration(0f, 10f, 110f, 34f, 17f)
            }
          )

          TrisleTab.PERMISSIONS -> PermissionsScreen(
            isAccessibilityActive = isAccessibilityActive,
            isNotificationActive = isNotificationActive
          )

          TrisleTab.EXCLUSIONS -> ExclusionsScreen(
            profile = cutoutProfile,
            excludedApps = excludedApps,
            onToggleLandscape = { viewModel.toggleLandscapeHide(it) },
            onToggleLockScreen = { viewModel.toggleLockScreenHide(it) },
            onToggleAppExclusion = { viewModel.toggleAppExclusion(it) }
          )

          TrisleTab.PRO -> ProLicenseScreen(
            licenseState = licenseState,
            isActivating = isActivating,
            statusMessage = statusMessage,
            onActivateKey = { viewModel.activateLicenseKey(it) },
            onDeactivate = { viewModel.deactivateLicense() }
          )
        }
      }
    }
  }
}

@Composable
private fun TrisleHeader(
  isPro: Boolean,
  isAccessibilityActive: Boolean,
  isNotificationActive: Boolean
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(ObsidianBlack)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Small geometric 3-island brand badge
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(GraphiteSurface)
          .border(0.8.dp, SteelBorder, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(1.5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.size(2.dp).background(TitaniumSilver, CircleShape))
          Box(modifier = Modifier.size(width = 6.dp, height = 3.dp).background(PlatinumWhite, RoundedCornerShape(1.dp)))
          Box(modifier = Modifier.size(2.dp).background(TitaniumSilver, CircleShape))
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "TRISLE",
            color = TextHighContrast,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (isPro) Color(0xFF21252B) else CarbonBackground)
              .border(0.6.dp, if (isPro) PlatinumWhite else SteelBorder, RoundedCornerShape(4.dp))
              .padding(horizontal = 5.dp, vertical = 1.dp)
          ) {
            Text(
              text = if (isPro) "PRO" else "FREE",
              color = if (isPro) PlatinumWhite else TextMuted,
              fontSize = 8.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }
        Text(
          text = "Three islands. One cutout.",
          color = TextMuted,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // System Service Health Pill
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(GraphiteSurface)
        .border(0.8.dp, SteelBorderSubtle, RoundedCornerShape(20.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val isReady = isAccessibilityActive && isNotificationActive
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(if (isReady) Color(0xFF4ADE80) else Color(0xFFFBBF24))
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (isReady) "READY" else "CONFIG",
        color = TitaniumSilver,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
