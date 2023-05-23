package io.lacanh.aiassistant.component.paging

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BasePagingDataAdapter<T: Any> : PagingDataAdapter<T, RecyclerView.ViewHolder>(BaseComparator()) {
    class BaseComparator<U: Any> : DiffUtil.ItemCallback<U>() {
        override fun areItemsTheSame(oldItem: U, newItem: U): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: U, newItem: U): Boolean {
            return false
        }
    }
}