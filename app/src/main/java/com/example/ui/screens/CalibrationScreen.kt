package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CutoutPosition
import com.example.model.CutoutProfile
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
fun CalibrationScreen(
  profile: CutoutProfile,
  onPositionChange: (CutoutPosition) -> Unit,
  onCalibrationChange: (offsetX: Float, offsetY: Float, width: Float, height: Float, cornerRadius: Float) -> Unit,
  onReset: () -> Unit
) {
  val scrollState = rememberScrollState()
  val density = LocalDensity.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Text(
      text = "HARDWARE CUTOUT CALIBRATION",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    // Visual Cutout Target Canvas with Phone Silhouette
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("calibration_canvas"),
      colors = CardDefaults.cardColors(containerColor = CarbonBackground),
      shape = RoundedCornerShape(20.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorder))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LIVE ALIGNMENT CANVAS",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = TextMuted
          )
          Text(
            text = "Drag pill to position",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = PlatinumWhite
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Phone Top Bezel Silhouette
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(Color(0xFF08090C))
            .border(
              width = 1.2.dp,
              color = SteelBorder,
              shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            ),
          contentAlignment = Alignment.TopCenter
        ) {
          // Status bar guideline
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(38.dp)
              .background(Color(0x15FFFFFF))
              .border(0.5.dp, Color(0x1AFFFFFF))
          )

          // Interactive Draggable Pill
          val horizAlignment = when (profile.position) {
            CutoutPosition.CENTER -> Alignment.TopCenter
            CutoutPosition.LEFT -> Alignment.TopStart
            CutoutPosition.RIGHT -> Alignment.TopEnd
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp),
            contentAlignment = horizAlignment
          ) {
            Box(
              modifier = Modifier
                .offset {
                  IntOffset(
                    x = with(density) { profile.offsetX.dp.roundToPx() },
                    y = with(density) { profile.offsetY.dp.roundToPx() }
                  )
                }
                .width(profile.width.dp)
                .height(profile.height.dp)
                .clip(RoundedCornerShape(profile.cornerRadius.dp))
                .background(ObsidianBlack)
                .border(1.5.dp, PlatinumWhite, RoundedCornerShape(profile.cornerRadius.dp))
                .pointerInput(profile) {
                  detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newX = (profile.offsetX + dragAmount.x / density.density).coerceIn(-150f, 150f)
                    val newY = (profile.offsetY + dragAmount.y / density.density).coerceIn(0f, 60f)
                    onCalibrationChange(newX, newY, profile.width, profile.height, profile.cornerRadius)
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                // Simulated Lens
                Box(
                  modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F1014))
                    .border(0.8.dp, Color(0xFF383A42), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${profile.width.toInt()}×${profile.height.toInt()}",
                  color = TitaniumSilver,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "X: ${profile.offsetX.toInt()}dp  •  Y: ${profile.offsetY.toInt()}dp  •  Radius: ${profile.cornerRadius.toInt()}dp",
          color = TextMediumContrast,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Position Selector
    Text(
      text = "CUTOUT ANCHOR POSITION",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      PositionButton(
        label = "Left",
        icon = Icons.Default.FormatAlignLeft,
        isSelected = profile.position == CutoutPosition.LEFT,
        modifier = Modifier.weight(1f),
        onClick = { onPositionChange(CutoutPosition.LEFT) }
      )
      PositionButton(
        label = "Center",
        icon = Icons.Default.CenterFocusStrong,
        isSelected = profile.position == CutoutPosition.CENTER,
        modifier = Modifier.weight(1f),
        onClick = { onPositionChange(CutoutPosition.CENTER) }
      )
      PositionButton(
        label = "Right",
        icon = Icons.Default.FormatAlignRight,
        isSelected = profile.position == CutoutPosition.RIGHT,
        modifier = Modifier.weight(1f),
        onClick = { onPositionChange(CutoutPosition.RIGHT) }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Precision Sliders with Fine-Tuning Increment Buttons
    Text(
      text = "DIMENSION CALIBRATION",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorderSubtle))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        // Vertical Offset (Y)
        PrecisionSliderRow(
          label = "Vertical Offset (Y)",
          value = profile.offsetY,
          min = 0f,
          max = 50f,
          unit = "dp",
          onValueChange = { onCalibrationChange(profile.offsetX, it, profile.width, profile.height, profile.cornerRadius) },
          onStep = { delta ->
            val newVal = (profile.offsetY + delta).coerceIn(0f, 50f)
            onCalibrationChange(profile.offsetX, newVal, profile.width, profile.height, profile.cornerRadius)
          }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Offset (X)
        PrecisionSliderRow(
          label = "Horizontal Offset (X)",
          value = profile.offsetX,
          min = -120f,
          max = 120f,
          unit = "dp",
          onValueChange = { onCalibrationChange(it, profile.offsetY, profile.width, profile.height, profile.cornerRadius) },
          onStep = { delta ->
            val newVal = (profile.offsetX + delta).coerceIn(-120f, 120f)
            onCalibrationChange(newVal, profile.offsetY, profile.width, profile.height, profile.cornerRadius)
          }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Width
        PrecisionSliderRow(
          label = "Cutout Width",
          value = profile.width,
          min = 60f,
          max = 220f,
          unit = "dp",
          onValueChange = { onCalibrationChange(profile.offsetX, profile.offsetY, it, profile.height, profile.cornerRadius) },
          onStep = { delta ->
            val newVal = (profile.width + delta).coerceIn(60f, 220f)
            onCalibrationChange(profile.offsetX, profile.offsetY, newVal, profile.height, profile.cornerRadius)
          }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Height
        PrecisionSliderRow(
          label = "Cutout Height",
          value = profile.height,
          min = 20f,
          max = 60f,
          unit = "dp",
          onValueChange = { onCalibrationChange(profile.offsetX, profile.offsetY, profile.width, it, profile.cornerRadius) },
          onStep = { delta ->
            val newVal = (profile.height + delta).coerceIn(20f, 60f)
            onCalibrationChange(profile.offsetX, profile.offsetY, profile.width, newVal, profile.cornerRadius)
          }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Corner Radius
        PrecisionSliderRow(
          label = "Corner Curvature",
          value = profile.cornerRadius,
          min = 4f,
          max = 30f,
          unit = "dp",
          onValueChange = { onCalibrationChange(profile.offsetX, profile.offsetY, profile.width, profile.height, it) },
          onStep = { delta ->
            val newVal = (profile.cornerRadius + delta).coerceIn(4f, 30f)
            onCalibrationChange(profile.offsetX, profile.offsetY, profile.width, profile.height, newVal)
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Hardware Presets
    Text(
      text = "DEVICE ARCHETYPE PRESETS",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedButton(
        onClick = {
          onPositionChange(CutoutPosition.CENTER)
          onCalibrationChange(0f, 10f, 110f, 34f, 17f)
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PlatinumWhite)
      ) {
        Text("Pixel Center", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }

      OutlinedButton(
        onClick = {
          onPositionChange(CutoutPosition.CENTER)
          onCalibrationChange(0f, 8f, 90f, 30f, 15f)
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PlatinumWhite)
      ) {
        Text("Samsung Infinity", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedButton(
        onClick = {
          onPositionChange(CutoutPosition.LEFT)
          onCalibrationChange(12f, 10f, 95f, 32f, 16f)
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PlatinumWhite)
      ) {
        Text("Corner Left", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }

      OutlinedButton(
        onClick = onReset,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMediumContrast)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Reset", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
      }
    }
  }
}

@Composable
private fun PrecisionSliderRow(
  label: String,
  value: Float,
  min: Float,
  max: Float,
  unit: String,
  onValueChange: (Float) -> Unit,
  onStep: (Float) -> Unit
) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(label, color = TextHighContrast, fontSize = 12.sp, fontWeight = FontWeight.Medium)
      Text("${value.toInt()}$unit", color = PlatinumWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { onStep(-1f) },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TitaniumSilver, modifier = Modifier.size(16.dp))
      }

      Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = min..max,
        modifier = Modifier.weight(1f),
        colors = SliderDefaults.colors(
          thumbColor = PlatinumWhite,
          activeTrackColor = PlatinumWhite,
          inactiveTrackColor = SteelBorderSubtle
        )
      )

      IconButton(
        onClick = { onStep(1f) },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = "Increase", tint = TitaniumSilver, modifier = Modifier.size(16.dp))
      }
    }
  }
}

@Composable
private fun PositionButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) GraphiteSurfaceVariant else CarbonBackground)
      .border(
        width = 1.dp,
        color = if (isSelected) PlatinumWhite else SteelBorderSubtle,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) PlatinumWhite else TitaniumSilver,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        color = if (isSelected) PlatinumWhite else TextMediumContrast,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
