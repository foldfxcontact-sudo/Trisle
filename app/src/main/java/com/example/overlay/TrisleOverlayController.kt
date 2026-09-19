package com.example.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import com.example.MainActivity
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

    val view = TrisleOverlayView(context).apply {
      // Tap on the overlay brings Trisle to the foreground
      setOnClickListener {
        try {
          val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
          }
          context.startActivity(intent)
        } catch (_: Exception) {}
      }
    }
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

    val density = context.resources.displayMetrics.density

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
      y = (profile.offsetY * density).toInt()
      x = (profile.offsetX * density).toInt()

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
      }
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
      val density = context.resources.displayMetrics.density
      params.gravity = gravityAlignment
      params.y = (profile.offsetY * density).toInt()
      params.x = (profile.offsetX * density).toInt()

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
      }

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
