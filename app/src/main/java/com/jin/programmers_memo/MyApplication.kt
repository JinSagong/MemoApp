package com.jin.programmers_memo

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder().schemaVersion(1).build())
    }
}