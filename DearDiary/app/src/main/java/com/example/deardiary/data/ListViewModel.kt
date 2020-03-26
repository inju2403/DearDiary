package com.example.deardiary.data

import androidx.lifecycle.ViewModel
import io.realm.Realm

class ListViewModel : ViewModel() {

    private val realm : Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val diaryDao : DiaryDao by lazy {
        DiaryDao(realm)
    }

    val diaryLiveData : RealmLiveData<DiaryData> by lazy {
        RealmLiveData<DiaryData> (diaryDao.getAllDiarys())
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }
}