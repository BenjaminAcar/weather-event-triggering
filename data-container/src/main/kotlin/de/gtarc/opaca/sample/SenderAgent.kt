package de.gtarc.opaca.sample

import de.dailab.jiacvi.behaviour.act
import de.gtarc.opaca.api.AgentContainerApi
import de.gtarc.opaca.container.AbstractContainerizedAgent
import de.gtarc.opaca.model.Message
import de.gtarc.opaca.model.Parameter
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.Charset
import de.gtarc.opaca.container.OpacaServer.OpacaException
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class SenderAgent(name: String) : AbstractContainerizedAgent(name = name) {

    private var lastMessage: Any? = null
    private var lastBroadcast: Any? = null
    private var lastPostedStream: Any? = null
    private val objectMapper = jacksonObjectMapper()

    override fun preStart() {
        super.preStart()

        addReaction("AddData", this::actionSendData)
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

    private fun actionSendData() {

        log.info("Reaction Send Data is triggered")
        // Safety. Otherwise GetData is called before AddData is completed. This causes null errors
        runBlocking {
            delay(500)
        }

        try {
            val responseJsonNode: JsonNode = sendOutboundInvoke("GetData", "data", mapOf(), JsonNode::class.java)
            log.info("Raw response JSON: $responseJsonNode")

            val response: Map<String, Any> = objectMapper.convertValue(responseJsonNode, object : TypeReference<Map<String, Any>>() {})
            log.info("Deserialized response: $response")

            val payload = mapOf(
                "temp_c" to response["temp_c"],
                "temp_f" to response["temp_f"],
                "is_day" to response["is_day"],
                "wind_mph" to response["wind_mph"],
                "wind_kph" to response["wind_kph"],
                "wind_degree" to response["wind_degree"],
                "wind_dir" to response["wind_dir"],
                "pressure_mb" to response["pressure_mb"],
                "pressure_in" to response["pressure_in"],
                "precip_mm" to response["precip_mm"],
                "precip_in" to response["precip_in"],
                "humidity" to response["humidity"],
                "cloud" to response["cloud"],
                "feelslike_c" to response["feelslike_c"],
                "feelslike_f" to response["feelslike_f"],
                "windchill_c" to response["windchill_c"],
                "windchill_f" to response["windchill_f"],
                "heatindex_c" to response["heatindex_c"],
                "heatindex_f" to response["heatindex_f"],
                "dewpoint_c" to response["dewpoint_c"],
                "dewpoint_f" to response["dewpoint_f"],
                "will_it_rain" to response["will_it_rain"],
                "chance_of_rain" to response["chance_of_rain"],
                "will_it_snow" to response["will_it_snow"],
                "chance_of_snow" to response["chance_of_snow"],
                "vis_km" to response["vis_km"],
                "vis_miles" to response["vis_miles"],
                "gust_mph" to response["gust_mph"],
                "gust_kph" to response["gust_kph"]
            )

            try {
                sendOutboundInvoke("Predict", "PredictionAgent", payload, String::class.java)
                log.info("Data sent successfully")
            } catch (e: Exception) {
                log.error("Error sending data to PredictionAgent", e)
            }
        } catch (e: Exception) {
            log.error("Error retrieving or processing data from GetData", e)
        }
    }



    private fun actionGetInfo() = mapOf(
        "name" to name,
        "lastMessage" to lastMessage,
        "lastBroadcast" to lastBroadcast,
        "lastPostedStream" to lastPostedStream,
        AgentContainerApi.ENV_CONTAINER_ID to System.getenv(AgentContainerApi.ENV_CONTAINER_ID),
        AgentContainerApi.ENV_PLATFORM_URL to System.getenv(AgentContainerApi.ENV_PLATFORM_URL),
        AgentContainerApi.ENV_OWNER to System.getenv(AgentContainerApi.ENV_OWNER),
        AgentContainerApi.ENV_TOKEN to System.getenv(AgentContainerApi.ENV_TOKEN)
    )
}