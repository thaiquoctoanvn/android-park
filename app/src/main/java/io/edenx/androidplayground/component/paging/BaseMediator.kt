package io.edenx.androidplayground.component.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class BaseMediator<T: Any>(
    private val apiFunc: suspend (Int, Int) -> List<T>?
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
            apiFunc(page, params.loadSize)?.let {
                LoadResult.Page(
                    data = it,
                    prevKey = if (page == 1) null else page - 1, // Only paging forward.
                    nextKey = page + 1
                )
            } ?: throw Exception()
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}