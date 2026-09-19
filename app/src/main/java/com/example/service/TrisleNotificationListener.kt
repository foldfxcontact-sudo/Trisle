package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.model.ActivityTier
import com.example.model.ActivityType
import com.example.model.IslandActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

  override fun onListenerConnected() {
    super.onListenerConnected()
    _isListenerConnected.value = true
    syncActiveNotifications()
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    _isListenerConnected.value = false
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    syncActiveNotifications()
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    syncActiveNotifications()
  }

  private fun syncActiveNotifications() {
    try {
      val activeSbns = activeNotifications ?: return
      val parsedList = mutableListOf<IslandActivity>()

      for (sbn in activeSbns) {
        val n = sbn.notification ?: continue
        val extras = n.extras ?: continue
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) continue

        // Categorize into Trisle priority tiers:
        // Tier 1: Call or Navigation
        // Tier 2: Timer or Stopwatch
        // Tier 3: Media
        // Tier 4: Message or generic alert
        val category = n.category
        val isMedia = category == Notification.CATEGORY_TRANSPORT ||
            n.actions?.isNotEmpty() == true && extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
        val isCall = category == Notification.CATEGORY_CALL
        val isNav = category == Notification.CATEGORY_NAVIGATION
        val isTimer = category == Notification.CATEGORY_STOPWATCH ||
            category == Notification.CATEGORY_ALARM ||
            (title.contains("timer", ignoreCase = true) || text.contains("timer", ignoreCase = true))

        val (type, tier) = when {
          isCall -> ActivityType.CALL to ActivityTier.TIER_1_URGENT
          isNav -> ActivityType.NAVIGATION to ActivityTier.TIER_1_URGENT
          isTimer -> ActivityType.TIMER to ActivityTier.TIER_2_TIME_SENSITIVE
          isMedia -> ActivityType.MEDIA to ActivityTier.TIER_3_CONTINUOUS
          else -> ActivityType.MESSAGE to ActivityTier.TIER_4_EPHEMERAL
        }

        parsedList.add(
          IslandActivity(
            id = "${sbn.packageName}_${sbn.id}",
            type = type,
            tier = tier,
            title = title.ifBlank { "Notification" },
            subtitle = text,
            appPackage = sbn.packageName,
            timestamp = sbn.postTime
          )
        )
      }
      _detectedActivities.value = parsedList
    } catch (_: Exception) {
      // Safe fallback, no crashes on non-standard notification extras
    }
  }
}
