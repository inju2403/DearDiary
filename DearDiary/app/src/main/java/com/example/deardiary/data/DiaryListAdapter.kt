package com.example.deardiary.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.deardiary.R
import kotlinx.android.synthetic.main.item_diary.view.*
import java.text.SimpleDateFormat

class DiaryListAdapter (private val list : MutableList<DiaryData>) : RecyclerView.Adapter<ItemViewHolder> () {

    private val dateFormat = SimpleDateFormat("yy MM/dd HH:mm")

    lateinit var itemClickListener : (itemId : String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        itemView.setOnClickListener {
            itemClickListener?.run {
                val memoId = it.tag as String
                this(memoId)
            }
        }
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if(list[position].title.isNotEmpty()) {
            holder.containerView.titleView.visibility = View.VISIBLE
            holder.containerView.titleView.text = list[position].title
        }
        else
        {
            holder.containerView.titleView.visibility = View.GONE
        }
        holder.containerView.summaryView.text = list[position].summary
        holder.containerView.dateView.text = dateFormat.format(list[position].createdAt)
        holder.containerView.tag = list[position].id
    }

}