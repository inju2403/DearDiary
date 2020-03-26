package com.example.deardiary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.deardiary.data.DetailViewModel
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private var viewModel: DetailViewModel? = null

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_attach -> {

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
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        super.onBackPressed()
 //       viewModel?.addOrUpdateDiary(this)
    }
}
