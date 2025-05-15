package io.quarkiverse.langchain4j.sample.chatbot

import dev.langchain4j.internal.Markers.SENSITIVE
import io.quarkus.websockets.next.OnOpen
import io.quarkus.websockets.next.OnTextMessage
import io.quarkus.websockets.next.WebSocket
import io.quarkus.websockets.next.WebSocketConnection
import jakarta.enterprise.context.SessionScoped
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ChatBotWebSocket::class.java)

@WebSocket(path = "/chatbot")
@Suppress("unused")
@SessionScoped
class ChatBotWebSocket(private val assistantService: AssistantService) {

    @OnOpen
    fun onOpen(connection: WebSocketConnection): Answer {
        return greeting
    }

    @OnTextMessage
    suspend fun onMessage(
        payload: String,
        connection: WebSocketConnection
    ): Answer {
        try {
            val request = Json.decodeFromString<Request>(payload)
            return assistantService.askQuestion(
                memoryId = request.sessionId,
                question = request.message
            )

        } catch (e: Exception) {
            logger.error(SENSITIVE, "Error while processing message: {}", payload, e)
            return errorAnswer
        }
    }

    @Serializable
    data class Request(val message: String, val sessionId: String)
}

