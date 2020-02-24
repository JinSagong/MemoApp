package com.jin.programmers_memo.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.PhotoView

class PhotoPagerAdapter : PagerAdapter() {
    private val items = arrayListOf<PhotoView>()

    fun addItem(item: PhotoView) = items.add(item)

    override fun instantiateItem(container: ViewGroup, position: Int) =
        items[position].apply { container.addView(this) }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) =
        container.removeView(`object` as View)

    override fun getCount() = items.size

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`
}