package com.example.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalibrationRepository
import com.example.engine.IslandLayoutState
import com.example.engine.PriorityEngine
import com.example.licensing.LicenseRepository
import com.example.model.ActivityTier
import com.example.model.ActivityType
import com.example.model.CutoutPosition
import com.example.model.CutoutProfile
import com.example.model.IslandActivity
import com.example.model.LicenseState
import com.example.model.LicenseTier
import com.example.service.TrisleAccessibilityService
import com.example.service.TrisleNotificationListener
import com.example.service.TrisleOverlayService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val licenseRepo = LicenseRepository(application)
  private val calibrationRepo = CalibrationRepository(application)
  private val priorityEngine = PriorityEngine()

  val licenseState: StateFlow<LicenseState> = licenseRepo.licenseState
  val cutoutProfile: StateFlow<CutoutProfile> = calibrationRepo.profile
  val excludedApps: StateFlow<Set<String>> = calibrationRepo.excludedApps

  val isAccessibilityActive: StateFlow<Boolean> = TrisleAccessibilityService.isServiceRunning
  val isNotificationActive: StateFlow<Boolean> = TrisleNotificationListener.isListenerConnected
  val isSystemOverlayActive: StateFlow<Boolean> = TrisleOverlayService.isOverlayActive

  // Simulated activity events for interactive sandbox
  private val _simulatedActivities = MutableStateFlow<List<IslandActivity>>(
    listOf(
      IslandActivity(
        id = "sim_media_1",
        type = ActivityType.MEDIA,
        tier = ActivityTier.TIER_3_CONTINUOUS,
        title = "Midnight City",
        subtitle = "M83 • Hurry Up, We're Dreaming",
        isPlaying = true,
        progress = 0.42f
      ),
      IslandActivity(
        id = "sim_timer_1",
        type = ActivityType.TIMER,
        tier = ActivityTier.TIER_2_TIME_SENSITIVE,
        title = "04:30",
        subtitle = "Espresso extraction timer",
        progress = 0.70f
      ),
      IslandActivity(
        id = "sim_msg_1",
        type = ActivityType.MESSAGE,
        tier = ActivityTier.TIER_4_EPHEMERAL,
        title = "Alex Chen",
        subtitle = "Meeting rescheduled to 3pm"
      )
    )
  )
  val simulatedActivities: StateFlow<List<IslandActivity>> = _simulatedActivities.asStateFlow()

  // Layout state combining current activities and license tier (Free vs Pro)
  val layoutState: StateFlow<IslandLayoutState> = combine(
    _simulatedActivities,
    licenseState
  ) { activities, license ->
    priorityEngine.resolveLayout(activities, license.tier)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IslandLayoutState())

  private val _activationStatusMessage = MutableStateFlow<String?>(null)
  val activationStatusMessage: StateFlow<String?> = _activationStatusMessage.asStateFlow()

  private val _isActivating = MutableStateFlow(false)
  val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

  init {
    // Dynamic progress tick for playing media and countdown timer
    viewModelScope.launch {
      while (isActive) {
        delay(1000)
        val current = _simulatedActivities.value
        val updated = current.map { activity ->
          if (activity.type == ActivityType.MEDIA && activity.isPlaying) {
            val nextProg = if (activity.progress >= 1f) 0f else activity.progress + 0.015f
            activity.copy(progress = nextProg)
          } else if (activity.type == ActivityType.TIMER) {
            val nextProg = if (activity.progress >= 1f) 0f else activity.progress + 0.025f
            activity.copy(progress = nextProg)
          } else {
            activity
          }
        }
        _simulatedActivities.value = updated
      }
    }
  }

  // System Overlay Service Control
  fun toggleSystemOverlay(context: Context) {
    if (isSystemOverlayActive.value) {
      TrisleOverlayService.stopService(context)
    } else {
      if (Settings.canDrawOverlays(context)) {
        TrisleOverlayService.startService(context)
      }
    }
  }

  // Satellite and media controls
  fun dismissSatellite(activityId: String) {
    val current = _simulatedActivities.value.toMutableList()
    current.removeAll { it.id == activityId }
    _simulatedActivities.value = current
  }

  fun promoteActivityToAnchor(activity: IslandActivity) {
    val current = _simulatedActivities.value.toMutableList()
    val index = current.indexOfFirst { it.id == activity.id }
    if (index > 0) {
      val item = current.removeAt(index)
      current.add(0, item.copy(timestamp = System.currentTimeMillis()))
      _simulatedActivities.value = current
    }
  }

  fun adjustTimer(deltaSeconds: Int) {
    val current = _simulatedActivities.value.toMutableList()
    val timerIndex = current.indexOfFirst { it.type == ActivityType.TIMER }
    if (timerIndex >= 0) {
      val item = current[timerIndex]
      val newProg = (item.progress - (deltaSeconds / 300f)).coerceIn(0.05f, 0.95f)
      current[timerIndex] = item.copy(progress = newProg)
      _simulatedActivities.value = current
    }
  }

  fun dismissCall() {
    val current = _simulatedActivities.value.toMutableList()
    current.removeAll { it.type == ActivityType.CALL }
    _simulatedActivities.value = current
  }

  fun toggleCurrentMediaPlayback() {
    val current = _simulatedActivities.value.toMutableList()
    val mediaIndex = current.indexOfFirst { it.type == ActivityType.MEDIA }
    if (mediaIndex >= 0) {
      val item = current[mediaIndex]
      current[mediaIndex] = item.copy(isPlaying = !item.isPlaying)
      _simulatedActivities.value = current
    }
  }

  // Sandbox controls
  fun toggleSimulatedMusic() {
    val current = _simulatedActivities.value.toMutableList()
    val existingIndex = current.indexOfFirst { it.type == ActivityType.MEDIA }
    if (existingIndex >= 0) {
      val item = current[existingIndex]
      current[existingIndex] = item.copy(isPlaying = !item.isPlaying, timestamp = System.currentTimeMillis())
    } else {
      current.add(
        IslandActivity(
          id = "sim_media_${System.currentTimeMillis()}",
          type = ActivityType.MEDIA,
          tier = ActivityTier.TIER_3_CONTINUOUS,
          title = "Solar Drift",
          subtitle = "Carbon Based Lifeforms",
          isPlaying = true,
          progress = 0.15f
        )
      )
    }
    _simulatedActivities.value = current
  }

  fun toggleSimulatedTimer() {
    val current = _simulatedActivities.value.toMutableList()
    val existingIndex = current.indexOfFirst { it.type == ActivityType.TIMER }
    if (existingIndex >= 0) {
      current.removeAt(existingIndex)
    } else {
      current.add(
        IslandActivity(
          id = "sim_timer_${System.currentTimeMillis()}",
          type = ActivityType.TIMER,
          tier = ActivityTier.TIER_2_TIME_SENSITIVE,
          title = "12:45",
          subtitle = "Focus session",
          progress = 0.65f
        )
      )
    }
    _simulatedActivities.value = current
  }

  fun toggleSimulatedCall() {
    val current = _simulatedActivities.value.toMutableList()
    val existingIndex = current.indexOfFirst { it.type == ActivityType.CALL }
    if (existingIndex >= 0) {
      current.removeAt(existingIndex)
    } else {
      current.add(
        0, // Call is Tier 1 (Urgent)
        IslandActivity(
          id = "sim_call_${System.currentTimeMillis()}",
          type = ActivityType.CALL,
          tier = ActivityTier.TIER_1_URGENT,
          title = "Incoming Call",
          subtitle = "Studio Hotline",
          timestamp = System.currentTimeMillis()
        )
      )
    }
    _simulatedActivities.value = current
  }

  fun toggleSimulatedMessage() {
    val current = _simulatedActivities.value.toMutableList()
    val existingIndex = current.indexOfFirst { it.type == ActivityType.MESSAGE }
    if (existingIndex >= 0) {
      current.removeAt(existingIndex)
    } else {
      current.add(
        IslandActivity(
          id = "sim_msg_${System.currentTimeMillis()}",
          type = ActivityType.MESSAGE,
          tier = ActivityTier.TIER_4_EPHEMERAL,
          title = "Elena Rostova",
          subtitle = "Files sent to repository."
        )
      )
    }
    _simulatedActivities.value = current
  }

  fun clearAllActivities() {
    _simulatedActivities.value = emptyList()
  }

  fun resetDefaultActivities() {
    _simulatedActivities.value = listOf(
      IslandActivity(
        id = "sim_media_1",
        type = ActivityType.MEDIA,
        tier = ActivityTier.TIER_3_CONTINUOUS,
        title = "Midnight City",
        subtitle = "M83 • Hurry Up, We're Dreaming",
        isPlaying = true,
        progress = 0.42f
      ),
      IslandActivity(
        id = "sim_timer_1",
        type = ActivityType.TIMER,
        tier = ActivityTier.TIER_2_TIME_SENSITIVE,
        title = "04:30",
        subtitle = "Espresso extraction timer",
        progress = 0.70f
      ),
      IslandActivity(
        id = "sim_msg_1",
        type = ActivityType.MESSAGE,
        tier = ActivityTier.TIER_4_EPHEMERAL,
        title = "Alex Chen",
        subtitle = "Meeting rescheduled to 3pm"
      )
    )
  }

  // Calibration Controls
  fun setCutoutPosition(pos: CutoutPosition) {
    calibrationRepo.updateProfile(cutoutProfile.value.copy(position = pos))
  }

  fun updateCalibration(offsetX: Float, offsetY: Float, width: Float, height: Float, cornerRadius: Float) {
    calibrationRepo.updateProfile(
      cutoutProfile.value.copy(
        offsetX = offsetX,
        offsetY = offsetY,
        width = width,
        height = height,
        cornerRadius = cornerRadius
      )
    )
  }

  fun toggleLandscapeHide(hide: Boolean) {
    calibrationRepo.updateProfile(cutoutProfile.value.copy(hideInLandscape = hide))
  }

  fun toggleLockScreenHide(hide: Boolean) {
    calibrationRepo.updateProfile(cutoutProfile.value.copy(hideOnLockScreen = hide))
  }

  fun toggleAppExclusion(pkg: String) {
    calibrationRepo.toggleAppExclusion(pkg)
  }

  // License Controls (Polar integration)
  fun activateLicenseKey(key: String) {
    viewModelScope.launch {
      _isActivating.value = true
      _activationStatusMessage.value = null
      val result = licenseRepo.activateLicense(key)
      _isActivating.value = false
      result.fold(
        onSuccess = { msg -> _activationStatusMessage.value = msg },
        onFailure = { err -> _activationStatusMessage.value = "Error: ${err.message}" }
      )
    }
  }

  fun deactivateLicense() {
    viewModelScope.launch {
      licenseRepo.deactivateLicense()
      _activationStatusMessage.value = "Device deactivated. Switched to Free tier."
    }
  }

  fun clearStatusMessage() {
    _activationStatusMessage.value = null
  }
}
