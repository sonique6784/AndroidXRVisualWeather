package fr.sonique.mygeminiapplication


// { "latitude":48.86, "longitude":2.3599997, "generationtime_ms":0.08690357208251953, "utc_offset_seconds":0, "timezone":"GMT", "timezone_abbreviation":"GMT", "elevation":36.0, "current_units":{ "time":"iso8601", "interval":"seconds", "weather_code":"wmo code", "temperature_2m":"°C" }, "current":{ "time":"2025-05-05T10:45", "interval":900, "weather_code":3, "temperature_2m":13.3 }, "daily_units":{ "time":"iso8601", "weather_code":"wmo code", "temperature_2m_min":"°C", "temperature_2m_max":"°C" }, "daily":{ "time":["2025-05-05", "2025-05-06", "2025-05-07", "2025-05-08", "2025-05-09", "2025-05-10", "2025-05-11"], "weather_code":[3, 61, 3, 3, 3, 3, 3], "temperature_2m_min":[6.4, 6.5, 9.7, 8.1, 9.1, 7.6, 9.7], "temperature_2m_max":[14.3, 15.6, 16.7, 16.4, 16.6, 20.6, 21.9] } }


//{
//    "latitude": 48.86,
//    "longitude": 2.3599997,
//    "generationtime_ms": 0.08690357208251953,
//    "utc_offset_seconds": 0,
//    "timezone": "GMT",
//    "timezone_abbreviation": "GMT",
//    "elevation": 36,
//    "current_units": {
//        "time": "iso8601",
//        "interval": "seconds",
//        "weather_code": "wmo code",
//        "temperature_2m": "°C"
//    },
//    "current": {
//        "time": "2025-05-05T10:45",
//        "interval": 900,
//        "weather_code": 3,
//        "temperature_2m": 13.3
//    },
//    "daily_units": {
//        "time": "iso8601",
//        "weather_code": "wmo code",
//        "temperature_2m_min": "°C",
//        "temperature_2m_max": "°C"
//    },
//    "daily": {
//        "time": [
//            "2025-05-05",
//            "2025-05-06",
//            "2025-05-07",
//            "2025-05-08",
//            "2025-05-09",
//            "2025-05-10",
//            "2025-05-11"
//        ],
//        "weather_code": [
//            3,
//            61,
//            3,
//            3,
//            3,
//            3,
//            3
//        ],
//        "temperature_2m_min": [
//            6.4,
//            6.5,
//            9.7,
//            8.1,
//            9.1,
//            7.6,
//            9.7
//        ],
//        "temperature_2m_max": [
//            14.3,
//            15.6,
//            16.7,
//            16.4,
//            16.6,
//            20.6,
//            21.9
//        ]
//    }
//}

enum class WeatherFor3dModel {
    SUNNY,
    CLOUDY,
    RAINING,
    STORM,
    SNOWY,
    FOGGY
}

fun getSimpleWeather3dType(code: Double) : WeatherFor3dModel {
    return when (code.toInt()) {
        0, 1 -> WeatherFor3dModel.SUNNY
        2, 3 -> WeatherFor3dModel.CLOUDY
        45, 48 -> WeatherFor3dModel.FOGGY
        51, 53, 55, 56, 57, 61, 63, 80, 82, 81 -> WeatherFor3dModel.RAINING
        65, 66, 67, 95, 96, 99 -> WeatherFor3dModel.STORM
        71, 73, 75, 77, 85, 86 -> WeatherFor3dModel.SNOWY
        else -> WeatherFor3dModel.SUNNY
    }
}


fun getWeatherName(code: Double) : String {
    return when (code.toInt()) {
        0 -> "clear Sky"
        1 -> "mainly Clear"
        2 -> "partly Cloudy"
        3 -> "overcast"
        45 -> "fog"
        48 -> "depositing Rime Fog"
        51 -> "light Drizzle"
        53 -> "moderate Drizzle"
        55 -> "dense Drizzle"
        56 -> "light Freezing Drizzle"
        57 -> "moderate Or Dense Freezing Drizzle"
        61 -> "light Rain"
        63 -> "moderate Rain"
        65 -> "heavy Rain"
        66 -> "light Freezing Rain"
        67 -> "moderate Or Heavy Freezing Rain"
        71 -> "slight Snowfall"
        73 -> "moderate Snowfall"
        75 -> "heavy Snowfall"
        77 -> "snow Grains"
        80 -> "slight Rain Showers"
        81 -> "moderate Rain Showers"
        82 -> "heavy Rain Showers"
        85 -> "slight Snow Showers"
        86 -> "heavy Snow Showers"
        95 -> "thunderstorm Slight Or Moderate"
        96 -> "thunderstorm Strong"
        99 -> "thunderstorm Heavy"
        else -> "sunny"
    }
}


data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double = 0.0,
    val utc_offset_seconds: Int = 0,
    val timezone: String,
    val timezone_abbreviation: String = "",
//    val elevation: Double,
//    val current_units: CurrentUnits,
    val current: Current? = null,
//    val hourly_units: HourlyUnits,
//    val hourly: Hourly,
    //val daily: Daily? = null,
)

data class Daily(
    val time: Array<String> = emptyArray(),
    val weather_code: Array<Int>? = emptyArray(),
    val temperature_2m_max: Double = -1.0,
    val temperature2mMax: Double = -1.0
)

data class CurrentUnits(
    val time: String,
    val interval: String,
    val temperature_2m: String,
    val wind_speed_10m: String
)

data class Current(
    val time: String,
    val interval: Int,
    val weather_code: Double = -1.0,
    val temperature_2m: Double,
    val wind_speed_10m: Double
) {
    fun getWeatherName(): String {
        return getWeatherName(weather_code)
    }
    fun getSimpleWeatherName(): WeatherFor3dModel {
        return getSimpleWeather3dType(weather_code)
    }
}

data class HourlyUnits(
    val time: String,
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val wind_speed_10m: String
)

data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Int>,
    val wind_speed_10m: List<Double>
)