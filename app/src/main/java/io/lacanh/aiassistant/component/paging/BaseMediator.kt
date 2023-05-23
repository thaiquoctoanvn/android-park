package io.lacanh.aiassistant.component.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class BaseMediator<T: Any>(
    private val apiFunc: suspend (Int, Int) -> List<T>
) : PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val page = params.key ?: 1
            LoadResult.Page(
                data = apiFunc(page, params.loadSize),
                prevKey = if (page == 1) null else page - 1, // Only paging forward.
                nextKey = page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}