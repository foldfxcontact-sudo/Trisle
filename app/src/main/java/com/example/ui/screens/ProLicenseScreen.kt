package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.licensing.LicenseRepository
import com.example.model.LicenseState
import com.example.model.LicenseTier
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
fun ProLicenseScreen(
  licenseState: LicenseState,
  isActivating: Boolean,
  statusMessage: String?,
  onActivateKey: (String) -> Unit,
  onDeactivate: () -> Unit
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val scrollState = rememberScrollState()
  var inputKey by remember { mutableStateOf(licenseState.licenseKey) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianBlack)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Text(
      text = "COMMERCIAL LICENSING (POLAR.SH)",
      color = TextMediumContrast,
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    // Pricing & Value Proposition Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = CarbonBackground),
      shape = RoundedCornerShape(20.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorder))
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column {
            Text(
              text = LicenseRepository.PRO_PRODUCT_NAME,
              color = TextHighContrast,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "One-time purchase • Direct DRM-free utility",
              color = TextMediumContrast,
              fontSize = 12.sp
            )
          }

          // Price Tag
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(GraphiteSurfaceVariant)
              .border(1.dp, SteelBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = LicenseRepository.PRO_PRICE_USD,
              color = PlatinumWhite,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Value Points
        FeatureBullet(text = "Three concurrent nodes: Anchor pill + 2 detached satellites")
        FeatureBullet(text = "Long-press floating dashboard cards with media scrubber")
        FeatureBullet(text = "No subscriptions, no ads, zero telemetry SDKs")
        FeatureBullet(text = "7-day refund guarantee open for all purchases")

        Spacer(modifier = Modifier.height(18.dp))

        // Buy button
        Button(
          onClick = {
            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LicenseRepository.CHECKOUT_URL))
              context.startActivity(intent)
            } catch (_: Exception) {}
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("buy_pro_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = PlatinumWhite,
            contentColor = ObsidianBlack
          )
        ) {
          Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Buy Pro (${LicenseRepository.PRO_PRICE_USD})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // License Activation Card
    Text(
      text = "LICENSE KEY ACTIVATION",
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
      Column(modifier = Modifier.padding(16.dp)) {
        if (licenseState.tier == LicenseTier.PRO_LIFETIME) {
          // Already Activated Pro State
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF4ADE80),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Trisle Pro is Active",
                color = TextHighContrast,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Key: ${licenseState.licenseKey.take(12)}••••",
                color = TitaniumSilver,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedButton(
            onClick = onDeactivate,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMediumContrast)
          ) {
            Text("Deactivate Device (Switch to Free)", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }
        } else {
          // Free Tier - Key input
          Text(
            text = "Enter the license key received after checkout:",
            color = TextMediumContrast,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 10.dp)
          )

          OutlinedTextField(
            value = inputKey,
            onValueChange = { inputKey = it.uppercase() },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("license_input_field"),
            placeholder = { Text("TRISLE-XXXX-XXXX", color = TextDisabled, fontFamily = FontFamily.Monospace) },
            singleLine = true,
            trailingIcon = {
              IconButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                  val text = clip.getItemAt(0).text?.toString() ?: ""
                  if (text.isNotBlank()) inputKey = text.trim().uppercase()
                }
              }) {
                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = TitaniumSilver)
              }
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PlatinumWhite,
              unfocusedBorderColor = SteelBorder,
              focusedTextColor = TextHighContrast,
              unfocusedTextColor = TextHighContrast,
              focusedContainerColor = CarbonBackground,
              unfocusedContainerColor = CarbonBackground
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
              focusManager.clearFocus()
              onActivateKey(inputKey)
            })
          )

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = {
              focusManager.clearFocus()
              onActivateKey(inputKey)
            },
            enabled = !isActivating && inputKey.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("activate_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = CarbonBackground,
              contentColor = PlatinumWhite
            )
          ) {
            if (isActivating) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PlatinumWhite, strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Activate License", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Developer & Reviewer Quick Test Helper Button
          OutlinedButton(
            onClick = {
              inputKey = "TRISLE-SANDBOX-PASS"
              onActivateKey("TRISLE-SANDBOX-PASS")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMediumContrast)
          ) {
            Text("Apply Sandbox Test Key (Demo Pro)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
          }
        }

        // Status or Error Message
        statusMessage?.let { msg ->
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = msg,
            color = if (msg.startsWith("Error")) Color(0xFFF87171) else Color(0xFF4ADE80),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Terms & Refund Policy Note
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = CarbonBackground),
      shape = RoundedCornerShape(16.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SteelBorderSubtle))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Security, contentDescription = null, tint = TitaniumSilver, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Commercial Terms & Policy", color = TextHighContrast, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "• Pricing: Fixed $5.99 USD one-time lifetime license. No hidden subscription tiers.\n• Refund: 7-day refund guarantee from date of checkout.\n• Network: Direct calls strictly to api.polar.sh customer portal. Zero user tracking.",
          color = TextMuted,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )
      }
    }
  }
}

@Composable
private fun FeatureBullet(text: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(text = "•", color = TitaniumSilver, fontSize = 12.sp)
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = text, color = TextMediumContrast, fontSize = 12.sp, lineHeight = 16.sp)
  }
}
