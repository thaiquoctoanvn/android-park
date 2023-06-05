package io.edenx.androidpark.data.model

import com.google.gson.annotations.SerializedName

data class CompletionResponse(
    @SerializedName("choices")
    var choices: List<Choice?>? = null,
    @SerializedName("created")
    var created: Number? = null,
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("model")
    var model: String? = null,
    @SerializedName("object")
    var `object`: String? = null,
    @SerializedName("usage")
    var usage: Usage? = null
) {
    data class Choice(
        @SerializedName("finish_reason")
        var finish_reason: String? = null,
        @SerializedName("index")
        var index: Number? = null,
        @SerializedName("logprobs")
        var logprobs: Any? = null,
        @SerializedName("text")
        var text: String? = null
    )

    data class Usage(
        @SerializedName("completion_tokens")
        var completion_tokens: Number? = null,
        @SerializedName("prompt_tokens")
        var prompt_tokens: Number? = null,
        @SerializedName("total_tokens")
        var total_tokens: Number? = null
    )
}

data class CompletionRequest(
    @SerializedName("logprobs")
    var logprobs: Any? = null,
    @SerializedName("max_tokens")
    var max_tokens: Number? = 150,
    @SerializedName("model")
    var model: String? = "text-davinci-003",
    @SerializedName("n")
    var n: Number? = null,
    @SerializedName("prompt")
    var prompt: String? = null,
    @SerializedName("stop")
    var stop: String? = null,
    @SerializedName("stream")
    var stream: Boolean? = null,
    @SerializedName("temperature")
    var temperature: Number? = 0,
    @SerializedName("top_p")
    var top_p: Number? = 1.0
)