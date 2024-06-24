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


class DataAgent(name: String): AbstractContainerizedAgent(name=name) {

    private var lastMessage: Any? = null
    private var lastBroadcast: Any? = null
    private var lastPostedStream: Any? = null
    @Volatile private var currentData: Map<String, Any>? = null

    override fun preStart() {
        super.preStart()

        addAction("AddData", mapOf(
            "temp_c" to Parameter("any", true),
            "temp_f" to Parameter("any", true),
            "is_day" to Parameter("any", true),
            "wind_mph" to Parameter("any", true),
            "wind_kph" to Parameter("any", true),
            "wind_degree" to Parameter("any", true),
            "wind_dir" to Parameter("any", true),
            "pressure_mb" to Parameter("any", true),
            "pressure_in" to Parameter("any", true),
            "precip_mm" to Parameter("any", true),
            "precip_in" to Parameter("any", true),
            "humidity" to Parameter("any", true),
            "cloud" to Parameter("any", true),
            "feelslike_c" to Parameter("any", true),
            "feelslike_f" to Parameter("any", true),
            "windchill_c" to Parameter("any", true),
            "windchill_f" to Parameter("any", true),
            "heatindex_c" to Parameter("any", true),
            "heatindex_f" to Parameter("any", true),
            "dewpoint_c" to Parameter("any", true),
            "dewpoint_f" to Parameter("any", true),
            "will_it_rain" to Parameter("any", true),
            "chance_of_rain" to Parameter("any", true),
            "will_it_snow" to Parameter("any", true),
            "chance_of_snow" to Parameter("any", true),
            "vis_km" to Parameter("any", true),
            "vis_miles" to Parameter("any", true),
            "gust_mph" to Parameter("any", true),
            "gust_kph" to Parameter("any", true)
        ), Parameter("string")) { params ->
            val dataMap = mapOf(
                "temp_c" to params["temp_c"]!!,
                "temp_f" to params["temp_f"]!!,
                "is_day" to params["is_day"]!!,
                "wind_mph" to params["wind_mph"]!!,
                "wind_kph" to params["wind_kph"]!!,
                "wind_degree" to params["wind_degree"]!!,
                "wind_dir" to params["wind_dir"]!!,
                "pressure_mb" to params["pressure_mb"]!!,
                "pressure_in" to params["pressure_in"]!!,
                "precip_mm" to params["precip_mm"]!!,
                "precip_in" to params["precip_in"]!!,
                "humidity" to params["humidity"]!!,
                "cloud" to params["cloud"]!!,
                "feelslike_c" to params["feelslike_c"]!!,
                "feelslike_f" to params["feelslike_f"]!!,
                "windchill_c" to params["windchill_c"]!!,
                "windchill_f" to params["windchill_f"]!!,
                "heatindex_c" to params["heatindex_c"]!!,
                "heatindex_f" to params["heatindex_f"]!!,
                "dewpoint_c" to params["dewpoint_c"]!!,
                "dewpoint_f" to params["dewpoint_f"]!!,
                "will_it_rain" to params["will_it_rain"]!!,
                "chance_of_rain" to params["chance_of_rain"]!!,
                "will_it_snow" to params["will_it_snow"]!!,
                "chance_of_snow" to params["chance_of_snow"]!!,
                "vis_km" to params["vis_km"]!!,
                "vis_miles" to params["vis_miles"]!!,
                "gust_mph" to params["gust_mph"]!!,
                "gust_kph" to params["gust_kph"]!!
            )
            actionAddData(dataMap)
        }

        addAction("GetData", mapOf(), Parameter("map")) {
            actionGetData()
        }
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

    @Synchronized
    fun actionAddData(new_row: Map<String, Any>): String {
        println("_________________Add data called____________________")
        println("Before update: $currentData")
        currentData = new_row
        println("After update: $currentData")
        return "Data added"
    }

    @Synchronized
    private fun actionGetData(): Map<String, Any>? {
        println("_________________Get data called____________________")
        println("Current data: $currentData")
        return currentData
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
