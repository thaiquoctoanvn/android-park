package io.edenx.androidpark.util

import android.animation.*
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.pow

sealed class AnimationUtil {

   //abstract fun attachToActivityContext(activity: Activity)

   class AddToCartAnimation : AnimationUtil() {
      private val tag = "add-to-cart-tag"
      private var mTarget: View? = null
      private var mDest: View? = null
      private var originX = 0f
      private var originY = 0f
      private var destX = 0f
      private var destY = 0f
      private var mCircleDuration = DEFAULT_DURATION
      private var mMoveDuration = DEFAULT_DURATION
      private val mDisappearDuration = DEFAULT_DURATION_DISAPPEAR
      private var mContextReference: WeakReference<Activity?>? = null
      private var mBorderWidth = 4
      private var mBorderColor = Color.BLACK
      private var mBitmap: Bitmap? = null
      private var mImageView: ImageView? = null
      private var mAnimationListener: Animator.AnimatorListener? = null

      fun attachActivity(activity: Activity?): AddToCartAnimation {
         mContextReference = WeakReference(activity)
         return this
      }

      fun setTargetView(view: View?): AddToCartAnimation {
         mTarget = view
         setOriginRect(mTarget!!.width.toFloat(), mTarget!!.height.toFloat())
         return this
      }

      private fun setOriginRect(x: Float, y: Float): AddToCartAnimation {
         originX = x
         originY = y
         return this
      }

      private fun setDestRect(x: Float, y: Float): AddToCartAnimation {
         destX = x
         destY = y
         return this
      }

      fun setDestView(view: View?): AddToCartAnimation {
         mDest = view
         setDestRect(mDest!!.width.toFloat(), mDest!!.width.toFloat())
         return this
      }

      fun setBorderWidth(width: Int): AddToCartAnimation {
         mBorderWidth = width
         return this
      }

      fun setBorderColor(color: Int): AddToCartAnimation {
         mBorderColor = color
         return this
      }

      fun setCircleDuration(duration: Int): AddToCartAnimation {
         mCircleDuration = duration
         return this
      }

      fun setMoveDuration(duration: Int): AddToCartAnimation {
         mMoveDuration = duration
         return this
      }

      private fun prepare(): Boolean {
         if (mContextReference!!.get() != null) {
            val decoreView = mContextReference!!.get()!!.window.decorView as ViewGroup
            mBitmap = drawViewToBitmap(mTarget, mTarget!!.width, mTarget!!.height)
            if (mImageView == null) mImageView = ImageView(mContextReference!!.get())
            mImageView?.setImageBitmap(mBitmap)
            val src = IntArray(2)
            mTarget!!.getLocationOnScreen(src)
            val params = FrameLayout.LayoutParams(mTarget!!.width, mTarget!!.height)
            //params.setMargins(src[0], src[1], 0, 0)
            if (mImageView?.parent == null) decoreView.addView(mImageView, params)
         }
         return true
      }

      fun startAnimation() {
         if (prepare()) {
            mTarget!!.visibility = View.VISIBLE
            avatarRevealAnimator.start()
         }
      }

      private val avatarRevealAnimator: AnimatorSet
         get() {
            val endRadius = Math.max(destX, destY) / 2
            val startRadius = Math.max(originX, originY) / 2

            val scaleFactor = 0.25f
            val scaleAnimatorY: Animator = ObjectAnimator.ofFloat(
               mImageView,
               View.SCALE_Y,
               scaleFactor
            )
            val scaleAnimatorX: Animator = ObjectAnimator.ofFloat(
               mImageView,
               View.SCALE_X,
               scaleFactor
            )

            // Start at the central point on the left edge of target view
            val startX = (mTarget!!.x + mTarget!!.width / 2 * 0) - (mTarget!!.width * scaleFactor * 0)
            val startY = (mTarget!!.y + mTarget!!.height / 2) - (mTarget!!.height * scaleFactor * 0)
            val endX = (mDest!!.x + mDest!!.width / 2) - 2 * (mTarget!!.width * scaleFactor)
            val endY = (mDest!!.y + mDest!!.height / 2) - 2 * (mTarget!!.height * scaleFactor)

            Log.d(tag, "endRadius: $endRadius")
            Log.d(tag, "srcViewX: ${mTarget!!.x}, srcViewY: ${mTarget!!.y}")
            Log.d(tag, "destViewX: ${mDest!!.x}, destViewY: ${mDest!!.y}")
            Log.d(tag, "startX: $startX, startY: $startY")
            Log.d(tag, "endX: $endX, endY: $endY")

            val translatorX: Animator = ObjectAnimator.ofFloat(
               mImageView,
               View.X,
               startX,
               endX
            )
            translatorX.interpolator =
               TimeInterpolator { input ->
                  (-Math.pow((input - 1).toDouble(), 2.0) + 1f).toFloat()
               }
            val translatorY: Animator = ObjectAnimator.ofFloat(
               mImageView,
               View.Y,
               startY,
               endY
            )
            translatorY.interpolator = LinearInterpolator()

            val animatorCircleSet = AnimatorSet()
            animatorCircleSet.duration = mCircleDuration.toLong()
            animatorCircleSet.playTogether(scaleAnimatorX, scaleAnimatorY, translatorX, translatorY)
            animatorCircleSet.addListener(object : Animator.AnimatorListener {
               override fun onAnimationStart(animation: Animator) {
                  if (mAnimationListener != null) mAnimationListener!!.onAnimationStart(animation)
               }

               override fun onAnimationEnd(animation: Animator) {
                  val animatorDisappearSet = AnimatorSet()
                  val disappearAnimatorY: Animator =
                     ObjectAnimator.ofFloat(mImageView, View.SCALE_Y, scaleFactor, 0f)
                  val disappearAnimatorX: Animator =
                     ObjectAnimator.ofFloat(mImageView, View.SCALE_X, scaleFactor, 0f)
                  animatorDisappearSet.duration = mDisappearDuration.toLong()
                  animatorDisappearSet.playTogether(disappearAnimatorX, disappearAnimatorY)
                  val total = AnimatorSet()
                  total.playSequentially(animatorDisappearSet)
                  total.addListener(object : Animator.AnimatorListener {
                     override fun onAnimationStart(animation: Animator) {}
                     override fun onAnimationEnd(animation: Animator) {
                        if (mAnimationListener != null) mAnimationListener!!.onAnimationEnd(
                           animation
                        )
                        reset()
                     }

                     override fun onAnimationCancel(animation: Animator) {}
                     override fun onAnimationRepeat(animation: Animator) {}
                  })
                  total.start()
               }

               override fun onAnimationCancel(animation: Animator) {}
               override fun onAnimationRepeat(animation: Animator) {}
            })
            return animatorCircleSet
         }

      private fun drawViewToBitmap(view: View?, width: Int, height: Int): Bitmap {
         val drawable: Drawable = BitmapDrawable()
         val dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
         val c = Canvas(dest)
         drawable.bounds = Rect(0, 0, width, height)
         drawable.draw(c)
         view!!.draw(c)
         return dest
      }

      private fun reset() {
         mBitmap!!.recycle()
         mBitmap = null
         if (mImageView?.parent != null) (mImageView?.parent as ViewGroup).removeView(mImageView)
         mImageView = null
      }

      fun setAnimationListener(listener: Animator.AnimatorListener?): AddToCartAnimation {
         mAnimationListener = listener
         return this
      }

      companion object {
         private const val DEFAULT_DURATION = 1000
         private const val DEFAULT_DURATION_DISAPPEAR = 200
      }
   }
}