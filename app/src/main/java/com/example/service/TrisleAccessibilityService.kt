package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.example.data.CalibrationRepository
import com.example.engine.PriorityEngine
import com.example.licensing.LicenseRepository
import com.example.model.ActivityTier
import com.example.model.ActivityType
import com.example.model.IslandActivity
import com.example.overlay.TrisleOverlayController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TrisleAccessibilityService : AccessibilityService() {

  companion object {
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _foregroundPackage = MutableStateFlow("")
    val foregroundPackage: StateFlow<String> = _foregroundPackage.asStateFlow()

    var instance: TrisleAccessibilityService? = null
      private set
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var overlayController: TrisleOverlayController? = null
  private lateinit var calibrationRepo: CalibrationRepository
  private lateinit var licenseRepo: LicenseRepository
  private val priorityEngine = PriorityEngine()

  override fun onServiceConnected() {
    super.onServiceConnected()
    instance = this
    _isServiceRunning.value = true

    overlayController = TrisleOverlayController(this)
    calibrationRepo = CalibrationRepository(this)
    licenseRepo = LicenseRepository(this)

    // Collect activities & profiles to keep accessibility overlay updated
    serviceScope.launch {
      combine(
        TrisleNotificationListener.detectedActivities,
        calibrationRepo.profile,
        licenseRepo.licenseState
      ) { activities, profile, license ->
        val resolvedActivities = if (activities.isNotEmpty()) {
          activities
        } else {
          listOf(
            IslandActivity(
              id = "acc_idle",
              type = ActivityType.MEDIA,
              tier = ActivityTier.TIER_3_CONTINUOUS,
              title = "Trisle Cutout Active",
              subtitle = "Accessibility Overlay",
              isPlaying = true
            )
          )
        }
        val layout = priorityEngine.resolveLayout(resolvedActivities, license.tier)
        Triple(layout, profile, license.tier)
      }.collect { (layout, profile, tier) ->
        overlayController?.attachOverlay(
          isAccessibility = true,
          layoutState = layout,
          profile = profile,
          tier = tier
        )
      }
    }
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      val pkg = event.packageName?.toString() ?: ""
      if (pkg.isNotBlank() && pkg != packageName) {
        _foregroundPackage.value = pkg
      }
    }
  }

  override fun onInterrupt() {
    // Service interrupted
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
    overlayController?.detachOverlay()
    instance = null
    _isServiceRunning.value = false
  }
}
