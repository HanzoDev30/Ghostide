package ir.hanzodev1375.components.image

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class ZoomableImageView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
) : SubsamplingScaleImageView(context, attrs) {

  private var rotationDegrees = 0
  private var zoomListener: OnZoomStateListener? = null
  private var lastZoomed: Boolean? = null

  fun interface OnZoomStateListener {
    fun onZoomStateChanged(zoomed: Boolean)
  }

  init {
    setOrientation(ORIENTATION_USE_EXIF)
    setMinimumScaleType(SCALE_TYPE_CENTER_INSIDE)
    setMaxScale(MAX_SCALE)
    setDoubleTapZoomStyle(ZOOM_FOCUS_FIXED)
    setDoubleTapZoomDuration(ZOOM_DURATION_MS)
    setQuickScaleEnabled(true)
    setPanEnabled(true)
    setZoomEnabled(true)
    setOnStateChangedListener(
      object : SubsamplingScaleImageView.DefaultOnStateChangedListener() {
        override fun onScaleChanged(newScale: Float, origin: Int) {
          notifyZoomState()
        }
      }
    )
  }

  fun setOnZoomStateListener(listener: OnZoomStateListener?) {
    zoomListener = listener
  }

  fun isZoomed(): Boolean = isReady && scale > minScale * 1.01f

  fun toggleZoom(animated: Boolean = true) {
    if (isZoomed()) zoomOut(animated) else zoomIn(animated)
  }

  fun zoomIn(animated: Boolean = true) {
    if (!isReady) return
    val target = if (minScale >= 1f) minScale * 2f else 1f
    animateTo(target.coerceAtMost(maxScale), animated)
  }

  fun zoomOut(animated: Boolean = true) {
    if (!isReady) return
    animateTo(minScale, animated)
  }

  fun rotateClockwise(): Int {
    rotationDegrees = (appliedOrientation + 90) % 360
    val previousScale = scale
    val wasZoomed = isZoomed()
    setOrientation(rotationDegrees)
    if (wasZoomed && previousScale > 0f && isReady) {
      setScaleAndCenter(previousScale.coerceIn(minScale, maxScale), imageCenter())
      notifyZoomState(true)
    } else {
      notifyZoomState(false)
    }
    return rotationDegrees
  }

  fun applyRotation(degrees: Int) {
    if (degrees == 0) return
    rotationDegrees = ((degrees % 360) + 360) % 360
    setOrientation(rotationDegrees)
  }

  fun currentRotation(): Int = rotationDegrees

  fun fitToScreen(animated: Boolean = true) {
    setMinimumScaleType(SCALE_TYPE_CENTER_INSIDE)
    if (isReady) animateTo(minScale, animated) else notifyZoomState()
  }

  fun fillScreen() {
    setMinimumScaleType(SCALE_TYPE_CENTER_CROP)
    notifyZoomState()
  }

  fun showActualSize(animated: Boolean = true) {
    if (!isReady) return
    animateTo(1f.coerceIn(minScale, maxScale), animated)
  }

  private fun animateTo(target: Float, animated: Boolean) {
    if (!isReady) return
    val center = getCenter() ?: imageCenter()
    if (!animated) {
      setScaleAndCenter(target, center)
      notifyZoomState()
      return
    }
    animateScaleAndCenter(target, center)
      ?.withDuration(ZOOM_DURATION_MS.toLong())
      ?.withEasing(EASE_IN_OUT_QUAD)
      ?.withOnAnimationEventListener(
        object : SubsamplingScaleImageView.DefaultOnAnimationEventListener() {
          override fun onComplete() {
            notifyZoomState()
          }

          override fun onInterruptedByUser() {
            notifyZoomState()
          }

          override fun onInterruptedByNewAnim() {
            notifyZoomState()
          }
        }
      )
      ?.start()
  }

  private fun imageCenter(): PointF {
    val swap = rotationDegrees == 90 || rotationDegrees == 270
    val width = if (swap) getSHeight() else getSWidth()
    val height = if (swap) getSWidth() else getSHeight()
    return PointF(width / 2f, height / 2f)
  }

  private fun notifyZoomState(forced: Boolean? = null) {
    val zoomed = forced ?: isZoomed()
    if (zoomed == lastZoomed) return
    lastZoomed = zoomed
    zoomListener?.onZoomStateChanged(zoomed)
  }

  companion object {
    private const val MAX_SCALE = 16f
    private const val ZOOM_DURATION_MS = 300
  }
}
