package com.example.deardiary.data

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class DiaryDao(private val realm : Realm) {

    fun getAllDiarys() : RealmResults<DiaryData> {
        return realm.where(DiaryData::class.java)
            .sort("createdAt", Sort.DESCENDING)
            .findAll()
    }

    fun selectDiary(id : String) : DiaryData {
        return realm.where(DiaryData::class.java)
            .equalTo("id", id)
            .findFirst() as DiaryData
    }

    fun addOrUpdateDiary(diaryData : DiaryData)  {
        realm.executeTransaction {
            diaryData.createdAt = Date()

            if(diaryData.content.length > 100)
                diaryData.summary = diaryData.content.substring(0..100)
            else
                diaryData.summary = diaryData.content

            it.copyToRealmOrUpdate(diaryData)
        }
    }

    fun deleteDiary(id : String) {
        realm.executeTransaction {
            it.where(DiaryData::class.java)
                .equalTo("id", id)
                .findFirst()?.let {
                    it?.deleteFromRealm()
                }
        }
    }

    fun getActiveAlarms() : RealmResults<DiaryData> {
        return realm.where(DiaryData::class.java)
            .greaterThan("alarmTime", Date())
            .findAll()
    }
}