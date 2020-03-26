package com.example.deardiary


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deardiary.data.DiaryListAdapter
import com.example.deardiary.data.ListViewModel
import kotlinx.android.synthetic.main.fragment_diary_list.*

class DiaryListFragment : Fragment() {

    private lateinit var listAdapter : DiaryListAdapter
    private var viewModel : ListViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diary_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activity!!.application!!.let {
            ViewModelProvider(activity!!.viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(ListViewModel::class.java)
        }
        viewModel!!.let {
            it.diaryLiveData.value?.let {
                listAdapter = DiaryListAdapter(it)
                diaryListView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                diaryListView.adapter = listAdapter
                listAdapter.itemClickListener = {
                    val intent = Intent(activity, DetailActivity::class.java)
                    intent.putExtra("DIARY_ID", it)
                    startActivity(intent)
                }
            }
            it.diaryLiveData.observe(this,
                Observer {
                    listAdapter.notifyDataSetChanged()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        listAdapter.notifyDataSetChanged()
    }
}
