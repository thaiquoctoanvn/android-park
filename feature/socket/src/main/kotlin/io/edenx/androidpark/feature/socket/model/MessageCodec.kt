package io.edenx.androidpark.feature.socket.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * JSON wire format shared by the client and the fake server. Every frame
 * carries a "type" field that picks the message kind.
 *
 * Built field by field rather than with `gson.fromJson(Tick::class.java)`:
 * Gson bypasses Kotlin constructors, so a missing field would land as null in a
 * non-null property and crash later, far from the bad frame.
 */
object MessageCodec {

    private const val TYPE = "type"

    fun encode(message: ServerMessage): String = JsonObject().apply {
        when (message) {
            is ServerMessage.Tick -> {
                addProperty(TYPE, "tick")
                addProperty("seq", message.seq)
                addProperty("symbol", message.symbol)
                addProperty("price", message.price)
                addProperty("ts", message.ts)
            }
            is ServerMessage.Ack -> {
                addProperty(TYPE, "ack")
                addProperty("commandId", message.commandId)
            }
        }
    }.toString()

    fun decodeServerMessage(text: String): ServerMessage? = parse(text) { json ->
        when (json.string(TYPE)) {
            "tick" -> ServerMessage.Tick(
                seq = json.long("seq"),
                symbol = json.string("symbol"),
                price = json.double("price"),
                ts = json.long("ts"),
            )
            "ack" -> ServerMessage.Ack(json.string("commandId"))
            else -> null
        }
    }

    fun encode(command: Command): String = JsonObject().apply {
        addProperty(TYPE, "command")
        addProperty("id", command.id)
        addProperty("action", command.type.name.lowercase())
        add("symbols", JsonArray().apply { command.symbols.forEach(::add) })
    }.toString()

    fun decodeCommand(text: String): Command? = parse(text) { json ->
        if (json.string(TYPE) != "command") return@parse null
        val type = Command.Type.entries
            .firstOrNull { it.name.equals(json.string("action"), ignoreCase = true) }
            ?: return@parse null
        Command(
            id = json.string("id"),
            type = type,
            symbols = json.getAsJsonArray("symbols").map { it.asString },
        )
    }

    /**
     * Bad JSON, a missing field or a wrong type all come back as null instead
     * of an exception, so one malformed frame never ends the stream. The
     * caller decides whether to count it.
     */
    private inline fun <T> parse(text: String, block: (JsonObject) -> T?): T? = try {
        block(JsonParser.parseString(text).asJsonObject)
    } catch (e: RuntimeException) {
        null
    }

    // A missing field makes get() return null, so these throw NPE, which parse() catches.
    private fun JsonObject.string(name: String): String = get(name).asString
    private fun JsonObject.long(name: String): Long = get(name).asLong
    private fun JsonObject.double(name: String): Double = get(name).asDouble
}
