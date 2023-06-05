package io.edenx.androidpark.component.nav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.edenx.androidpark.R
import io.edenx.androidpark.component.base.BaseFragment
import io.edenx.androidpark.data.model.MenuItem
import io.edenx.androidpark.databinding.FragmentSecondBinding
import io.edenx.androidpark.databinding.ViewMessageTypingBoxBinding

class SecondFragment : BaseFragment<FragmentSecondBinding>(FragmentSecondBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = SecondAdapter(
                (1..15).map {
                    MenuItem(it, name = "Hey you!")
                }.toMutableList()
            )
            addItemDecoration(DividerItemDecoration(this.context, RecyclerView.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_spacing_16)!!)
            })
        }
    }

    override fun setListener() {
        binding.btBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    class SecondAdapter(val items: MutableList<MenuItem>) : RecyclerView.Adapter<SecondAdapter.ItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            return ItemViewHolder(ViewMessageTypingBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.bindData(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ItemViewHolder(private val binding: ViewMessageTypingBoxBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bindData(item: MenuItem) {
                binding.tb.setText(item.name)
            }
        }
    }
}