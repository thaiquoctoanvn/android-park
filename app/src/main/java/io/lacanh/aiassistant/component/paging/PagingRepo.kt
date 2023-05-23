package io.lacanh.aiassistant.component.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.lacanh.aiassistant.data.Api
import io.lacanh.aiassistant.data.model.DoggoImageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PagingRepo @Inject constructor(private val api: Api) {
    fun getDoggoImages(): Flow<PagingData<DoggoImageModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20 // PAGING_LIMIT
            ),
            pagingSourceFactory = {
                BaseMediator(
                    apiFunc = { page, loadSize ->
                        api.getDoggoImages(page, loadSize).getOrNull() ?: listOf()
                    }
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }
}