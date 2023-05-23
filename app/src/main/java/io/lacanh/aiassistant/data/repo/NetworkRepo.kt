package io.lacanh.aiassistant.data.repo

import io.lacanh.aiassistant.data.*
import io.lacanh.aiassistant.data.model.ChatRequest
import io.lacanh.aiassistant.data.model.ChatResponse
import io.lacanh.aiassistant.data.model.CompletionRequest
import io.lacanh.aiassistant.data.model.CompletionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NetworkRepo(private val api: Api) {
    fun chatWithGpt(request: ChatRequest): Flow<Result<ChatResponse>> {
        return flow {
            emit(api.chatWithGpt(request))
        }.flowOn(Dispatchers.IO)
    }

    fun askCompletion(request: CompletionRequest): Flow<Result<CompletionResponse>> {
        return flow {
            emit(api.askCompletion(request))
        }.flowOn(Dispatchers.IO)
    }
}