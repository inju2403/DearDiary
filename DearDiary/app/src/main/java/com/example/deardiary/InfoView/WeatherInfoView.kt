package com.example.deardiary.InfoView

import android.content.Context
import android.util.AttributeSet
import com.example.deardiary.R
import kotlinx.android.synthetic.main.view_info.view.*

class WeatherInfoView @JvmOverloads constructor (context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0)
    : InfoView(context, attrs, defStyleAttr) {

    init {
        typeImage.setImageResource(R.drawable.ic_weather)
        infoText.setText("")
    }

    fun setWeather(weatherText: String) {
        if(weatherText.isEmpty()) {
            infoText.setText("오늘 날씨는 어떤가요?")
        }
        else {
            infoText.setText(weatherText)
        }
    }
}