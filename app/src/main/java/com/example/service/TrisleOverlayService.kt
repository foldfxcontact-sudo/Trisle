package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
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

class TrisleOverlayService : Service() {

  companion object {
    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    fun startService(context: Context) {
      if (Settings.canDrawOverlays(context)) {
        val intent = Intent(context, TrisleOverlayService::class.java)
        context.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, TrisleOverlayService::class.java)
      context.stopService(intent)
    }
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private lateinit var overlayController: TrisleOverlayController
  private lateinit var calibrationRepo: CalibrationRepository
  private lateinit var licenseRepo: LicenseRepository
  private val priorityEngine = PriorityEngine()

  override fun onCreate() {
    super.onCreate()
    overlayController = TrisleOverlayController(this)
    calibrationRepo = CalibrationRepository(this)
    licenseRepo = LicenseRepository(this)
    _isOverlayActive.value = true

    // Combine notifications, calibration profile, and license tier to update overlay in real time
    serviceScope.launch {
      combine(
        TrisleNotificationListener.detectedActivities,
        calibrationRepo.profile,
        licenseRepo.licenseState
      ) { activities, profile, license ->
        val resolvedActivities = if (activities.isNotEmpty()) {
          activities
        } else {
          // Default preview activity when no active notification exists
          listOf(
            IslandActivity(
              id = "idle_media",
              type = ActivityType.MEDIA,
              tier = ActivityTier.TIER_3_CONTINUOUS,
              title = "Trisle Active",
              subtitle = "Listening for background sessions",
              isPlaying = true
            )
          )
        }
        val layout = priorityEngine.resolveLayout(resolvedActivities, license.tier)
        Triple(layout, profile, license.tier)
      }.collect { (layout, profile, tier) ->
        overlayController.attachOverlay(
          isAccessibility = false,
          layoutState = layout,
          profile = profile,
          tier = tier
        )
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
    overlayController.detachOverlay()
    _isOverlayActive.value = false
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
