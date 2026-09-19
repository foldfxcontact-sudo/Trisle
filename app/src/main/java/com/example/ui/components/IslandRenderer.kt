package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalDensity
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
import com.example.ui.theme.GraphiteSurfaceVariant
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
  onSatelliteClick: (IslandActivity) -> Unit = {},
  onSatelliteDismiss: (String) -> Unit = {},
  onAdjustTimer: (Int) -> Unit = {},
  onDismissCall: () -> Unit = {}
) {
  var isExpanded by remember { mutableStateOf(false) }
  val density = LocalDensity.current

  // Cutout alignment
  val horizontalAlignment = when (profile.position) {
    CutoutPosition.CENTER -> Alignment.CenterHorizontally
    CutoutPosition.LEFT -> Alignment.Start
    CutoutPosition.RIGHT -> Alignment.End
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp),
    horizontalAlignment = horizontalAlignment
  ) {
    // Dynamic 3-Island Row: [Left Satellite] <-> [Center Anchor Notch Pill] <-> [Right Satellite]
    Box(
      modifier = Modifier
        .wrapContentSize()
        .offset {
          IntOffset(
            x = with(density) { profile.offsetX.dp.roundToPx() },
            y = with(density) { profile.offsetY.dp.roundToPx() }
          )
        },
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
              onClick = { onSatelliteClick(leftActivity) },
              onFlickDismiss = { onSatelliteDismiss(leftActivity.id) }
            )
          }
        }

        // 2. Center Anchor Pill (Hugging or expanding around camera cutout)
        AnchorPill(
          activity = layoutState.anchorActivity,
          profile = profile,
          isExpanded = isExpanded,
          onTap = {
            isExpanded = !isExpanded
            onAnchorClick()
          },
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
              onClick = { onSatelliteClick(rightActivity) },
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
      Spacer(modifier = Modifier.height(14.dp))
      ExpandedDashboardCard(
        activity = layoutState.anchorActivity,
        licenseTier = licenseTier,
        onTogglePlayback = onTogglePlayback,
        onAdjustTimer = onAdjustTimer,
        onDismissCall = onDismissCall,
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
    targetValue = if (activity != null) maxOf(profile.width.dp, 164.dp) else profile.width.dp,
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
      .border(1.2.dp, SteelBorder, RoundedCornerShape(profile.cornerRadius.dp))
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
          .size(13.dp)
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
          tint = if (activity.type == ActivityType.CALL) Color(0xFF4ADE80) else TitaniumSilver,
          modifier = Modifier.size(15.dp)
        )

        // Center Title
        Text(
          text = activity.title,
          color = TextHighContrast,
          fontSize = 11.5.sp,
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
            text = "ACTIVE",
            color = Color(0xFFFBBF24),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        } else if (activity.type == ActivityType.CALL) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(Color(0xFF4ADE80))
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
      tint = if (activity.type == ActivityType.CALL) Color(0xFF4ADE80) else TitaniumSilver,
      modifier = Modifier.size(16.dp)
    )
  }
}

@Composable
private fun MiniEqualizer() {
  val infiniteTransition = rememberInfiniteTransition(label = "eq")
  val h1 by infiniteTransition.animateFloat(
    initialValue = 4f,
    targetValue = 12f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "h1"
  )
  val h2 by infiniteTransition.animateFloat(
    initialValue = 10f,
    targetValue = 5f,
    animationSpec = infiniteRepeatable(
      animation = tween(350, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "h2"
  )
  val h3 by infiniteTransition.animateFloat(
    initialValue = 6f,
    targetValue = 14f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "h3"
  )

  Row(
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.size(width = 2.dp, height = h1.dp).background(PlatinumWhite, RoundedCornerShape(1.dp)))
    Box(modifier = Modifier.size(width = 2.dp, height = h2.dp).background(PlatinumWhite, RoundedCornerShape(1.dp)))
    Box(modifier = Modifier.size(width = 2.dp, height = h3.dp).background(PlatinumWhite, RoundedCornerShape(1.dp)))
  }
}

@Composable
private fun ExpandedDashboardCard(
  activity: IslandActivity?,
  licenseTier: LicenseTier,
  onTogglePlayback: () -> Unit = {},
  onAdjustTimer: (Int) -> Unit = {},
  onDismissCall: () -> Unit = {},
  onClose: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(CarbonBackground)
      .border(1.2.dp, SteelBorder, RoundedCornerShape(20.dp))
      .padding(16.dp)
  ) {
    Column {
      // Header row
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
              .background(GraphiteSurface)
              .border(0.8.dp, SteelBorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getActivityIcon(activity?.type ?: ActivityType.MEDIA),
              contentDescription = null,
              tint = if (activity?.type == ActivityType.CALL) Color(0xFF4ADE80) else PlatinumWhite,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = activity?.title ?: "No Active Activity",
              color = TextHighContrast,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = activity?.subtitle ?: "Tap activities below to simulate",
              color = TextMediumContrast,
              fontSize = 11.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
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

      // Activity-Specific Interactive Controls
      when (activity?.type) {
        ActivityType.MEDIA -> {
          // Seekbar
          LinearProgressIndicator(
            progress = { activity.progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(4.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = PlatinumWhite,
            trackColor = SteelBorderSubtle
          )
          Spacer(modifier = Modifier.height(14.dp))

          // Media Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(onClick = {}, modifier = Modifier.size(38.dp)) {
              Icon(Icons.Default.SkipPrevious, "Previous", tint = TitaniumSilver)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(GraphiteSurface)
                .border(1.2.dp, SteelBorder, CircleShape)
                .clickable { onTogglePlayback() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (activity.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = PlatinumWhite,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            IconButton(onClick = {}, modifier = Modifier.size(38.dp)) {
              Icon(Icons.Default.SkipNext, "Next", tint = TitaniumSilver)
            }
          }
        }

        ActivityType.TIMER -> {
          // Timer Quick Adjust Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TIMER RUNNING",
              color = Color(0xFFFBBF24),
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              OutlinedButton(
                onClick = { onAdjustTimer(-30) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TitaniumSilver)
              ) {
                Text("-30s", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
              }
              OutlinedButton(
                onClick = { onAdjustTimer(60) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PlatinumWhite)
              ) {
                Text("+1m", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
              }
            }
          }
        }

        ActivityType.CALL -> {
          // Call Actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { onDismissCall() },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1A1A), contentColor = Color(0xFFF87171))
            ) {
              Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Decline", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
              onClick = { onClose() },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142918), contentColor = Color(0xFF4ADE80))
            ) {
              Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Answer", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
          }
        }

        else -> {
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
}

private fun getActivityIcon(type: ActivityType): ImageVector {
  return when (type) {
    ActivityType.MEDIA -> Icons.Default.MusicNote
    ActivityType.TIMER -> Icons.Default.Timer
    ActivityType.CALL -> Icons.Default.Call
    ActivityType.NAVIGATION -> Icons.Default.Navigation
    ActivityType.MESSAGE -> Icons.Default.ChatBubble
  }
}
