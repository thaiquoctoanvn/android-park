package io.edenx.androidplayground.component.media

import android.Manifest
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.ListenableFuture
import io.edenx.androidplayground.R
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.data.model.SimpleItem
import io.edenx.androidplayground.databinding.ActivityPlaylistBinding
import io.edenx.androidplayground.databinding.ItemTextBinding

class PlaylistActivity : BaseActivity<ActivityPlaylistBinding>(ActivityPlaylistBinding::inflate) {

    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    private val browser: MediaBrowser?
        get() = if (browserFuture.isDone) browserFuture.get() else null
    private val audioItems = mutableListOf<MediaItem>()
    private val permissionRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val multiplePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val isAllPermissionGrant = it.entries.all { permission ->
                permission.value
            }
            if (isAllPermissionGrant) {
                displayPlaylist()
            }
        }

    override fun onViewCreated() {
        Glide.with(this)
            .load("https://www.rollingstone.com/wp-content/uploads/2021/09/RS_500_Great_Songs_1800x1200.jpg?w=1581&h=1054&crop=1")
            .error(R.drawable.artwork_placeholder)
            .into(binding.imgPlaylist)
        binding.rvPlaylist.apply {
            playlistAdapter = PlaylistAdapter() { item, view ->
                audioItems.find { it.mediaId == item.id }?.let {
                    browser?.setMediaItem(it)
                    browser?.prepare()
                    startActivity(Intent(this@PlaylistActivity, MediaPlayerActivity::class.java))
                }
            }
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(this.context)
            addItemDecoration(
                DividerItemDecoration(
                    this.context,
                    RecyclerView.VERTICAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(
                            this@PlaylistActivity,
                            R.drawable.list_spacing_12
                        )!!
                    )
                })
        }
    }

    override fun onStart() {
        super.onStart()
        browserFuture =
            MediaBrowser.Builder(
                this,
                SessionToken(this, ComponentName(this, Media3PlaybackService::class.java))
            )
                .buildAsync()
        browserFuture.addListener({
            if (permissionRequired.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
                displayPlaylist()
            } else multiplePermissionRequest.launch(permissionRequired)

        }, ContextCompat.getMainExecutor(this))
    }

    override fun setListener() {
        binding.btPlayAll.setOnClickListener {
            browser?.setMediaItems(audioItems)
            browser?.prepare()
            startActivity(Intent(this, MediaPlayerActivity::class.java))
        }
    }

    private fun displayPlaylist() {

        audioItems.clear()
        audioItems.addAll(fetchLocalAudio())
        playlistAdapter.submitList(audioItems.map {
            SimpleItem(
                id = it.mediaId,
                title = it.mediaMetadata.title.toString()
            )
        })
    }

    private fun fetchLocalAudio(): MutableList<MediaItem> {
        val result = mutableMapOf<Long, MediaItem>()
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null
        )?.let {
            it.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn =
                    it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    if (result.containsKey(id)) continue
                    result[id] = MediaItem.Builder()
                        .setMediaId(id.toString())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(it.getString(nameColumn))
                                .build()
                        )
                        .setUri(
                            ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                        )
                        .build()
                }
            }
            it.close()
        }
        return result.values.toMutableList()
    }

    class PlaylistAdapter(
        mItems: List<SimpleItem> = listOf(),
        private val onItemClick: (SimpleItem, View) -> Unit = { _, _ -> }
    ) : ListAdapter<SimpleItem, PlaylistAdapter.ItemHolder>(AdapterDiff()) {
        init {
            submitList(mItems)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(
                ItemTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bindData(getItem(position))
        }

        inner class ItemHolder(private val binding: ItemTextBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bindData(item: SimpleItem) {
                with(binding) {
                    txt.text = item.title
                    root.setOnClickListener {
                        onItemClick(item, it)
                    }
                }
            }
        }

        // Diff adapter does not replace an element if a new element is recognized as the same.
        // We need to return false in both of checking funcs to ensure that the adapter always binds all data in case of duplication
        class AdapterDiff : DiffUtil.ItemCallback<SimpleItem>() {
            override fun areItemsTheSame(oldItem: SimpleItem, newItem: SimpleItem): Boolean {
                return false
            }

            override fun areContentsTheSame(oldItem: SimpleItem, newItem: SimpleItem): Boolean {
                return false
            }
        }
    }
}