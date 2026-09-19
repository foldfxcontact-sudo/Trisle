package com.example.model

enum class ActivityTier(val rank: Int, val label: String) {
  TIER_1_URGENT(1, "Urgent (Calls & Nav)"),
  TIER_2_TIME_SENSITIVE(2, "Time-Sensitive (Timers)"),
  TIER_3_CONTINUOUS(3, "Continuous (Media)"),
  TIER_4_EPHEMERAL(4, "Alerts (Messages)")
}

enum class ActivityType {
  MEDIA,
  TIMER,
  CALL,
  MESSAGE,
  NAVIGATION
}

data class IslandActivity(
  val id: String,
  val type: ActivityType,
  val tier: ActivityTier,
  val title: String,
  val subtitle: String = "",
  val progress: Float = 0f,
  val isPlaying: Boolean = false,
  val timestamp: Long = System.currentTimeMillis(),
  val appPackage: String = "",
  val contentIntent: android.app.PendingIntent? = null,
  val notificationCount: Int = 1,
  val chronometerBase: Long = 0L,
  val isCountDown: Boolean = false,
  val playbackPositionMs: Long = 0L,
  val playbackDurationMs: Long = 0L
)

enum class CutoutPosition {
  CENTER,
  LEFT,
  RIGHT
}

data class CutoutProfile(
  val position: CutoutPosition = CutoutPosition.CENTER,
  val offsetX: Float = 0f,
  val offsetY: Float = 10f,
  val width: Float = 110f,
  val height: Float = 34f,
  val cornerRadius: Float = 17f,
  val hideInLandscape: Boolean = true,
  val hideOnLockScreen: Boolean = false
)

enum class LicenseTier {
  FREE,
  PRO_LIFETIME
}

data class LicenseState(
  val tier: LicenseTier = LicenseTier.FREE,
  val licenseKey: String = "",
  val activationId: String = "",
  val activatedAt: Long = 0L,
  val lastValidatedAt: Long = 0L,
  val isDeviceActive: Boolean = false
)
