package com.jin.programmers_memo.viewmodel

import androidx.lifecycle.ViewModel
import com.jin.programmers_memo.R
import com.jin.programmers_memo.model.MemoDao
import io.realm.Realm
import io.realm.Sort

class MemoViewmodel : ViewModel() {
    private val mRealm = Realm.getDefaultInstance()
    private val memoDao = MemoDao(mRealm)
    private val memo = memoDao.getAll()
    private var memoSorted = memo

    fun sort(methodId: Int, orderId: Int) {
        val method = when (methodId) {
            R.id.sortRadioButton1 -> "title"
            R.id.sortRadioButton2 -> "dateCreated"
            R.id.sortRadioButton3 -> "dateLastEdited"
            else -> return
        }
        val order = if (orderId == R.id.ascRadioButton) Sort.ASCENDING else Sort.DESCENDING
        memoSorted = memo.sort(method, order)
    }

    fun getAll() = memoSorted

    fun getSearchResult(query: String) = memoSorted.where()
        .contains("title", query).or().contains("content", query).findAll()!!

    fun getById(id: Long) = memo.where().equalTo("id", id).findFirst()!!

    fun insert(title: String, content: String, date: Long, image: ArrayList<String>) =
        memoDao.insert(title, content, date, image)

    fun delete(id: Long) = memoDao.delete(id)

    fun update(id: Long, title: String, content: String, date: Long, image: ArrayList<String>) =
        memoDao.update(id, title, content, date, null, image)

    fun setFavorite(id: Long, favorite: Boolean) =
        memoDao.update(id, null, null, null, favorite, null)

    override fun onCleared() {
        mRealm.close()
        super.onCleared()
    }
}