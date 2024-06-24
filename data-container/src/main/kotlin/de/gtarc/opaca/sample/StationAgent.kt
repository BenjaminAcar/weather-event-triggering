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
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.bean.CsvBindByName
import com.fasterxml.jackson.databind.ObjectMapper


class StationAgent(name: String) : AbstractContainerizedAgent(name = name) {

    data class ForecastData(
        val temp_c: Double,
        val temp_f: Double,
        val is_day: Int,
        val wind_mph: Double,
        val wind_kph: Double,
        val wind_degree: Int,
        val wind_dir: String,
        val pressure_mb: Double,
        val pressure_in: Double,
        val precip_mm: Double,
        val precip_in: Double,
        val humidity: Int,
        val cloud: Int,
        val feelslike_c: Double,
        val feelslike_f: Double,
        val windchill_c: Double,
        val windchill_f: Double,
        val heatindex_c: Double,
        val heatindex_f: Double,
        val dewpoint_c: Double,
        val dewpoint_f: Double,
        val will_it_rain: Int,
        val chance_of_rain: Int,
        val will_it_snow: Int,
        val chance_of_snow: Int,
        val vis_km: Double,
        val vis_miles: Double,
        val gust_mph: Double,
        val gust_kph: Double
    )

    private var lastMessage: Any? = null
    private var lastBroadcast: Any? = null
    private var lastPostedStream: Any? = null

    override fun preStart() {
        super.preStart()

        addAction("GatherData", emptyMap(), Parameter("string")) {
            actionGatherData()
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

    fun actionGatherData(): String {
        val filePath = "/app/data/cleaned_forecast_data.csv"
        val csvFile = File(filePath)
        val lines = csvFile.readLines()
    
        if (lines.isNotEmpty()) {
            for (i in 1 until lines.size) {
                val dataLine = lines[i].split(",")
    
                val forecastData = ForecastData(
                    temp_c = dataLine[0].toDouble(),
                    temp_f = dataLine[1].toDouble(),
                    is_day = dataLine[2].toInt(),
                    wind_mph = dataLine[4].toDouble(),
                    wind_kph = dataLine[5].toDouble(),
                    wind_degree = dataLine[6].toInt(),
                    wind_dir = dataLine[7],
                    pressure_mb = dataLine[8].toDouble(),
                    pressure_in = dataLine[9].toDouble(),
                    precip_mm = dataLine[10].toDouble(),
                    precip_in = dataLine[11].toDouble(),
                    humidity = dataLine[12].toInt(),
                    cloud = dataLine[13].toInt(),
                    feelslike_c = dataLine[14].toDouble(),
                    feelslike_f = dataLine[15].toDouble(),
                    windchill_c = dataLine[16].toDouble(),
                    windchill_f = dataLine[17].toDouble(),
                    heatindex_c = dataLine[18].toDouble(),
                    heatindex_f = dataLine[19].toDouble(),
                    dewpoint_c = dataLine[20].toDouble(),
                    dewpoint_f = dataLine[21].toDouble(),
                    will_it_rain = dataLine[22].toInt(),
                    chance_of_rain = dataLine[23].toInt(),
                    will_it_snow = dataLine[24].toInt(),
                    chance_of_snow = dataLine[25].toInt(),
                    vis_km = dataLine[26].toDouble(),
                    vis_miles = dataLine[27].toDouble(),
                    gust_mph = dataLine[28].toDouble(),
                    gust_kph = dataLine[29].toDouble()
                )
                val dataMap = mapOf(
                    "temp_c" to forecastData.temp_c,
                    "temp_f" to forecastData.temp_f,
                    "is_day" to forecastData.is_day,
                    "wind_mph" to forecastData.wind_mph,
                    "wind_kph" to forecastData.wind_kph,
                    "wind_degree" to forecastData.wind_degree,
                    "wind_dir" to forecastData.wind_dir,
                    "pressure_mb" to forecastData.pressure_mb,
                    "pressure_in" to forecastData.pressure_in,
                    "precip_mm" to forecastData.precip_mm,
                    "precip_in" to forecastData.precip_in,
                    "humidity" to forecastData.humidity,
                    "cloud" to forecastData.cloud,
                    "feelslike_c" to forecastData.feelslike_c,
                    "feelslike_f" to forecastData.feelslike_f,
                    "windchill_c" to forecastData.windchill_c,
                    "windchill_f" to forecastData.windchill_f,
                    "heatindex_c" to forecastData.heatindex_c,
                    "heatindex_f" to forecastData.heatindex_f,
                    "dewpoint_c" to forecastData.dewpoint_c,
                    "dewpoint_f" to forecastData.dewpoint_f,
                    "will_it_rain" to forecastData.will_it_rain,
                    "chance_of_rain" to forecastData.chance_of_rain,
                    "will_it_snow" to forecastData.will_it_snow,
                    "chance_of_snow" to forecastData.chance_of_snow,
                    "vis_km" to forecastData.vis_km,
                    "vis_miles" to forecastData.vis_miles,
                    "gust_mph" to forecastData.gust_mph,
                    "gust_kph" to forecastData.gust_kph
                )

                val currentTime = System.currentTimeMillis()
                logAnalytics(currentTime.toString())
                println(currentTime.toString())
                sendOutboundInvoke("AddData", "data", dataMap, String::class.java)
                runBlocking {
                    delay(1500)
                }
            }
        }
        return "Data is sent to the Data Agent"
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
