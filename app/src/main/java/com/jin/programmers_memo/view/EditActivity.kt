package com.jin.programmers_memo.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.programmers_memo.R
import com.jin.programmers_memo.model.Memo
import com.jin.programmers_memo.utils.DELETE_RESULT
import com.jin.programmers_memo.utils.GlideRequestListener
import com.jin.programmers_memo.utils.InfoToast
import com.jin.programmers_memo.utils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
import com.jin.programmers_memo.viewmodel.MemoViewmodel
import gun0912.tedimagepicker.builder.TedImagePicker

import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.content_edit.*
import kotlinx.android.synthetic.main.dialog_import_external_image.*
import org.jetbrains.anko.*
import java.util.*

class EditActivity : AppCompatActivity() {
    private val memoViewModel by lazy { ViewModelProvider(this).get(MemoViewmodel::class.java) }
    private val id by lazy { intent.getLongExtra("id", 0L) }
    private lateinit var memo: Memo
    private val image = arrayListOf<String>()
    private val importDialog by lazy { BottomSheetDialog(this) }

    private var isDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        setSupportActionBar(editToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        editToolbar.setNavigationOnClickListener { onBackPressed() }

        if (id != 0L) {
            // 메모 상세보기 화면에서 바로 수정 화면으로 넘어오는 경우에 다음의 작업을 수행합니다.
            overridePendingTransition(0, 0)
            memo = memoViewModel.getById(id)
            titleEditText.setText(memo.title)
            contentEditText.setText(memo.content)
            memo.image.forEach { addPhoto(it, false) }
        }

        setImportDialog()
    }

    private fun setImportDialog() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        importDialog.setContentView(R.layout.dialog_import_external_image)
        importDialog.setCanceledOnTouchOutside(false)
        importDialog.behavior.isHideable = false

        importDialog.setOnShowListener {
            GlideRequestListener.success = false
            importDialog.importEditText.setText("")
            importDialog.importImageVIew.visibility = View.GONE
            importDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        importDialog.importTextView.setOnClickListener {
            imm.hideSoftInputFromWindow(importDialog.importEditText.windowToken, 0)
            importDialog.importImageVIew.visibility = View.VISIBLE
            Glide.with(this).load(importDialog.importEditText.text.toString())
                .listener(GlideRequestListener.getListener(this, true))
                .placeholder(R.drawable.ic_loading_96dp)
                .error(R.drawable.ic_no_image_available).into(importDialog.importImageVIew)
        }

        importDialog.importCancelTextView.setOnClickListener { importDialog.dismiss() }

        importDialog.importDoneTextView.setOnClickListener {
            // 입력받은 url이 호출 가능한 이미지인지 Dialog에서 미리 판단 후 메모에 추가합니다..
            if (GlideRequestListener.success) {
                addPhoto(importDialog.importEditText.text.toString(), true)
                importDialog.dismiss()
            } else {
                InfoToast.infoToast?.cancel()
                InfoToast.infoToast = longToast(R.string.text_import_disable)
            }
        }

        /* 이미지 로딩에 실패하면 계속해서 로딩을 시도하는데 그 작업을 중단하지 않으면
        *  다른 로딩 작업이 진행될 때 UI가 겹쳐지므로 이를 방지해야 합니다.
        * */
        importDialog.setOnDismissListener { Glide.with(this).clear(importDialog.importImageVIew) }
    }

    private fun addPhoto(uri: String, isImport: Boolean) {
        image.add(uri)

        val photoLayout = RelativeLayout(this)
        photoLayout.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        val photoImageView = ImageView(this)
        photoImageView.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        photoImageView.adjustViewBounds = true

        Glide.with(this).load(uri).listener(GlideRequestListener.getListener(this, isImport))
            .placeholder(R.drawable.ic_loading_96dp).error(R.drawable.ic_no_image_available)
            .into(photoImageView)
        photoImageView.setOnClickListener {
            startActivity<PhotoActivity>(
                "uri" to image.toTypedArray(),
                "position" to editImageLayout.indexOfChild(photoLayout)
            )
        }

        val removeButtonImageView = ImageView(this)
        removeButtonImageView.setPadding(
            resources.getDimensionPixelSize(R.dimen.paddingDefault),
            resources.getDimensionPixelSize(R.dimen.paddingDefault),
            resources.getDimensionPixelSize(R.dimen.paddingDefault),
            resources.getDimensionPixelSize(R.dimen.paddingDefault)
        )
        removeButtonImageView.setImageResource(R.drawable.ic_remove_circle_24dp)
        removeButtonImageView.setOnClickListener { removePhoto(photoLayout) }

        photoLayout.addView(photoImageView)
        photoLayout.addView(removeButtonImageView)
        editImageLayout.addView(photoLayout)
    }

    private fun removePhoto(photoLayout: RelativeLayout) {
        image.removeAt(editImageLayout.indexOfChild(photoLayout))
        editImageLayout.removeView(photoLayout)
    }

    private fun selectImageFromAlbumAndCamera() {
        // 권한 허가 여부를 먼저 확인합니다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
        )
        else TedImagePicker.with(this).startMultiImage { uriList ->
            uriList.forEach { addPhoto(it.toString(), false) }
        }
    }

    private fun save() {
        val titleText = titleEditText.text.toString()
        val contentText = contentEditText.text.toString()

        // 작성 내용이 없으면 메모를 저장하지 않습니다.
        if (titleText.isEmpty() && contentText.isEmpty() && image.isEmpty()) {
            isDeleted = true
            InfoToast.infoToast?.cancel()
            InfoToast.infoToast = longToast(R.string.text_no_input)
            memoViewModel.delete(id)
            setResult(DELETE_RESULT)
        } else {
            val time = Calendar.getInstance().timeInMillis
            if (id == 0L) memoViewModel.insert(titleText, contentText, time, image)
            else memoViewModel.update(id, titleText, contentText, time, image)
        }
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_ap_album_and_camera -> selectImageFromAlbumAndCamera().run { true }
            R.id.action_ap_external_image -> importDialog.show().run { true }
            R.id.action_save -> save().run { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        if (id != 0L && !isDeleted) overridePendingTransition(0, 0)
        super.onPause()
        if (!isDeleted) InfoToast.infoToast?.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            TedImagePicker.with(this).startMultiImage { uriList ->
                uriList.forEach { addPhoto(it.toString(), false) }
            }
        } else {
            InfoToast.infoToast?.cancel()
            InfoToast.infoToast = longToast(R.string.text_no_permission)
        }
    }
}