package io.edenx.androidpark.component.paging

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import io.edenx.androidpark.component.base.BaseActivity
import io.edenx.androidpark.data.model.DoggoImageModel
import io.edenx.androidpark.databinding.ActivityPagingBinding
import io.edenx.androidpark.databinding.ItemMenuBinding
import io.edenx.androidpark.util.DmitrysGridItemDecoration

@AndroidEntryPoint
class PagingActivity : BaseActivity<ActivityPagingBinding>(ActivityPagingBinding::inflate) {

    private lateinit var pagingAdapter: DoggoImageAdapter

    private val pagingViewModel by viewModels<PagingViewModel>()

    override fun onViewCreated() {
        binding.rvPaging.apply {
            pagingAdapter = DoggoImageAdapter()
            adapter = pagingAdapter
            layoutManager = GridLayoutManager(this.context, 2, RecyclerView.VERTICAL, false)
            addItemDecoration(DmitrysGridItemDecoration(24, 2))
        }
        pagingViewModel.getDoggoImages()
    }

    override fun setObserver() {
        pagingViewModel.doggoImageObserver.observe(this) {
            pagingAdapter.submitData(lifecycle, it)
        }
    }

    class DoggoImageAdapter :PagingDataAdapter<DoggoImageModel, RecyclerView.ViewHolder>(BasePagingDataAdapter.BaseComparator()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ItemViewHolder(ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as ItemViewHolder).binDate(getItem(position))
        }

        class ItemViewHolder(private val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
            fun binDate(item: DoggoImageModel?) {
                Glide.with(binding.imgMenu)
                    .load(item?.url)
                    .into(binding.imgMenu)
            }
        }
    }
}