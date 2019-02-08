package com.marei.sagameshimikkelinebot

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestClientException
import org.springframework.web.client.HttpStatusCodeException
import com.marei.sagameshimikkelinebot.model.Cuisine
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson

//import com.fasterxml.jackson.module.kotlin.*


@Component
class CloudTasks @Autowired constructor(private val environment: Environment,
                                        private val restTemplateBuilder: RestTemplateBuilder) {
    companion object {
        private const val DEV_ENV = "dev"
        // TODO: 仮のURL（多分こうなるけど）
        private const val HEROKU_APP_URL = "https://sagameshimikke-line-bot.herokuapp.com"
    }

    // private val restOperations = restTemplateBuilder.rootUri("https://api-apeast.graphcms.com")
    //                                                 .build()
    private val restOperations = restTemplateBuilder.build()
    private val graphcmsUri: String? = environment.getProperty("spring.config.graphcms.uri")

    /**
     * initialDelay：アプリケーションが起動してから何秒後に実行するか（ミリ秒指定）
     * fixedDelay：何秒ごとに処理を実行するか（ミリ秒指定）
     * アプリケーション起動5秒後と20秒間隔で実行
     */
    @Scheduled(initialDelay = 5000, fixedDelay = 20000)
    fun helloWorld() {
        val query = """query publishedCuisines {
  cuisines(where: {status: PUBLISHED, restaurant: {status: PUBLISHED}}) {
    id
    name
    photo {
      fileName
      url
    }
    restaurant {
      name
      url
      place
      googleMapSrc
    }
  }
}
"""
        try {
            if (graphcmsUri is String) {
                val params = mapOf(
                        "publishedCuisines" to "operationName",
                        "query" to query,
                        "variables" to null
                )
                val res = restOperations.postForObject(graphcmsUri, params, Map::class.java)
                val cuisineList = mutableListOf<Cuisine>()
                val mapper = jacksonObjectMapper()
                if (res is Map) {
                    val data = res["data"]
                    if (data is Map<*, *>) {
                        val cuisines = data["cuisines"]
                        if (cuisines is List<*>) {
                            for (cuisine in cuisines) {
                                val gson = Gson()
                                val jsonString: String = gson.toJson(cuisine)
                                val cuisineInstance = mapper.readValue<Cuisine>(jsonString)
                                cuisineList.add(cuisineInstance)
                            }
                        }
                    }
                }
                println("cuisineListの中身： $cuisineList")
                println("\n")
            }
        } catch (e: HttpStatusCodeException) {
            println("Status Code Error: $e.getStatusCode()")
        } catch (e: RestClientException) {
            println("Unexpected Error: $e")
        }
        println("\nhelloWorld method end.\n")
    }

    /**
     * cron：cron 記法で実行時間を指定
     * zone：cron の起動時間のタイムゾーンを指定
     * 毎日00時00分00秒に実行
     */
    // @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Tokyo")
    // fun helloWorld2() {
    // }

    /**
     * 20分毎にアプリケーションのURLへGETアクセスする
     *
     * 無料版の Heroku だと、 Dyno に30分間アクセスが無いとアイドリング状態になってしまうため、
     * それを回避するための策略。
     */
    @Scheduled(initialDelay = 1200000, fixedDelay = 1200000)
    fun connectionHerokuApp() {
        // 開発環境の場合は処理を実行しない
        if (environment.getProperty("spring.config.name") == DEV_ENV) return

        try {
            restOperations.getForObject(HEROKU_APP_URL, String::class.java)
        } catch (e: HttpStatusCodeException) {
            println("Status Code Error: $e.getStatusCode()")
        } catch (e: RestClientException) {
            println("Unexpected Error: $e")
        }
        println("connectionHerokuApp method end.")
    }
}
