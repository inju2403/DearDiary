package com.example.deardiary.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.deardiary.R
import com.example.deardiary.data.DetailViewModel
import com.google.android.material.snackbar.Snackbar
import com.takisoft.datetimepicker.DatePickerDialog
import com.takisoft.datetimepicker.TimePickerDialog
import kotlinx.android.synthetic.main.activity_detail.*
import java.io.File
import java.util.*

class DetailActivity : AppCompatActivity() {

    private var viewModel: DetailViewModel? = null
    private val dialogCalendar = Calendar.getInstance()
    private val REQUEST_IMAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(detailToolbar)
        supportActionBar!!.title = ""

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(DetailViewModel::class.java)
        }

        viewModel!!.diaryLiveData.observe (this, Observer {
            titleEdit.setText(it.title)
            contentEdit.setText(it.content)
            alarmInfoView.setAlarmDate(it.alarmTime)
            locationInfoView.setLocation(it.latitude, it.longitude)
            weatherInfoView.setWeather(it.weather)

            val imageFile = File(
                getDir("image", Context.MODE_PRIVATE),
                it.imageFile)

            bgImage.setImageURI(imageFile.toUri())
        })

        val diaryId = intent.getStringExtra("DIARY_ID")
        if(diaryId != null) viewModel!!.loadDiary(diaryId)


        titleEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel!!.diaryData.title = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        contentEdit.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel!!.diaryData.content = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    private fun openDateDialog() {
        val datePickerDialog = DatePickerDialog(this)
        datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
            dialogCalendar.set(year, month, dayOfMonth)
            openTimeDialog()
        }
        datePickerDialog.show()
    }

    private fun openTimeDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                dialogCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                dialogCalendar.set(Calendar.MINUTE, minute)

                viewModel?.setAlarm(dialogCalendar.time)
            },
            0, 0, false)
        timePickerDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    @SuppressLint("MissingPermission")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_attach -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_IMAGE)
            }
            R.id.action_save -> {
                viewModel?.addOrUpdateDiary(this)
                Toast.makeText(this,
                    "저장 완료", Toast.LENGTH_LONG).show()
                finish()
            }
            R.id.action_delete -> {
                val diaryId = intent.getStringExtra("DIARY_ID")
                if(diaryId == null) {
                    Toast.makeText(this,
                        "저장되지 않은 일기는 삭제할 수 없습니다", Toast.LENGTH_LONG).show()
                }
                else {
                    viewModel?.deleteDiary(diaryId)
                    Toast.makeText(this,
                        "삭제 완료", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            R.id.menu_share -> {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, supportActionBar?.title)
                intent.putExtra(Intent.EXTRA_TEXT, contentEdit.text.toString())

                startActivity(intent)
            }
            R.id.menu_alarm -> {
                if(viewModel?.diaryData?.alarmTime!!.after(Date())) {
                    AlertDialog.Builder(this)
                        .setTitle("안내")
                        .setMessage("기존에 알람이 설정되어 있습니다. 삭제 또는 재설정할 수 있습니다.")
                        .setPositiveButton("재설정", DialogInterface.OnClickListener { dialog, which ->
                            openDateDialog()
                        })
                        .setNegativeButton("삭제", DialogInterface.OnClickListener { dialog, which ->
                            viewModel?.deleteAlarm()
                        })
                        .show()
                }
                else {
                    openDateDialog()
                }
            }
            R.id.menu_location -> {
                AlertDialog.Builder(this)
                    .setTitle("안내")
                    .setMessage("현재 위치를 일기에 저장하거나 삭제할 수 있습니다.")
                    .setPositiveButton("위치지정", DialogInterface.OnClickListener { dialog, which ->
                        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                        if(!isGPSEnabled && !isNetworkEnabled) {
                            Snackbar.make(
                                toolbarLayout,
                                "폰의 위치기능을 켜야 기능을 사용할 수 있습니다.",
                                Snackbar.LENGTH_LONG)
                                .setAction("설정", View.OnClickListener {
                                    val goToSettings = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    startActivity(goToSettings)
                                }).show()
                        }
                        else {
                            val criteria = Criteria()
                            criteria.accuracy = Criteria.ACCURACY_MEDIUM
                            criteria.powerRequirement = Criteria.POWER_MEDIUM

                            locationManager.requestSingleUpdate(criteria, object :
                                LocationListener {
                                override fun onLocationChanged(location: Location?) {
                                    location?.run {
                                        viewModel!!.setLocation(latitude, longitude)
                                    }
                                }

                                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                }

                                override fun onProviderEnabled(provider: String?) {
                                }

                                override fun onProviderDisabled(provider: String?) {
                                }

                            }, null)
                        }
                    })
                    .setNegativeButton("삭제", DialogInterface.OnClickListener { dialog, which ->
                        viewModel!!.deleteLocation()
                    })
                    .show()
            }
            R.id.menu_weather -> {
                AlertDialog.Builder(this)
                    .setTitle("안내")
                    .setMessage("현재 날씨를 일기에 저장하거나 삭제할 수 있습니다.")
                    .setPositiveButton("날씨 가져오기", DialogInterface.OnClickListener { dialog, which ->
                        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                        if(!isGPSEnabled && !isNetworkEnabled) {
                            Snackbar.make(
                                toolbarLayout,
                                "폰의 위치기능을 켜야 기능을 사용할 수 있습니다.",
                                Snackbar.LENGTH_LONG)
                                .setAction("설정", View.OnClickListener {
                                    val goToSettings = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    startActivity(goToSettings)
                                }).show()
                        }
                        else {
                            val criteria = Criteria()
                            criteria.accuracy = Criteria.ACCURACY_MEDIUM
                            criteria.powerRequirement = Criteria.POWER_MEDIUM

                            locationManager.requestSingleUpdate(criteria, object :
                                LocationListener {
                                override fun onLocationChanged(location: Location?) {
                                    location?.run {
                                        viewModel!!.setWeather(latitude, longitude)
                                    }
                                }

                                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                }

                                override fun onProviderEnabled(provider: String?) {
                                }

                                override fun onProviderDisabled(provider: String?) {
                                }

                            }, null)
                        }
                    })
                    .setNegativeButton("삭제", DialogInterface.OnClickListener { dialog, which ->
                        viewModel!!.deleteWeather()
                    })
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        super.onBackPressed()
 //       viewModel?.addOrUpdateDiary(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                val inputStream = data?.data?.let { contentResolver.openInputStream(it) }
                inputStream?.let {
                    val image = BitmapFactory.decodeStream(it)

                    bgImage.setImageURI(null)
                    image?.let { viewModel?.setImageFile(this, it) }

                    it.close()
                }
            }
            catch (e: Exception) {
                println(e)
            }
        }
    }
}
