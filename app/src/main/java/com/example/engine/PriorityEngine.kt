package com.example.engine

import com.example.model.IslandActivity
import com.example.model.LicenseTier

data class IslandLayoutState(
  val anchorActivity: IslandActivity? = null,
  val leftSatellite: IslandActivity? = null,
  val rightSatellite: IslandActivity? = null,
  val totalActiveCount: Int = 0
)

class PriorityEngine {

  fun resolveLayout(
    activities: List<IslandActivity>,
    licenseTier: LicenseTier
  ): IslandLayoutState {
    if (activities.isEmpty()) {
      return IslandLayoutState()
    }

    // Sort primarily by Tier rank (1 Urgent -> 2 Time-Sensitive -> 3 Continuous -> 4 Ephemeral)
    // Ties are broken by most recent state change (timestamp descending)
    val sorted = activities.sortedWith(
      compareBy<IslandActivity> { it.tier.rank }
        .thenByDescending { it.timestamp }
    )

    val anchor = sorted.firstOrNull()

    // Free Tier: Single anchor pill only.
    // Pro Tier (Lifetime): Detaches up to 2 concurrent satellite bubbles.
    return if (licenseTier == LicenseTier.PRO_LIFETIME) {
      val left = if (sorted.size > 1) sorted[1] else null
      val right = if (sorted.size > 2) sorted[2] else null
      IslandLayoutState(
        anchorActivity = anchor,
        leftSatellite = left,
        rightSatellite = right,
        totalActiveCount = sorted.size
      )
    } else {
      IslandLayoutState(
        anchorActivity = anchor,
        leftSatellite = null,
        rightSatellite = null,
        totalActiveCount = sorted.size
      )
    }
  }
}
