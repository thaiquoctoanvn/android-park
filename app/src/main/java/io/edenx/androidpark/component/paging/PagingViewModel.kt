package io.edenx.androidpark.component.paging

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import io.edenx.androidpark.data.model.DoggoImageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PagingViewModel @Inject constructor(
    private val pagingRepo: PagingRepo
) : ViewModel() {
    val doggoImageObserver = MutableLiveData<PagingData<DoggoImageModel>>()

    fun getDoggoImages() {
        viewModelScope.launch {
            pagingRepo.getDoggoImages()
                .cachedIn(viewModelScope)
                .collectLatest {
                    withContext(Dispatchers.Main) {
                        doggoImageObserver.value = it
                    }
                }
        }
    }
}