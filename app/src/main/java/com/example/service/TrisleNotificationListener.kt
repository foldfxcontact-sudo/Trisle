package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.model.ActivityTier
import com.example.model.ActivityType
import com.example.model.IslandActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrisleNotificationListener : NotificationListenerService() {

  companion object {
    private val _isListenerConnected = MutableStateFlow(false)
    val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

    private val _detectedActivities = MutableStateFlow<List<IslandActivity>>(emptyList())
    val detectedActivities: StateFlow<List<IslandActivity>> = _detectedActivities.asStateFlow()

    fun isNotificationAccessGranted(context: Context): Boolean {
      val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
      ) ?: ""
      val myComponent = ComponentName(context, TrisleNotificationListener::class.java).flattenToString()
      return enabledListeners.contains(myComponent)
    }
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var tickerJob: Job? = null

  override fun onListenerConnected() {
    super.onListenerConnected()
    _isListenerConnected.value = true
    syncActiveNotifications()
    startPeriodicTicker()
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    _isListenerConnected.value = false
    tickerJob?.cancel()
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    syncActiveNotifications()
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    syncActiveNotifications()
  }

  private fun startPeriodicTicker() {
    tickerJob?.cancel()
    tickerJob = serviceScope.launch {
      while (isActive) {
        delay(1000)
        // Refresh media or timer state if active activities exist
        if (_detectedActivities.value.any { it.type == ActivityType.MEDIA || it.type == ActivityType.TIMER }) {
          syncActiveNotifications()
        }
      }
    }
  }

  private fun syncActiveNotifications() {
    try {
      val activeSbns = activeNotifications ?: return
      val parsedList = mutableListOf<IslandActivity>()

      for (sbn in activeSbns) {
        val n = sbn.notification ?: continue
        val extras = n.extras ?: continue
        var title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        var text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) continue

        val category = n.category
        val isChronometer = extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false)
        val isCountDown = extras.getBoolean("android.chronometerCountDown", false)
        val hasMediaSession = extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
        val isMedia = category == Notification.CATEGORY_TRANSPORT || (n.actions?.isNotEmpty() == true && hasMediaSession)
        val isCall = category == Notification.CATEGORY_CALL
        val isNav = category == Notification.CATEGORY_NAVIGATION
        val isTimer = isChronometer || category == Notification.CATEGORY_STOPWATCH ||
            category == Notification.CATEGORY_ALARM ||
            (title.contains("timer", ignoreCase = true) || text.contains("timer", ignoreCase = true))

        var isPlaying = false
        var playbackPos = 0L
        var playbackDur = 0L

        if (hasMediaSession) {
          try {
            val mediaToken = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
            } else {
              @Suppress("DEPRECATION")
              extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
            }

            if (mediaToken != null) {
              val controller = MediaController(this, mediaToken)
              val pbState = controller.playbackState
              isPlaying = pbState?.state == PlaybackState.STATE_PLAYING
              playbackPos = pbState?.position ?: 0L

              val meta = controller.metadata
              if (meta != null) {
                val t = meta.getString(MediaMetadata.METADATA_KEY_TITLE)
                val a = meta.getString(MediaMetadata.METADATA_KEY_ARTIST)
                  ?: meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                if (!t.isNullOrBlank()) title = t
                if (!a.isNullOrBlank()) text = a
                playbackDur = meta.getLong(MediaMetadata.METADATA_KEY_DURATION)
              }
            }
          } catch (_: Exception) {}
        }

        val progressVal = extras.getInt(Notification.EXTRA_PROGRESS, 0)
        val progressMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        val progress = if (progressMax > 0) {
          progressVal.toFloat() / progressMax
        } else if (playbackDur > 0 && playbackPos > 0) {
          (playbackPos.toFloat() / playbackDur).coerceIn(0f, 1f)
        } else {
          0f
        }

        val (type, tier) = when {
          isCall -> ActivityType.CALL to ActivityTier.TIER_1_URGENT
          isNav -> ActivityType.NAVIGATION to ActivityTier.TIER_1_URGENT
          isTimer -> ActivityType.TIMER to ActivityTier.TIER_2_TIME_SENSITIVE
          isMedia -> ActivityType.MEDIA to ActivityTier.TIER_3_CONTINUOUS
          else -> ActivityType.MESSAGE to ActivityTier.TIER_4_EPHEMERAL
        }

        val count = maxOf(1, if (n.number > 0) n.number else extras.getInt("android.notification.count", 1))
        val chronometerBase = if (n.`when` > 0L) n.`when` else sbn.postTime

        parsedList.add(
          IslandActivity(
            id = "${sbn.packageName}_${sbn.id}",
            type = type,
            tier = tier,
            title = title.ifBlank { "Notification" },
            subtitle = text,
            progress = progress,
            isPlaying = isPlaying,
            appPackage = sbn.packageName,
            timestamp = sbn.postTime,
            contentIntent = n.contentIntent,
            notificationCount = count,
            chronometerBase = chronometerBase,
            isCountDown = isCountDown,
            playbackPositionMs = playbackPos,
            playbackDurationMs = playbackDur
          )
        )
      }
      _detectedActivities.value = parsedList
    } catch (_: Exception) {
      // Safe fallback, no crashes on non-standard notification extras
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    tickerJob?.cancel()
  }
}
