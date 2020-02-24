package com.jin.programmers_memo.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Memo(
    @PrimaryKey var id: Long = 0L,
    var title: String = "",
    var content: String = "",
    var dateCreated: Long = 0L,
    var dateLastEdited: Long = 0L,
    var favorite: Boolean = false,
    var image: RealmList<String> = RealmList()
) : RealmObject()