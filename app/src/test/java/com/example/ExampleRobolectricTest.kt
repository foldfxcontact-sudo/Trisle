package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.PriorityEngine
import com.example.licensing.LicenseRepository
import com.example.model.ActivityTier
import com.example.model.ActivityType
import com.example.model.IslandActivity
import com.example.model.LicenseTier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Trisle", appName)
  }

  @Test
  fun `free tier only resolves single anchor pill`() {
    val engine = PriorityEngine()
    val activities = listOf(
      IslandActivity("1", ActivityType.CALL, ActivityTier.TIER_1_URGENT, "Call"),
      IslandActivity("2", ActivityType.TIMER, ActivityTier.TIER_2_TIME_SENSITIVE, "Timer"),
      IslandActivity("3", ActivityType.MEDIA, ActivityTier.TIER_3_CONTINUOUS, "Music")
    )

    val layout = engine.resolveLayout(activities, LicenseTier.FREE)
    assertNotNull(layout.anchorActivity)
    assertEquals("Call", layout.anchorActivity?.title)
    assertNull(layout.leftSatellite)
    assertNull(layout.rightSatellite)
    assertEquals(3, layout.totalActiveCount)
  }

  @Test
  fun `pro tier resolves anchor and two detached satellites`() {
    val engine = PriorityEngine()
    val activities = listOf(
      IslandActivity("1", ActivityType.CALL, ActivityTier.TIER_1_URGENT, "Call"),
      IslandActivity("2", ActivityType.TIMER, ActivityTier.TIER_2_TIME_SENSITIVE, "Timer"),
      IslandActivity("3", ActivityType.MEDIA, ActivityTier.TIER_3_CONTINUOUS, "Music")
    )

    val layout = engine.resolveLayout(activities, LicenseTier.PRO_LIFETIME)
    assertNotNull(layout.anchorActivity)
    assertEquals("Call", layout.anchorActivity?.title)
    assertNotNull(layout.leftSatellite)
    assertEquals("Timer", layout.leftSatellite?.title)
    assertNotNull(layout.rightSatellite)
    assertEquals("Music", layout.rightSatellite?.title)
    assertEquals(3, layout.totalActiveCount)
  }

  @Test
  fun `sandbox license key activates pro lifetime`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = LicenseRepository(context)

    val result = repo.activateLicense("TRISLE-SANDBOX-PASS")
    assertTrue(result.isSuccess)
    assertEquals(LicenseTier.PRO_LIFETIME, repo.licenseState.value.tier)

    repo.deactivateLicense()
    assertEquals(LicenseTier.FREE, repo.licenseState.value.tier)
  }

  @Test
  fun `excluded apps toggling works correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.CalibrationRepository(context)

    val pkg = "com.test.camera"
    val initiallyExcluded = repo.excludedApps.value.contains(pkg)
    repo.toggleAppExclusion(pkg)
    assertEquals(!initiallyExcluded, repo.excludedApps.value.contains(pkg))

    repo.toggleAppExclusion(pkg)
    assertEquals(initiallyExcluded, repo.excludedApps.value.contains(pkg))
  }

  @Test
  fun `island activity supports chronometer and notification badge`() {
    val activity = IslandActivity(
      id = "test_timer",
      type = ActivityType.TIMER,
      tier = ActivityTier.TIER_2_TIME_SENSITIVE,
      title = "Timer",
      chronometerBase = 1000L,
      isCountDown = true,
      notificationCount = 3
    )

    assertEquals(3, activity.notificationCount)
    assertTrue(activity.isCountDown)
    assertEquals(1000L, activity.chronometerBase)
  }
}
