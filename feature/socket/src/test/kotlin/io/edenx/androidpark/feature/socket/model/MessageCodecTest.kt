package io.edenx.androidpark.feature.socket.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MessageCodecTest {

    @Test
    fun tick_roundTrips() {
        val tick = ServerMessage.Tick(seq = 42, symbol = "AAPL", price = 187.25, ts = 1_700_000_000_000)
        assertEquals(tick, MessageCodec.decodeServerMessage(MessageCodec.encode(tick)))
    }

    @Test
    fun ack_roundTrips() {
        val ack = ServerMessage.Ack("cmd-1")
        assertEquals(ack, MessageCodec.decodeServerMessage(MessageCodec.encode(ack)))
    }

    @Test
    fun command_roundTrips() {
        val command = Command("cmd-1", Command.Type.SUBSCRIBE, listOf("AAPL", "GOOG"))
        assertEquals(command, MessageCodec.decodeCommand(MessageCodec.encode(command)))
    }

    @Test
    fun command_withNoSymbols_roundTrips() {
        val command = Command("cmd-2", Command.Type.UNSUBSCRIBE, emptyList())
        assertEquals(command, MessageCodec.decodeCommand(MessageCodec.encode(command)))
    }

    @Test
    fun malformedFrames_decodeToNull() {
        listOf(
            "",                                                         // empty
            "not json",                                                 // not JSON
            "[1, 2]",                                                   // not an object
            """{"seq":1,"symbol":"AAPL","price":1.0,"ts":1}""",         // no type
            """{"type":"weather"}""",                                   // unknown type
            """{"type":"tick","symbol":"AAPL","price":1.0,"ts":1}""",   // missing seq
            """{"type":"tick","seq":1,"symbol":"AAPL","price":"x","ts":1}""", // wrong type
            """{"type":"tick","seq":1,"symbol":null,"price":1.0,"ts":1}""",   // null field
        ).forEach { frame ->
            assertNull("expected null for: $frame", MessageCodec.decodeServerMessage(frame))
        }
    }

    @Test
    fun malformedCommands_decodeToNull() {
        listOf(
            """{"type":"command","id":"1","action":"buy","symbols":[]}""", // unknown action
            """{"type":"command","id":"1","action":"subscribe"}""",        // missing symbols
            MessageCodec.encode(ServerMessage.Ack("1")),                   // not a command
        ).forEach { frame ->
            assertNull("expected null for: $frame", MessageCodec.decodeCommand(frame))
        }
    }
}
