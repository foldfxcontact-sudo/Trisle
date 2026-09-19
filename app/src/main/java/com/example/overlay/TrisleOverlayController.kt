package com.example.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import com.example.engine.IslandLayoutState
import com.example.model.CutoutPosition
import com.example.model.CutoutProfile
import com.example.model.LicenseTier

class TrisleOverlayController(private val context: Context) {

  private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
  private var overlayView: TrisleOverlayView? = null
  private var isAttached = false

  fun attachOverlay(
    isAccessibility: Boolean,
    layoutState: IslandLayoutState,
    profile: CutoutProfile,
    tier: LicenseTier
  ) {
    if (isAttached && overlayView != null) {
      updateState(layoutState, profile, tier)
      return
    }

    val view = TrisleOverlayView(context)
    view.updateState(layoutState, profile, tier)

    val windowType = if (isAccessibility) {
      WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
      } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
      }
    }

    val gravityAlignment = when (profile.position) {
      CutoutPosition.CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
      CutoutPosition.LEFT -> Gravity.TOP or Gravity.START
      CutoutPosition.RIGHT -> Gravity.TOP or Gravity.END
    }

    val params = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      windowType,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
          WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = gravityAlignment
      y = profile.offsetY.toInt()
      x = profile.offsetX.toInt()
    }

    try {
      windowManager.addView(view, params)
      overlayView = view
      isAttached = true
    } catch (_: Exception) {
      isAttached = false
    }
  }

  fun updateState(
    layoutState: IslandLayoutState,
    profile: CutoutProfile,
    tier: LicenseTier
  ) {
    overlayView?.let { view ->
      view.updateState(layoutState, profile, tier)
      val params = view.layoutParams as? WindowManager.LayoutParams ?: return
      val gravityAlignment = when (profile.position) {
        CutoutPosition.CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
        CutoutPosition.LEFT -> Gravity.TOP or Gravity.START
        CutoutPosition.RIGHT -> Gravity.TOP or Gravity.END
      }
      params.gravity = gravityAlignment
      params.y = profile.offsetY.toInt()
      params.x = profile.offsetX.toInt()
      try {
        windowManager.updateViewLayout(view, params)
      } catch (_: Exception) {}
    }
  }

  fun detachOverlay() {
    overlayView?.let {
      try {
        windowManager.removeView(it)
      } catch (_: Exception) {}
      overlayView = null
      isAttached = false
    }
  }

  val isOverlayAttached: Boolean
    get() = isAttached
}
