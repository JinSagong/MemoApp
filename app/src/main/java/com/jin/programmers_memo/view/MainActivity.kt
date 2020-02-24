package com.jin.programmers_memo.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jin.programmers_memo.adapter.MemoAdapter
import com.jin.programmers_memo.R
import com.jin.programmers_memo.model.Memo
import com.jin.programmers_memo.viewmodel.MemoViewmodel

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_sort.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {
    private val memoViewModel by lazy { ViewModelProvider(this).get(MemoViewmodel::class.java) }
    private val itemList by lazy { arrayListOf<Memo>() }
    private val mAdapter by lazy { MemoAdapter(memoViewModel, itemList) }
    private val sortDialog by lazy { BottomSheetDialog(this) }

    // 중복 터치를 방지하는데 이용되었습니다.
    private var addFabTouched = false

    private var sortMethodId = 0
    private var sortOrderId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)

        setSortDialog()

        mainRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mainRecyclerView.adapter = mAdapter

        notifyDataChanged()

        addFab.setOnClickListener {
            if (!addFabTouched) {
                addFabTouched = true
                startActivityForResult(Intent(this, EditActivity::class.java), 0)
            }
        }
    }

    private fun setSortDialog() {
        // 이전에 사용하였던 정렬방식을 불러와 이용합니다.
        sortMethodId = defaultSharedPreferences.getInt("sortMethodId", R.id.sortRadioButton3)
        sortOrderId = defaultSharedPreferences.getInt("sortOrderId", R.id.descRadioButton)
        memoViewModel.sort(sortMethodId, sortOrderId)

        sortDialog.setContentView(R.layout.dialog_sort)
        sortDialog.behavior.skipCollapsed = true

        sortDialog.setOnShowListener {
            sortDialog.sortMethodRadioGroup.check(sortMethodId)
            sortDialog.sortOrderRadioGroup.check(sortOrderId)
            sortDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        sortDialog.sortCancelTextView.setOnClickListener { sortDialog.dismiss() }

        sortDialog.sortDoneTextView.setOnClickListener {
            sortMethodId = sortDialog.sortMethodRadioGroup.checkedRadioButtonId
            sortOrderId = sortDialog.sortOrderRadioGroup.checkedRadioButtonId
            defaultSharedPreferences.edit().putInt("sortMethodId", sortMethodId).apply()
            defaultSharedPreferences.edit().putInt("sortOrderId", sortOrderId).apply()
            memoViewModel.sort(sortMethodId, sortOrderId)
            notifyDataChanged()
            sortDialog.dismiss()
        }
    }

    private fun notifyDataChanged() {
        itemList.clear()
        itemList.addAll(memoViewModel.getAll())
        mAdapter.notifyDataSetChanged()

        if (itemList.isEmpty()) {
            mainRecyclerView.visibility = View.GONE
            mainDescriptionTextView.visibility = View.VISIBLE
        } else {
            mainRecyclerView.visibility = View.VISIBLE
            mainDescriptionTextView.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> startActivity<SearchActivity>(
                "sortMethodId" to sortMethodId, "sortOrderId" to sortOrderId
            ).run { true }
            R.id.action_sort -> sortDialog.show().run { true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        addFabTouched = false
        mAdapter.setOffDetail()
        notifyDataChanged()
    }
}