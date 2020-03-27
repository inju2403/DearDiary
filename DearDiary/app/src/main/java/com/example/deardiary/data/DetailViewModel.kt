package com.example.deardiary.data

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deardiary.AlarmTool
import io.realm.Realm
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

class DetailViewModel : ViewModel() {

    var diaryData = DiaryData()
    val diaryLiveData : MutableLiveData<DiaryData> by lazy {
        MutableLiveData<DiaryData>().apply {
            value = diaryData
        }
    }

    private val realm : Realm by lazy {
        Realm.getDefaultInstance()
    }

    private val diaryDao : DiaryDao by lazy {
        DiaryDao(realm)
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    fun loadDiary(id: String) {
        diaryData = realm.copyFromRealm(diaryDao.selectDiary(id))
        diaryLiveData.value = diaryData
    }

    fun setAlarm(time: Date) {
        diaryData.alarmTime = time
        diaryLiveData.value = diaryData
    }

    fun deleteAlarm() {
        diaryData.alarmTime = Date(0)
        diaryLiveData.value = diaryData
    }

    fun setLocation(latitude: Double, longitude: Double) {
        diaryData.latitude = latitude
        diaryData.longitude = longitude
        diaryLiveData.value = diaryData
    }

    fun deleteLocation() {
        diaryData.latitude = 0.0
        diaryData.longitude = 0.0
        diaryLiveData.value = diaryData
    }

    fun setWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            diaryData.weather = WeatherData.getCurrentWeather(latitude, longitude)
            diaryLiveData.value = diaryData
        }
    }

    fun deleteWeather() {
        diaryData.weather = ""
        diaryLiveData.value = diaryData
    }

    fun setImageFile(context: Context, bitmap: Bitmap) {
        val imageFile = File(
            context.getDir("image", Context.MODE_PRIVATE),
            diaryData.id + ".jpg")

        if(imageFile.exists()) imageFile.delete()

        try {
            imageFile.createNewFile()
            val outputStream = FileOutputStream(imageFile)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.close()

            diaryData.imageFile = diaryData.id + ".jpg"
            diaryLiveData.value = diaryData
        }
        catch (e: Exception) {
            println(e)
        }
    }

    fun addOrUpdateDiary(context: Context) {
        diaryDao.addOrUpdateDiary(diaryData)

        AlarmTool.deleteAlarm(context, diaryData.id)
        if(diaryData.alarmTime.after(Date())) {
            AlarmTool.addAlarm(context, diaryData.id, diaryData.alarmTime)
        }
    }

    fun deleteDiary(id: String) {
        diaryDao.deleteDiary(id)
    }

}