package com.jin.programmers_memo.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.jin.programmers_memo.R
import com.jin.programmers_memo.adapter.PhotoPagerAdapter
import com.jin.programmers_memo.utils.InfoToast
import kotlinx.android.synthetic.main.activity_photo.*

class PhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        val uri = intent.getStringArrayExtra("uri")
        val position = intent.getIntExtra("position", 0)
        val adapter = PhotoPagerAdapter()

        uri?.forEach {
            val photoView = PhotoView(this)
            Glide.with(this).load(it).placeholder(R.drawable.ic_loading_96dp)
                .error(R.drawable.ic_no_image_available).into(photoView)
            adapter.addItem(photoView)
        }

        /* ViewPager와 PhotoView를 동시에 이용할 때 가끔씩 발생하는 에러를 막기 위해
        *  onTouchEvent와 onInterceptTouchEvent에 예외처리를 하였습니다.
        * */
        val photoViewPager = object : ViewPager(this) {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouchEvent(ev: MotionEvent?) = try {
                super.onTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                false
            }

            override fun onInterceptTouchEvent(ev: MotionEvent?) = try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                false
            }
        }
        photoViewPager.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        photoLayout.addView(photoViewPager)
        photoViewPager.adapter = adapter
        photoViewPager.currentItem = position
    }

    override fun onPause() {
        super.onPause()
        InfoToast.infoToast?.cancel()
    }
}