package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.IslandLayoutState
import com.example.model.ActivityType
import com.example.model.CutoutPosition
import com.example.model.CutoutProfile
import com.example.model.IslandActivity
import com.example.model.LicenseTier
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

@Composable
fun TrisleIslandContainer(
  layoutState: IslandLayoutState,
  profile: CutoutProfile,
  licenseTier: LicenseTier,
  onAnchorClick: () -> Unit = {},
  onTogglePlayback: () -> Unit = {},
  onSatelliteDismiss: (String) -> Unit = {}
) {
  var isExpanded by remember { mutableStateOf(false) }

  // Cutout alignment
  val horizontalAlignment = when (profile.position) {
    CutoutPosition.CENTER -> Alignment.CenterHorizontally
    CutoutPosition.LEFT -> Alignment.Start
    CutoutPosition.RIGHT -> Alignment.End
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    horizontalAlignment = horizontalAlignment
  ) {
    // Dynamic 3-Island Row: [Left Satellite] <-> [Center Anchor Notch Pill] <-> [Right Satellite]
    Box(
      modifier = Modifier
        .wrapContentSize()
        .offset { IntOffset(profile.offsetX.toInt(), profile.offsetY.toInt()) },
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {

        // 1. Left Satellite Bubble (Detached node)
        AnimatedVisibility(
          visible = layoutState.leftSatellite != null && licenseTier == LicenseTier.PRO_LIFETIME,
          enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(),
          exit = scaleOut(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)) + fadeOut()
        ) {
          layoutState.leftSatellite?.let { leftActivity ->
            SatelliteBubble(
              activity = leftActivity,
              onClick = { onAnchorClick() },
              onFlickDismiss = { onSatelliteDismiss(leftActivity.id) }
            )
          }
        }

        // 2. Center Anchor Pill (Hugging or expanding around camera cutout)
        AnchorPill(
          activity = layoutState.anchorActivity,
          profile = profile,
          isExpanded = isExpanded,
          onTap = { isExpanded = !isExpanded },
          onLongPress = { isExpanded = true }
        )

        // 3. Right Satellite Bubble (Detached node)
        AnimatedVisibility(
          visible = layoutState.rightSatellite != null && licenseTier == LicenseTier.PRO_LIFETIME,
          enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(),
          exit = scaleOut(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)) + fadeOut()
        ) {
          layoutState.rightSatellite?.let { rightActivity ->
            SatelliteBubble(
              activity = rightActivity,
              onClick = { onAnchorClick() },
              onFlickDismiss = { onSatelliteDismiss(rightActivity.id) }
            )
          }
        }
      }
    }

    // Pro Long-Press Expanded Dashboard Card
    AnimatedVisibility(
      visible = isExpanded,
      enter = fadeIn() + scaleIn(initialScale = 0.95f),
      exit = fadeOut() + scaleOut(targetScale = 0.95f)
    ) {
      Spacer(modifier = Modifier.height(12.dp))
      ExpandedDashboardCard(
        activity = layoutState.anchorActivity,
        licenseTier = licenseTier,
        onTogglePlayback = onTogglePlayback,
        onClose = { isExpanded = false }
      )
    }
  }
}

