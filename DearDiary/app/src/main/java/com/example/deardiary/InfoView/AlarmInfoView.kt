package com.example.deardiary.InfoView

import android.content.Context
import android.util.AttributeSet
import com.example.deardiary.R
import kotlinx.android.synthetic.main.view_info.view.*
import java.text.SimpleDateFormat
import java.util.*

class AlarmInfoView @JvmOverloads constructor (context: Context,
                                               attrs: AttributeSet? = null,
                                               defStyleAttr: Int = 0)
    : InfoView(context, attrs, defStyleAttr) {
    companion object {
        private val dateFormat = SimpleDateFormat("yy/MM/dd HH:mm")
    }

    init {
        typeImage.setImageResource(R.drawable.ic_alarm)
        infoText.setText("")
    }

    fun setAlarmDate(alarmDate: Date) {
        if(alarmDate.before(Date())) {
            infoText.setText("일기를 언제 다시 보고 싶으신가요?")
        }
        else {
            infoText.setText(dateFormat.format(alarmDate))
        }
    }
}