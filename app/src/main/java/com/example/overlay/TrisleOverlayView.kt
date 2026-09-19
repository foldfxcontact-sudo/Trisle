package com.example.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.engine.IslandLayoutState
import com.example.model.ActivityType
import com.example.model.CutoutPosition
import com.example.model.CutoutProfile
import com.example.model.IslandActivity
import com.example.model.LicenseTier

class TrisleOverlayView(context: Context) : FrameLayout(context) {

  private val density = context.resources.displayMetrics.density

  private var currentProfile = CutoutProfile()
  private var currentLayoutState = IslandLayoutState()
  private var currentTier = LicenseTier.FREE

  private val rootRow = LinearLayout(context).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
  }

  // Left Satellite View
  private val leftSatellite = SatelliteView(context)

  // Anchor Pill View
  private val anchorPill = AnchorPillView(context)

  // Right Satellite View
  private val rightSatellite = SatelliteView(context)

  init {
    clipChildren = false
    clipToPadding = false

    val spacePx = (8 * density).toInt()

    rootRow.addView(leftSatellite)
    rootRow.addView(anchorPill, LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.WRAP_CONTENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
      setMargins(spacePx, 0, spacePx, 0)
    })
    rootRow.addView(rightSatellite)

    val frameParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
      gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
    }
    addView(rootRow, frameParams)

    updateViews()
  }

  fun updateState(
    layoutState: IslandLayoutState,
    profile: CutoutProfile,
    tier: LicenseTier
  ) {
    currentLayoutState = layoutState
    currentProfile = profile
    currentTier = tier
    updateViews()
  }

  private fun updateViews() {
    val isPro = currentTier == LicenseTier.PRO_LIFETIME

    // Left Satellite
    if (isPro && currentLayoutState.leftSatellite != null) {
      leftSatellite.visibility = View.VISIBLE
      leftSatellite.setActivity(currentLayoutState.leftSatellite)
    } else {
      leftSatellite.visibility = View.GONE
    }

    // Anchor Pill
    anchorPill.update(currentLayoutState.anchorActivity, currentProfile)

    // Right Satellite
    if (isPro && currentLayoutState.rightSatellite != null) {
      rightSatellite.visibility = View.VISIBLE
      rightSatellite.setActivity(currentLayoutState.rightSatellite)
    } else {
      rightSatellite.visibility = View.GONE
    }

    requestLayout()
    invalidate()
  }

  // Anchor Pill Sub-View
  class AnchorPillView(context: Context) : FrameLayout(context) {
    private val density = context.resources.displayMetrics.density
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#3A3D45")
      style = Paint.Style.STROKE
      strokeWidth = 1.2f * density
    }
    private val rectF = RectF()

    private val titleText = TextView(context).apply {
      setTextColor(Color.parseColor("#F4F4F6"))
      textSize = 11f
      maxLines = 1
      visibility = View.GONE
    }

    private val statusIndicator = View(context).apply {
      setBackgroundColor(Color.parseColor("#4ADE80"))
      visibility = View.GONE
    }

    init {
      setWillNotDraw(false)
      val padH = (10 * density).toInt()
      setPadding(padH, 0, padH, 0)

      val innerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(titleText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val dotPx = (6 * density).toInt()
        addView(statusIndicator, LinearLayout.LayoutParams(dotPx, dotPx).apply {
          setMargins((6 * density).toInt(), 0, 0, 0)
        })
      }
      addView(innerRow, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }

    fun update(activity: IslandActivity?, profile: CutoutProfile) {
      val w = if (activity != null) maxOf(profile.width, 160f) else profile.width
      val h = profile.height
      layoutParams = (layoutParams ?: LayoutParams(0, 0)).apply {
        width = (w * density).toInt()
        height = (h * density).toInt()
      }

      if (activity != null) {
        titleText.text = activity.title
        titleText.visibility = View.VISIBLE
        statusIndicator.visibility = View.VISIBLE
      } else {
        titleText.visibility = View.GONE
        statusIndicator.visibility = View.GONE
      }
      invalidate()
    }

    override fun onDraw(canvas: Canvas) {
      super.onDraw(canvas)
      val r = height / 2f
      rectF.set(0f, 0f, width.toFloat(), height.toFloat())
      canvas.drawRoundRect(rectF, r, r, bgPaint)
      canvas.drawRoundRect(rectF, r, r, strokePaint)
    }
  }

  // Satellite Bubble Sub-View
  class SatelliteView(context: Context) : FrameLayout(context) {
    private val density = context.resources.displayMetrics.density
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#3A3D45")
      style = Paint.Style.STROKE
      strokeWidth = 1.2f * density
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#E0E2EC")
      style = Paint.Style.FILL
    }

    init {
      setWillNotDraw(false)
      val sizePx = (34 * density).toInt()
      layoutParams = LayoutParams(sizePx, sizePx)
    }

    fun setActivity(activity: IslandActivity?) {
      invalidate()
    }

    override fun onDraw(canvas: Canvas) {
      super.onDraw(canvas)
      val cx = width / 2f
      val cy = height / 2f
      val r = width / 2f - 2f
      canvas.drawCircle(cx, cy, r, bgPaint)
      canvas.drawCircle(cx, cy, r, strokePaint)
      canvas.drawCircle(cx, cy, 3f * density, dotPaint)
    }
  }
}
