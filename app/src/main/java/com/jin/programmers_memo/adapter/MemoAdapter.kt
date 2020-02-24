package com.jin.programmers_memo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jin.programmers_memo.R
import com.jin.programmers_memo.model.Memo
import com.jin.programmers_memo.utils.DateToString
import com.jin.programmers_memo.view.DetailActivity
import com.jin.programmers_memo.viewmodel.MemoViewmodel
import kotlinx.android.synthetic.main.item_memolist.view.*
import org.jetbrains.anko.startActivity

class MemoAdapter(vm: MemoViewmodel, itemList: List<Memo>) :
    RecyclerView.Adapter<MemoAdapter.MyViewHolder>() {
    private var list = itemList
    private val memoViewmodel = vm

    // 중복 터치를 방지하는데 이용되었습니다.
    private var onDetail = false

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val favorite: ImageView = v.itemFavoriteImageView
        val title: TextView = v.itemTitleTextView
        val content: TextView = v.itemContentTextView
        val time: TextView = v.itemTimeTextView
        val photo: ImageView = v.itemPhotoImageView
    }

    fun setOffDetail() {
        onDetail = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memolist, parent, false)
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.title
        holder.content.text = item.content
        holder.time.text = DateToString.getDateString(item.dateLastEdited)
        holder.favorite.setImageResource(
            if (item.favorite) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp
        )
        holder.favorite.setOnClickListener {
            memoViewmodel.setFavorite(item.id, !item.favorite)
            notifyDataSetChanged()
        }
        if (item.image.isNotEmpty()) {
            holder.photo.visibility = View.VISIBLE
            Glide.with(holder.itemView).load(item.image.first())
                .placeholder(R.drawable.ic_loading_96dp)
                .error(R.drawable.ic_no_image_available).centerCrop().into(holder.photo)
        } else holder.photo.visibility = View.GONE

        holder.itemView.setOnClickListener {
            if (!onDetail) {
                onDetail = true
                it.context.startActivity<DetailActivity>("id" to item.id)
            }
        }
    }
}