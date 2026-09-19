package com.example.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.SystemClock
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.example.MainActivity
import com.example.engine.IslandLayoutState
import com.example.model.ActivityType
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
    clipChildren = false
    clipToPadding = false
  }

  // Left Satellite View
  private val leftSatellite = SatelliteView(context, isLeft = true)

  // Anchor Pill View
  private val anchorPill = AnchorPillView(context)

  // Right Satellite View
  private val rightSatellite = SatelliteView(context, isLeft = false)

  private var wasLeftVisible = false
  private var wasRightVisible = false

  var onSatelliteDismissListener: ((IslandActivity) -> Unit)? = null

  init {
    clipChildren = false
    clipToPadding = false

    val spacePx = (8 * density).toInt()

    rootRow.addView(leftSatellite)
    rootRow.addView(
      anchorPill,
      LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        setMargins(spacePx, 0, spacePx, 0)
      }
    )
    rootRow.addView(rightSatellite)

    val frameParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
      gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
    }
    addView(rootRow, frameParams)

    leftSatellite.onDismiss = { act ->
      onSatelliteDismissListener?.invoke(act)
    }
    rightSatellite.onDismiss = { act ->
      onSatelliteDismissListener?.invoke(act)
    }

    // Default root background click opens MainActivity only if children don't consume touch
    setOnClickListener {
      launchTrisleHome()
    }
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
    val leftAct = if (isPro) currentLayoutState.leftSatellite else null
    val rightAct = if (isPro) currentLayoutState.rightSatellite else null

    // 1. Update Left Satellite with Elastic Spring Physics
    if (leftAct != null) {
      leftSatellite.setActivity(leftAct)
      if (!wasLeftVisible) {
        animateSatelliteAttach(leftSatellite, isLeft = true)
        wasLeftVisible = true
      }
    } else {
      if (wasLeftVisible) {
        animateSatelliteMerge(leftSatellite, isLeft = true)
        wasLeftVisible = false
      } else {
        leftSatellite.visibility = View.GONE
      }
    }

    // 2. Update Anchor Pill with Morphing Physics
    anchorPill.update(currentLayoutState.anchorActivity, currentProfile)

    // 3. Update Right Satellite with Elastic Spring Physics
    if (rightAct != null) {
      rightSatellite.setActivity(rightAct)
      if (!wasRightVisible) {
        animateSatelliteAttach(rightSatellite, isLeft = false)
        wasRightVisible = true
      }
    } else {
      if (wasRightVisible) {
        animateSatelliteMerge(rightSatellite, isLeft = false)
        wasRightVisible = false
      } else {
        rightSatellite.visibility = View.GONE
      }
    }

    requestLayout()
  }

  private fun animateSatelliteAttach(view: SatelliteView, isLeft: Boolean) {
    view.visibility = View.VISIBLE
    view.scaleX = 0.1f
    view.scaleY = 0.1f
    view.alpha = 0f
    // Start physically tucked toward center anchor pill
    val initialOffset = (if (isLeft) 24f else -24f) * density
    view.translationX = initialOffset

    // Spring scale X
    SpringAnimation(view, DynamicAnimation.SCALE_X, 1f).apply {
      spring = SpringForce(1f).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
      }
      start()
    }

    // Spring scale Y
    SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f).apply {
      spring = SpringForce(1f).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
      }
      start()
    }

    // Spring Translation X (detaching outward)
    SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 0f).apply {
      spring = SpringForce(0f).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
      }
      start()
    }

    // Spring Alpha
    SpringAnimation(view, DynamicAnimation.ALPHA, 1f).apply {
      spring = SpringForce(1f).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        stiffness = SpringForce.STIFFNESS_MEDIUM
      }
      start()
    }
  }

  private fun animateSatelliteMerge(view: SatelliteView, isLeft: Boolean) {
    val mergeOffset = (if (isLeft) 24f else -24f) * density

    SpringAnimation(view, DynamicAnimation.TRANSLATION_X, mergeOffset).apply {
      spring = SpringForce(mergeOffset).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        stiffness = SpringForce.STIFFNESS_MEDIUM
      }
      start()
    }

    SpringAnimation(view, DynamicAnimation.SCALE_X, 0f).apply {
      spring = SpringForce(0f).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        stiffness = SpringForce.STIFFNESS_MEDIUM
      }
      start()
    }

    SpringAnimation(view, DynamicAnimation.SCALE_Y, 0f).apply {
      spring = SpringForce(0f).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        stiffness = SpringForce.STIFFNESS_MEDIUM
      }
      start()
    }

    SpringAnimation(view, DynamicAnimation.ALPHA, 0f).apply {
      spring = SpringForce(0f).apply {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        stiffness = SpringForce.STIFFNESS_MEDIUM
      }
      addEndListener { _, _, _, _ ->
        if (view.scaleX <= 0.05f) {
          view.visibility = View.GONE
        }
      }
      start()
    }
  }

  private fun launchTrisleHome() {
    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  companion object {
    fun launchActivityIntent(context: Context, activity: IslandActivity?) {
      if (activity == null) return
      if (activity.contentIntent != null) {
        try {
          activity.contentIntent.send()
          return
        } catch (_: Exception) {}
      }
      if (activity.appPackage.isNotBlank()) {
        try {
          val launchIntent = context.packageManager.getLaunchIntentForPackage(activity.appPackage)
          if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return
          }
        } catch (_: Exception) {}
      }
      // Fallback: Open Trisle App
      try {
        val intent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
      } catch (_: Exception) {}
    }
  }

  // ==========================================
  // Anchor Pill Sub-View with Fluid Physics
  // ==========================================
  class AnchorPillView(context: Context) : FrameLayout(context) {
    private val density = context.resources.displayMetrics.density
    private var currentActivity: IslandActivity? = null
    private var currentWidthPx = 0
    private var targetWidthPx = 0
    private var widthAnimator: ValueAnimator? = null

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

    private val iconView = ImageView(context).apply {
      visibility = View.GONE
      scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private val titleText = TextView(context).apply {
      setTextColor(Color.parseColor("#F4F4F6"))
      textSize = 11f
      maxLines = 1
      isSingleLine = true
      visibility = View.GONE
    }

    private val subtitleText = TextView(context).apply {
      setTextColor(Color.parseColor("#9296A1"))
      textSize = 9.5f
      maxLines = 1
      isSingleLine = true
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

      val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        addView(titleText)
        addView(subtitleText)
      }

      val iconPx = (18 * density).toInt()
      val dotPx = (6 * density).toInt()

      val innerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(iconView, LinearLayout.LayoutParams(iconPx, iconPx).apply {
          setMargins(0, 0, (6 * density).toInt(), 0)
        })
        addView(textCol, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        addView(statusIndicator, LinearLayout.LayoutParams(dotPx, dotPx).apply {
          setMargins((6 * density).toInt(), 0, 0, 0)
        })
      }
      addView(innerRow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))

      // Direct tap interaction: launches specific app via contentIntent or appPackage
      setOnClickListener {
        launchActivityIntent(context, currentActivity)
      }
    }

    fun update(activity: IslandActivity?, profile: CutoutProfile) {
      currentActivity = activity
      val newTargetW = if (activity != null) {
        val calculated = maxOf(profile.width, 180f)
        (calculated * density).toInt()
      } else {
        (profile.width * density).toInt()
      }
      val targetH = (profile.height * density).toInt()

      if (targetWidthPx != newTargetW) {
        targetWidthPx = newTargetW
        animateWidth(targetWidthPx, targetH)
      } else {
        layoutParams = (layoutParams ?: LayoutParams(0, 0)).apply {
          width = targetWidthPx
          height = targetH
        }
      }

      if (activity != null) {
        // App icon
        if (activity.appPackage.isNotBlank()) {
          try {
            val iconDrawable = context.packageManager.getApplicationIcon(activity.appPackage)
            iconView.setImageDrawable(iconDrawable)
            iconView.visibility = View.VISIBLE
          } catch (_: Exception) {
            iconView.visibility = View.GONE
          }
        } else {
          iconView.visibility = View.GONE
        }

        titleText.text = activity.title
        titleText.visibility = View.VISIBLE

        // Subtitle or Chronometer format
        val sub = if (activity.type == ActivityType.TIMER && activity.chronometerBase > 0) {
          formatChronometer(activity.chronometerBase, activity.isCountDown)
        } else {
          activity.subtitle
        }

        if (sub.isNotBlank()) {
          subtitleText.text = sub
          subtitleText.visibility = View.VISIBLE
        } else {
          subtitleText.visibility = View.GONE
        }

        statusIndicator.visibility = View.VISIBLE
        statusIndicator.setBackgroundColor(
          when (activity.type) {
            ActivityType.CALL -> Color.parseColor("#4ADE80") // Green
            ActivityType.TIMER -> Color.parseColor("#F59E0B") // Amber
            ActivityType.MEDIA -> Color.parseColor("#38BDF8") // Cyan
            else -> Color.parseColor("#A855F7") // Purple
          }
        )
      } else {
        iconView.visibility = View.GONE
        titleText.visibility = View.GONE
        subtitleText.visibility = View.GONE
        statusIndicator.visibility = View.GONE
      }
      invalidate()
    }

    private fun formatChronometer(base: Long, isCountDown: Boolean): String {
      val now = System.currentTimeMillis()
      val diff = if (isCountDown) (base - now).coerceAtLeast(0) else (now - base).coerceAtLeast(0)
      val totalSec = diff / 1000
      val min = totalSec / 60
      val sec = totalSec % 60
      return "%02d:%02d".format(min, sec)
    }

    private fun animateWidth(toW: Int, toH: Int) {
      widthAnimator?.cancel()
      val fromW = if (width > 0) width else toW
      widthAnimator = ValueAnimator.ofInt(fromW, toW).apply {
        duration = 240
        interpolator = OvershootInterpolator(1.1f)
        addUpdateListener { anim ->
          val w = anim.animatedValue as Int
          layoutParams = (layoutParams ?: LayoutParams(0, 0)).apply {
            width = w
            height = toH
          }
          requestLayout()
        }
        start()
      }
    }

    override fun onDraw(canvas: Canvas) {
      super.onDraw(canvas)
      val r = height / 2f
      rectF.set(0f, 0f, width.toFloat(), height.toFloat())
      canvas.drawRoundRect(rectF, r, r, bgPaint)
      canvas.drawRoundRect(rectF, r, r, strokePaint)
    }
  }

  // ==========================================
  // Satellite Bubble Sub-View with Rich Graphics
  // ==========================================
  class SatelliteView(context: Context, val isLeft: Boolean) : FrameLayout(context) {
    private val density = context.resources.displayMetrics.density
    private var currentActivity: IslandActivity? = null
    private var cachedIcon: Drawable? = null
    private var lastPackage: String = ""

    var onDismiss: ((IslandActivity) -> Unit)? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#3A3D45")
      style = Paint.Style.STROKE
      strokeWidth = 1.2f * density
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#38BDF8")
      style = Paint.Style.STROKE
      strokeWidth = 2f * density
    }
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#EF4444")
      style = Paint.Style.FILL
    }
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.WHITE
      textSize = 8f * density
      textAlign = Paint.Align.CENTER
      isFakeBoldText = true
    }
    private val equalizerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.parseColor("#38BDF8")
      style = Paint.Style.FILL
    }

    private val rectF = RectF()
    private val clipPath = Path()

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
      override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        launchActivityIntent(context, currentActivity)
        return true
      }

      override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
      ): Boolean {
        if (e1 != null && (e1.y - e2.y > 20f || kotlin.math.abs(velocityY) > 400f)) {
          // Swipe up / away gesture: dismiss bubble
          dismissWithAnimation()
          return true
        }
        return false
      }
    })

    init {
      setWillNotDraw(false)
      val sizePx = (36 * density).toInt()
      layoutParams = LayoutParams(sizePx, sizePx)

      setOnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
        true
      }
    }

    fun setActivity(activity: IslandActivity?) {
      currentActivity = activity
      if (activity != null && activity.appPackage != lastPackage) {
        lastPackage = activity.appPackage
        cachedIcon = try {
          if (lastPackage.isNotBlank()) {
            context.packageManager.getApplicationIcon(lastPackage)
          } else null
        } catch (_: Exception) {
          null
        }
      }
      invalidate()
    }

    private fun dismissWithAnimation() {
      currentActivity?.let { act ->
        SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, -50f * density).apply {
          spring = SpringForce(-50f * density).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_HIGH
          }
          start()
        }
        SpringAnimation(this, DynamicAnimation.SCALE_X, 0f).apply {
          spring = SpringForce(0f).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_HIGH
          }
          start()
        }
        SpringAnimation(this, DynamicAnimation.SCALE_Y, 0f).apply {
          spring = SpringForce(0f).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_HIGH
          }
          start()
        }
        SpringAnimation(this, DynamicAnimation.ALPHA, 0f).apply {
          spring = SpringForce(0f).apply {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_HIGH
          }
          addEndListener { _, _, _, _ ->
            visibility = View.GONE
            translationY = 0f
            onDismiss?.invoke(act)
          }
          start()
        }
      }
    }

    override fun onDraw(canvas: Canvas) {
      super.onDraw(canvas)
      val cx = width / 2f
      val cy = height / 2f
      val r = width / 2f - 2f

      // 1. Draw Base Bubble Body
      canvas.drawCircle(cx, cy, r, bgPaint)
      canvas.drawCircle(cx, cy, r, strokePaint)

      val act = currentActivity

      // 2. Draw Progress Ring if applicable
      if (act != null) {
        val progress = when {
          act.progress > 0f -> act.progress
          act.type == ActivityType.TIMER && act.chronometerBase > 0 -> {
            val elapsedSec = (System.currentTimeMillis() - act.chronometerBase) / 1000
            ((elapsedSec % 60) / 60f).coerceIn(0f, 1f)
          }
          else -> 0f
        }

        if (progress > 0f) {
          rectF.set(cx - r + 1.5f, cy - r + 1.5f, cx + r - 1.5f, cy + r - 1.5f)
          progressPaint.color = when (act.type) {
            ActivityType.TIMER -> Color.parseColor("#F59E0B")
            ActivityType.CALL -> Color.parseColor("#4ADE80")
            else -> Color.parseColor("#38BDF8")
          }
          canvas.drawArc(rectF, -90f, progress * 360f, false, progressPaint)
        }
      }

      // 3. Draw App Icon or Activity Graphic in the Center
      val iconSize = (20 * density).toInt()
      val iconLeft = (cx - iconSize / 2f).toInt()
      val iconTop = (cy - iconSize / 2f).toInt()

      if (cachedIcon != null) {
        canvas.save()
        clipPath.reset()
        clipPath.addCircle(cx, cy, r - 3f, Path.Direction.CW)
        canvas.clipPath(clipPath)
        cachedIcon?.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
        cachedIcon?.draw(canvas)
        canvas.restore()
      } else {
        // Fallback: type-specific tactile glyph
        when (act?.type) {
          ActivityType.MEDIA -> {
            // Draw mini 3-bar animated equalizer
            val barW = 2.5f * density
            val h1 = 7f * density
            val h2 = 12f * density
            val h3 = 9f * density
            equalizerPaint.color = Color.parseColor("#38BDF8")
            canvas.drawRoundRect(cx - 5f * density, cy - h1 / 2, cx - 5f * density + barW, cy + h1 / 2, 2f, 2f, equalizerPaint)
            canvas.drawRoundRect(cx - 1f * density, cy - h2 / 2, cx - 1f * density + barW, cy + h2 / 2, 2f, 2f, equalizerPaint)
            canvas.drawRoundRect(cx + 3f * density, cy - h3 / 2, cx + 3f * density + barW, cy + h3 / 2, 2f, 2f, equalizerPaint)
          }
          ActivityType.TIMER -> {
            equalizerPaint.color = Color.parseColor("#F59E0B")
            canvas.drawCircle(cx, cy, 4f * density, equalizerPaint)
          }
          ActivityType.CALL -> {
            equalizerPaint.color = Color.parseColor("#4ADE80")
            canvas.drawCircle(cx, cy, 4.5f * density, equalizerPaint)
          }
          else -> {
            equalizerPaint.color = Color.parseColor("#E0E2EC")
            canvas.drawCircle(cx, cy, 3.5f * density, equalizerPaint)
          }
        }
      }

      // 4. Notification Badge if count > 1
      if (act != null && act.notificationCount > 1) {
        val badgeRadius = 6.5f * density
        val badgeCx = width - badgeRadius - 1f
        val badgeCy = badgeRadius + 1f
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgeBgPaint)
        val textY = badgeCy - ((badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f)
        canvas.drawText(
          if (act.notificationCount > 9) "9+" else act.notificationCount.toString(),
          badgeCx,
          textY,
          badgeTextPaint
        )
      }
    }
  }
}
