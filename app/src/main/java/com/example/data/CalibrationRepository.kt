package com.example.data

import android.content.Context
import com.example.model.CutoutPosition
import com.example.model.CutoutProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalibrationRepository(context: Context) {

  private val prefs = context.getSharedPreferences("trisle_calibration_prefs", Context.MODE_PRIVATE)

  private val _profile = MutableStateFlow(loadProfile())
  val profile: StateFlow<CutoutProfile> = _profile.asStateFlow()

  private val _excludedApps = MutableStateFlow(loadExcludedApps())
  val excludedApps: StateFlow<Set<String>> = _excludedApps.asStateFlow()

  private fun loadProfile(): CutoutProfile {
    val posName = prefs.getString("cutout_pos", CutoutPosition.CENTER.name) ?: CutoutPosition.CENTER.name
    val pos = try { CutoutPosition.valueOf(posName) } catch (_: Exception) { CutoutPosition.CENTER }
    return CutoutProfile(
      position = pos,
      offsetX = prefs.getFloat("offset_x", 0f),
      offsetY = prefs.getFloat("offset_y", 8f),
      width = prefs.getFloat("pill_width", 112f),
      height = prefs.getFloat("pill_height", 34f),
      cornerRadius = prefs.getFloat("corner_radius", 17f),
      hideInLandscape = prefs.getBoolean("hide_landscape", true),
      hideOnLockScreen = prefs.getBoolean("hide_lock", false)
    )
  }

  fun updateProfile(newProfile: CutoutProfile) {
    prefs.edit()
      .putString("cutout_pos", newProfile.position.name)
      .putFloat("offset_x", newProfile.offsetX)
      .putFloat("offset_y", newProfile.offsetY)
      .putFloat("pill_width", newProfile.width)
      .putFloat("pill_height", newProfile.height)
      .putFloat("corner_radius", newProfile.cornerRadius)
      .putBoolean("hide_landscape", newProfile.hideInLandscape)
      .putBoolean("hide_lock", newProfile.hideOnLockScreen)
      .apply()
    _profile.value = newProfile
  }

  private fun loadExcludedApps(): Set<String> {
    return prefs.getStringSet("excluded_packages", setOf(
      "com.google.android.GoogleCamera",
      "com.android.camera",
      "com.sec.android.app.camera",
      "com.google.android.youtube",
      "com.netflix.mediaclient"
    )) ?: emptySet()
  }

  fun toggleAppExclusion(packageName: String) {
    val current = _excludedApps.value.toMutableSet()
    if (current.contains(packageName)) {
      current.remove(packageName)
    } else {
      current.add(packageName)
    }
    prefs.edit().putStringSet("excluded_packages", current).apply()
    _excludedApps.value = current
  }
}
