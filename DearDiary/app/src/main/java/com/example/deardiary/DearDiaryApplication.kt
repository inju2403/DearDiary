package com.example.deardiary

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import io.realm.Realm

class DearDiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        NaverMapSdk.getInstance(this).setClient(
            NaverMapSdk.NaverCloudPlatformClient("jjkijur1pk"))
    }
}