package com.example.deardiary

import android.app.Application
import io.realm.Realm

class DearDiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}