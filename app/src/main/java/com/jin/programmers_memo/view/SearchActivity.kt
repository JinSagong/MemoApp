package com.jin.programmers_memo.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jin.programmers_memo.R
import com.jin.programmers_memo.adapter.MemoAdapter
import com.jin.programmers_memo.model.Memo
import com.jin.programmers_memo.viewmodel.MemoViewmodel

import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.content_search.*

class SearchActivity : AppCompatActivity() {
    private val memoViewModel by lazy { ViewModelProvider(this).get(MemoViewmodel::class.java) }
    private val itemList = arrayListOf<Memo>()
    private val mAdapter by lazy { MemoAdapter(memoViewModel, itemList) }

    private var query = ""
    private var onSearch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(searchToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        overridePendingTransition(0, 0)

        memoViewModel.sort(
            intent.getIntExtra("sortMethodId", 0),
            intent.getIntExtra("sortOrderId", 0)
        )

        searchRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        searchRecyclerView.adapter = mAdapter

        backImageView.setOnClickListener {
            onSearch = false
            onBackPressed()
        }
        closeImageView.setOnClickListener { searchEditText.setText("") }

        searchEditText.addTextChangedListener {
            query = it.toString()
            notifyDataChanged()
            searchRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun notifyDataChanged() {
        if (query.isEmpty()) {
            // 검색어를 입력하지 않으면 검색 안내를 표시합니다.
            closeImageView.visibility = View.GONE
            searchRecyclerView.visibility = View.GONE
            searchDescriptionTextView.text = getString(R.string.text_search_page)
            searchDescriptionTextView.visibility = View.VISIBLE
        } else {
            closeImageView.visibility = View.VISIBLE
            itemList.clear()
            memoViewModel.getSearchResult(query).let { m ->
                if (m.isNotEmpty()) {
                    // 검색 결과를 표시합니다.
                    itemList.addAll(m)
                    mAdapter.notifyDataSetChanged()
                    searchRecyclerView.visibility = View.VISIBLE
                    searchDescriptionTextView.visibility = View.GONE
                } else {
                    // 검색 결과가 없음을 나타냅니다.
                    searchRecyclerView.visibility = View.GONE
                    searchDescriptionTextView.text = getString(R.string.text_no_search_result)
                    searchDescriptionTextView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mAdapter.setOffDetail()
        notifyDataChanged()
    }

    override fun onPause() {
        if (!onSearch) overridePendingTransition(0, 0)
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onSearch = false
    }
}
