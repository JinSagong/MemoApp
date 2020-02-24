package com.jin.programmers_memo.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.createObject
import io.realm.kotlin.where

class MemoDao(private val mRealm: Realm) {

    fun getAll() = mRealm.where<Memo>().findAll()!!

    fun insert(title: String, content: String, date: Long, image: ArrayList<String>) =
        mRealm.executeTransaction {
            val newObject =
                it.createObject<Memo>((it.where<Memo>().max("id")?.toLong() ?: 0) + 1)
            newObject.title = title
            newObject.content = content
            newObject.dateCreated = date
            newObject.dateLastEdited = date
            newObject.favorite = false
            newObject.image = RealmList<String>().apply { this.addAll(image) }
        }

    fun delete(id: Long) = mRealm.executeTransaction {
        it.where<Memo>().equalTo("id", id).findFirst()?.deleteFromRealm()
    }

    fun update(
        id: Long, title: String?, content: String?,
        date: Long?, favorite: Boolean?, image: ArrayList<String>?
    ) = mRealm.executeTransaction {
        val obj = it.where<Memo>().equalTo("id", id).findFirst()
        title?.let { column -> obj?.title = column }
        content?.let { column -> obj?.content = column }
        date?.let { column -> obj?.dateLastEdited = column }
        favorite?.let { column -> obj?.favorite = column }
        image?.let { column -> obj?.image = RealmList<String>().apply { this.addAll(column) } }
    }
}