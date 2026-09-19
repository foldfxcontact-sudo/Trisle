package com.example.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CutoutPosition
import com.example.model.CutoutProfile
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
fun CalibrationScreen(
  profile: CutoutProfile,
  onPositionChange: (CutoutPosition) -> Unit,
  onCalibrationChange: (Float, Float, Float, Float, Float) -> Unit,
  onReset: () -> Unit
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
      text = "HARDWARE CUTOUT CALIBRATION",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    // Visual Cutout Target Canvas
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = CarbonBackground),
      shape = RoundedCornerShape(20.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorder))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "HARDWARE ALIGNMENT TARGET",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Pill Simulation according to current profile
        Box(
          modifier = Modifier
            .width(profile.width.dp)
            .height(profile.height.dp)
            .clip(RoundedCornerShape(profile.cornerRadius.dp))
            .background(ObsidianBlack)
            .border(1.2.dp, PlatinumWhite, RoundedCornerShape(profile.cornerRadius.dp)),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(GraphiteSurface)
                .border(1.dp, SteelBorder, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${profile.width.toInt()} × ${profile.height.toInt()} dp",
              color = TitaniumSilver,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Position: ${profile.position.name} • Y: ${profile.offsetY.toInt()}dp • Radius: ${profile.cornerRadius.toInt()}dp",
          color = TextMediumContrast,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Position Selector
    Text(
      text = "CAMERA HOLE-PUNCH POSITION",
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
      PositionTab(
        label = "Left Hole",
        selected = profile.position == CutoutPosition.LEFT,
        modifier = Modifier.weight(1f),
        onClick = { onPositionChange(CutoutPosition.LEFT) }
      )
      PositionTab(
        label = "Center Notch",
        selected = profile.position == CutoutPosition.CENTER,
        modifier = Modifier.weight(1f),
        onClick = { onPositionChange(CutoutPosition.CENTER) }
      )
      PositionTab(
        label = "Right Hole",
        selected = profile.position == CutoutPosition.RIGHT,
        modifier = Modifier.weight(1f),
        onClick = { onPositionChange(CutoutPosition.RIGHT) }
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Geometry Adjustments Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorderSubtle))
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Width
        CalibrationSlider(
          title = "Pill Width",
          value = profile.width,
          range = 80f..220f,
          unit = "dp",
          onValueChange = { newW ->
            onCalibrationChange(profile.offsetX, profile.offsetY, newW, profile.height, profile.cornerRadius)
          }
        )

        // Height
        CalibrationSlider(
          title = "Pill Height",
          value = profile.height,
          range = 24f..50f,
          unit = "dp",
          onValueChange = { newH ->
            onCalibrationChange(profile.offsetX, profile.offsetY, profile.width, newH, profile.cornerRadius)
          }
        )

        // Offset Y (Top status bar distance)
        CalibrationSlider(
          title = "Vertical Top Offset (Y)",
          value = profile.offsetY,
          range = 0f..50f,
          unit = "dp",
          onValueChange = { newY ->
            onCalibrationChange(profile.offsetX, newY, profile.width, profile.height, profile.cornerRadius)
          }
        )

        // Offset X
        CalibrationSlider(
          title = "Horizontal Offset (X)",
          value = profile.offsetX,
          range = -80f..80f,
          unit = "dp",
          onValueChange = { newX ->
            onCalibrationChange(newX, profile.offsetY, profile.width, profile.height, profile.cornerRadius)
          }
        )

        // Corner Radius
        CalibrationSlider(
          title = "Corner Radius",
          value = profile.cornerRadius,
          range = 8f..28f,
          unit = "dp",
          onValueChange = { newR ->
            onCalibrationChange(profile.offsetX, profile.offsetY, profile.width, profile.height, newR)
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Hardware Presets
    Text(
      text = "HARDWARE PROFILE PRESETS",
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
      PresetChip(
        label = "Pixel 7/8/9",
        modifier = Modifier.weight(1f),
        onClick = {
          onPositionChange(CutoutPosition.CENTER)
          onCalibrationChange(0f, 10f, 110f, 34f, 17f)
        }
      )
      PresetChip(
        label = "Samsung Infinity-O",
        modifier = Modifier.weight(1f),
        onClick = {
          onPositionChange(CutoutPosition.CENTER)
          onCalibrationChange(0f, 8f, 106f, 32f, 16f)
        }
      )
      PresetChip(
        label = "Corner Cutout",
        modifier = Modifier.weight(1f),
        onClick = {
          onPositionChange(CutoutPosition.LEFT)
          onCalibrationChange(12f, 8f, 112f, 34f, 17f)
        }
      )
    }
  }
}

@Composable
private fun PositionTab(
  label: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(if (selected) GraphiteSurface else CarbonBackground)
      .border(1.dp, if (selected) PlatinumWhite else SteelBorder, RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (selected) PlatinumWhite else TextMediumContrast,
      fontSize = 11.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun PresetChip(
  label: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBackground)
      .border(0.8.dp, SteelBorder, RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = TitaniumSilver,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun CalibrationSlider(
  title: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  unit: String,
  onValueChange: (Float) -> Unit
) {
  Column(modifier = Modifier.padding(vertical = 6.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(text = title, color = TextHighContrast, fontSize = 12.sp, fontWeight = FontWeight.Medium)
      Text(
        text = "${value.toInt()} $unit",
        color = TitaniumSilver,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace
      )
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = range,
      colors = SliderDefaults.colors(
        thumbColor = PlatinumWhite,
        activeTrackColor = TitaniumSilver,
        inactiveTrackColor = SteelBorderSubtle
      )
    )
  }
}
