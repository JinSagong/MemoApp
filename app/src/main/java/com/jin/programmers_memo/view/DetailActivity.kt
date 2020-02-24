package com.jin.programmers_memo.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.programmers_memo.R
import com.jin.programmers_memo.model.Memo
import com.jin.programmers_memo.utils.*
import com.jin.programmers_memo.viewmodel.MemoViewmodel

import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlinx.android.synthetic.main.dialog_delete.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.startActivity

class DetailActivity : AppCompatActivity() {
    private val memoViewModel by lazy { ViewModelProvider(this).get(MemoViewmodel::class.java) }
    private val id by lazy { intent.getLongExtra("id", 0L) }
    private lateinit var memo: Memo
    private val deleteDialog by lazy { BottomSheetDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(detailToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        detailToolbar.setNavigationOnClickListener { onBackPressed() }

        setDeleteDialog()

        memo = memoViewModel.getById(id)
        showMemo()
    }

    private fun setDeleteDialog() {
        deleteDialog.setContentView(R.layout.dialog_delete)
        deleteDialog.behavior.skipCollapsed = true

        deleteDialog.setOnShowListener {
            deleteDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        deleteDialog.deleteCancelTextView.setOnClickListener { deleteDialog.dismiss() }

        deleteDialog.deleteDoneTextView.setOnClickListener {
            memoViewModel.delete(id)
            deleteDialog.dismiss()
            finish()
        }
    }

    private fun showMemo() {
        titleTextView.text = memo.title
        contentTextView.text = memo.content
        val dateLastEditedText =
            "${getString(R.string.text_last_edited)}: ${DateToString.getDateString(memo.dateLastEdited)}"
        val dateCreatedText =
            "${getString(R.string.text_created)}: ${DateToString.getDateString(memo.dateCreated)}"
        dateLastEditedTextView.text = dateLastEditedText
        dateCreatedTextView.text = dateCreatedText

        detailImageLayout.removeAllViews()
        memo.image.forEachWithIndex { position, uri ->
            val photoImageView = ImageView(this)
            photoImageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            photoImageView.adjustViewBounds = true
            Glide.with(this).load(uri).listener(GlideRequestListener.getListener(this, false))
                .placeholder(R.drawable.ic_loading_96dp)
                .error(R.drawable.ic_no_image_available).into(photoImageView)
            photoImageView.setOnClickListener {
                startActivity<PhotoActivity>(
                    "uri" to memo.image.toTypedArray(), "position" to position
                )
            }
            detailImageLayout.addView(photoImageView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        menu.findItem(R.id.action_favorite)
            .setIcon(if (memo.favorite) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                memoViewModel.setFavorite(id, !memo.favorite)
                /* ViewModel을 통해 데이터 값이 변경되면 RealmResults는 자체적으로 Observe 기능이
                *  포함되어 있어 변경된 값을 미리 정의한 변수를 통해 바로 이용할 수 있습니다.
                * */
                item.setIcon(if (memo.favorite) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp)
                true
            }
            R.id.action_edit -> {
                startActivityForResult(
                    Intent(this, EditActivity::class.java)
                        .apply { this.putExtra("id", id) }, EDIT_REQUEST
                )
                true
            }
            R.id.action_delete -> deleteDialog.show().run { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        InfoToast.infoToast?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /* 마찬가지로 EditActivity에서 데이터 값이 변경되었더라도 Singleton의 Realm Instance를
        *  이용하므로 별도의 Observer 구현 없이 RealmResults를 이용하여 업데이트된 데이터 값을
        *  바로 이용할 수 있습니다.
        * */
        if (requestCode == EDIT_REQUEST) if (resultCode == DELETE_RESULT) finish() else showMemo()
    }
}