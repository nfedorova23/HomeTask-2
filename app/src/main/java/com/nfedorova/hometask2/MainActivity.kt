package com.nfedorova.hometask2

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.nfedorova.hometask2.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var drumSize = 50
    private var animatorSet: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            drumSizeSeekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    drumView.drumSize = progress
                    drumSize = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            drumView.setOnClickListener {
                animateDrum()
            }

            resetButton.setOnClickListener {
                drumGroup.isVisible = true
                resultImageView.isVisible = false
                drawTextView.isVisible = false
                resetButton.isVisible = false
                drumView.drumSize = drumSize
                drumView.rotation = 0f
            }
        }
    }

    private fun animateDrum() {
        animatorSet?.removeAllListeners()
        animatorSet?.cancel()

        val rotationValue = Random.nextLong(0, 360)
        val rotation = ObjectAnimator.ofFloat(
            binding.drumView,
            "rotation",
            3600f + rotationValue.toFloat()
        ).setDuration(1000 + rotationValue)
        rotation.repeatMode = ValueAnimator.RESTART

        val sizeIncrease = ObjectAnimator.ofInt(
            binding.drumView,
            "drumSize",
            100
        ).setDuration(500)

        animatorSet = AnimatorSet().apply {
            play(rotation)
            play(sizeIncrease).after(rotation)
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        animatorSet?.addListener(onEnd = {
            showResult(rotationValue)
        })
    }

    private fun showResult(rotationValue: Long) {
        val angel = 360f / 7f
        when (rotationValue.toFloat()) {
            in 0f..angel -> showDrawTextView(getColor(R.color.purple), PURPLE_STRING)
            in angel * 2..angel * 3 -> showDrawTextView(getColor(R.color.cyan), CYAN_STRING)
            in angel * 4..angel * 5 -> showDrawTextView(getColor(R.color.yellow), YELLOW_STRING)
            in angel * 6..angel * 7 -> showDrawTextView(getColor(R.color.red), RED_STRING)
            else -> showImage()
        }
    }

    private fun showImage() = with(binding) {
        drumGroup.isVisible = false
        resultImageView.isVisible = true
        drawTextView.isVisible = false

        Glide.with(this@MainActivity)
            .asBitmap()
            .load("https://loremflickr.com/640/360")
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                    resultImageView.setImageDrawable(
                        AppCompatResources.getDrawable(
                            this@MainActivity,
                            android.R.drawable.stat_sys_download_done
                        )
                    )
                    resultImageView.imageTintList = ColorStateList.valueOf(getColor(R.color.black))
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    resultImageView.setImageDrawable(
                        AppCompatResources.getDrawable(
                            this@MainActivity,
                            android.R.drawable.stat_notify_error
                        )
                    )
                    resultImageView.imageTintList = ColorStateList.valueOf(getColor(R.color.red))
                    resetButton.isVisible = true
                }

                override fun onResourceReady(
                    resource: Bitmap, transition: Transition<in Bitmap>?,
                ) {
                    resultImageView.setImageBitmap(resource)
                    resultImageView.imageTintList = null
                    resetButton.isVisible = true
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun showDrawTextView(@ColorInt color: Int, text: String) =
        with(binding) {
            drumGroup.isVisible = false
            resultImageView.isVisible = false
            drawTextView.isVisible = true
            animateDrawTextView(color, text)
        }

    private fun animateDrawTextView(@ColorInt color: Int, text: String) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { valueAnimator ->
            val updateText = text.subSequence(
                startIndex = 0,
                endIndex = (valueAnimator.animatedFraction * text.length).toInt()
            ).toString()
            binding.drawTextView.field = DrawTextField(text = updateText, color = color)
        }
        animator.setDuration(1L).start()
        animator.addListener(
            onEnd = {
                binding.resetButton.isVisible = true
            }
        )
    }

    companion object {
        private const val RED_STRING = "красный"
        private const val YELLOW_STRING = "желтый"
        private const val CYAN_STRING = "голубой"
        private const val PURPLE_STRING = "фиолетовый"
    }
}