@Composable
private fun AnchorPill(
  activity: IslandActivity?,
  profile: CutoutProfile,
  isExpanded: Boolean,
  onTap: () -> Unit,
  onLongPress: () -> Unit
) {
  val animatedWidth by animateDpAsState(
    targetValue = if (activity != null) maxOf(profile.width.dp, 160.dp) else profile.width.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
    label = "pill_width"
  )

  val animatedHeight by animateDpAsState(
    targetValue = profile.height.dp,
    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
    label = "pill_height"
  )

  Box(
    modifier = Modifier
      .width(animatedWidth)
      .height(animatedHeight)
      .clip(RoundedCornerShape(profile.cornerRadius.dp))
      .background(ObsidianBlack)
      .border(1.dp, SteelBorder, RoundedCornerShape(profile.cornerRadius.dp))
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { onTap() },
          onLongPress = { onLongPress() }
        )
      }
      .padding(horizontal = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    if (activity == null) {
      // Idle Cutout Pill with simulated camera lens ring
      Box(
        modifier = Modifier
          .size(12.dp)
          .clip(CircleShape)
          .background(Color(0xFF0F1014))
          .border(0.8.dp, Color(0xFF2C2E35), CircleShape)
      )
    } else {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left Icon
        val icon = getActivityIcon(activity.type)
        Icon(
          imageVector = icon,
          contentDescription = activity.type.name,
          tint = TitaniumSilver,
          modifier = Modifier.size(15.dp)
        )

        // Center Title / Subtitle
        Text(
          text = activity.title,
          color = TextHighContrast,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = FontFamily.Monospace,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp)
        )

        // Right Mini Visualizer or Timer Status
        if (activity.type == ActivityType.MEDIA && activity.isPlaying) {
          MiniEqualizer()
        } else if (activity.type == ActivityType.TIMER) {
          Text(
            text = "REC",
            color = Color(0xFF4ADE80),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        } else {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(TitaniumSilver)
          )
        }
      }
    }
  }
}

@Composable
private fun SatelliteBubble(
  activity: IslandActivity,
  onClick: () -> Unit,
  onFlickDismiss: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(ObsidianBlack)
      .border(1.2.dp, SteelBorder, CircleShape)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    val icon = getActivityIcon(activity.type)
    Icon(
      imageVector = icon,
      contentDescription = activity.title,
      tint = TitaniumSilver,
      modifier = Modifier.size(16.dp)
    )
  }
}

@Composable
private fun MiniEqualizer() {
  Row(
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.size(width = 2.dp, height = 7.dp).background(PlatinumWhite, RoundedCornerShape(1.dp)))
    Box(modifier = Modifier.size(width = 2.dp, height = 12.dp).background(PlatinumWhite, RoundedCornerShape(1.dp)))
    Box(modifier = Modifier.size(width = 2.dp, height = 5.dp).background(PlatinumWhite, RoundedCornerShape(1.dp)))
  }
}

@Composable
private fun ExpandedDashboardCard(
  activity: IslandActivity?,
  licenseTier: LicenseTier,
  onTogglePlayback: () -> Unit = {},
  onClose: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(CarbonBackground)
      .border(1.dp, SteelBorder, RoundedCornerShape(20.dp))
      .padding(16.dp)
  ) {
    Column {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(GraphiteSurface)
              .border(0.8.dp, SteelBorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getActivityIcon(activity?.type ?: ActivityType.MEDIA),
              contentDescription = null,
              tint = PlatinumWhite,
              modifier = Modifier.size(14.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = activity?.title ?: "No Active Activity",
              color = TextHighContrast,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = activity?.subtitle ?: "Tap activities below to trigger",
              color = TextMediumContrast,
              fontSize = 11.sp
            )
          }
        }

        IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Content specific to activity
      if (activity?.type == ActivityType.MEDIA) {
        LinearProgressIndicator(
          progress = { activity.progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = PlatinumWhite,
          trackColor = SteelBorderSubtle
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.SkipPrevious, "Previous", tint = TitaniumSilver)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(GraphiteSurface)
              .border(1.dp, SteelBorder, CircleShape)
              .clickable { onTogglePlayback() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (activity?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = "Play/Pause",
              tint = PlatinumWhite,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.SkipNext, "Next", tint = TitaniumSilver)
          }
        }
      } else {
        // Quick Reply or Action
        Text(
          text = "Priority Tier: ${activity?.tier?.label ?: "Standard"}",
          fontSize = 11.sp,
          color = TextMuted,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

private fun getActivityIcon(type: ActivityType): ImageVector {
  return when (type) {
    ActivityType.MEDIA -> Icons.Default.MusicNote
    ActivityType.TIMER -> Icons.Default.Timer
    ActivityType.CALL -> Icons.Default.Call
    ActivityType.MESSAGE -> Icons.Default.ChatBubble
    ActivityType.NAVIGATION -> Icons.Default.GraphicEq
  }
}
