package de.gtarc.opaca.sample

import de.dailab.jiacvi.behaviour.act
import de.gtarc.opaca.api.AgentContainerApi
import de.gtarc.opaca.container.AbstractContainerizedAgent
import de.gtarc.opaca.model.Message
import de.gtarc.opaca.model.Parameter
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import de.gtarc.opaca.container.OpacaServer.OpacaException
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class CarAgent(name: String) : AbstractContainerizedAgent(name = name) {

    private var lastMessage: Any? = null
    private var lastBroadcast: Any? = null
    private var lastPostedStream: Any? = null

    override fun preStart() {
        super.preStart()

        addReaction("Predict", this::actionGetPrediction)
    }

    override fun behaviour() = super.behaviour().and(act {

        on<Message> {
            log.info("ON $it")
            lastMessage = it.payload
        }

        listen<Message>("topic") {
            log.info("LISTEN $it")
            lastBroadcast = it.payload
        }
    })

    private fun actionGetPrediction() {
        runBlocking {
            delay(500)
        }
        println("Perceived changes in the prediction. Retrieving current prediction.")
        val result = sendOutboundInvoke("GetPrediction", "PredictionAgent", mapOf(), String::class.java)
        val currentTime = System.currentTimeMillis()
        logAnalytics(currentTime.toString())
        println("New prediction retrieved: $result")
    }


    private fun logAnalytics(value: String) {
        println("New event is added to the analytics csv")
        val logEntry = "$value,\n"
        File("/app/data/analytics_logs.csv").appendText(logEntry)
    }

    private fun actionGetInfo() = mapOf(
        Pair("name", name),
        Pair("lastMessage", lastMessage),
        Pair("lastBroadcast", lastBroadcast),
        Pair("lastPostedStream", lastPostedStream),
        Pair(AgentContainerApi.ENV_CONTAINER_ID, System.getenv(AgentContainerApi.ENV_CONTAINER_ID)),
        Pair(AgentContainerApi.ENV_PLATFORM_URL, System.getenv(AgentContainerApi.ENV_PLATFORM_URL)),
        Pair(AgentContainerApi.ENV_OWNER, System.getenv(AgentContainerApi.ENV_OWNER)),
        Pair(AgentContainerApi.ENV_TOKEN, System.getenv(AgentContainerApi.ENV_TOKEN))
    )
}
