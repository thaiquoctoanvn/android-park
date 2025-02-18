package io.edenx.androidplayground.data.repo

import io.edenx.androidplayground.data.*
import io.edenx.androidplayground.data.model.ChatRequest
import io.edenx.androidplayground.data.model.ChatResponse
import io.edenx.androidplayground.data.model.CompletionRequest
import io.edenx.androidplayground.data.model.CompletionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class NetworkRepo(private val api: AuthApi) {
    fun chatWithGpt(request: ChatRequest): Flow<Result<ChatResponse>> {
        return flow {
            emit(api.chatWithGpt(request))
        }.flowOn(Dispatchers.IO)
    }

    fun askCompletion(request: CompletionRequest): Flow<Result<CompletionResponse>> {
        return flow {
            val result = api.askCompletion(request)
            if (result.isFailure) throw result.exceptionOrNull() ?: IOException()
            else emit(result)
        }.flowOn(Dispatchers.IO)
    }
}