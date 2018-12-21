package com.marei.sagameshimikkelinebot

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
// import org.springframework.boot.web.client.RestTemplate
// import org.springframework.boot.web.client.HttpClientErrorException
// import org.springframework.boot.http.HttpMethod
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestClientException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.http.HttpMethod
import java.net.URLEncoder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

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
        var result = "エラー出てない"
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
        println(query)
        // val uri = "https://api-apeast.graphcms.com/v1/cjl24acf702r001aq4d4n7qa0/master?query=query%20publishedCuisines%20%7B%0A%20%20cuisines(where%3A%20%7Bstatus%3A%20PUBLISHED%2C%20restaurant%3A%20%7Bstatus%3A%20PUBLISHED%7D%7D)%20%7B%0A%20%20%20%20id%0A%20%20%20%20name%0A%20%20%20%20photo%20%7B%0A%20%20%20%20%20%20fileName%0A%20%20%20%20%20%20url%0A%20%20%20%20%7D%0A%20%20%20%20restaurant%20%7B%0A%20%20%20%20%20%20name%0A%20%20%20%20%20%20url%0A%20%20%20%20%20%20place%0A%20%20%20%20%20%20googleMapSrc%0A%20%20%20%20%7D%0A%20%20%7D%0A%7D%0A&operationName=publishedCuisines"
        val uri = "https://api-apeast.graphcms.com/v1/cjl24acf702r001aq4d4n7qa0/master?"
        try {
            if (graphcmsUri is String) {
                val params: MultiValueMap<String, String> = LinkedMultiValueMap()
                params.add("operationName", "publishedCuisines")
                params.add("query", query)
                params.add("variables", null)
                // val res = restOperations.getForObject(uri, String::class.java)

                val params2 = mapOf(
                        "publishedCuisines" to "operationName",
                        "query" to query,
                        "variables" to null
                )
                println(params2)
                val res = restOperations.postForObject(uri, params2, Map::class.java)
                // val res = restOperations.postForObject(URLEncoder.encode(uri, "utf-8"), params, MultiValueMap::class.java)
                // val res = restOperations.exchange(uri, HttpMethod.POST, null, String::class.java)
                println(res)
            }
            // println(result)
            // if (graphcmsUri is String) restOperations.getForObject(URLEncoder.encode("https://api-apeast.graphcms.com/", "utf-8"), String::class.java)
            // if (graphcmsUri is String) restOperations.exchange("https://api-apeast.graphcms.com/v1/cjl24acf702r001aq4d4n7qa0/master", HttpMethod.POST, null, String::class.java)
        } catch (e: HttpStatusCodeException) {
            println("ステータスコードのエラー出た $e.getStatusCode()")
        } catch (e: RestClientException) {
            println("不明なエラー出た $e")
        } finally {
            // println(result)
        }
         println("終了")
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
     * 無料版のHerokuだと、Dynoに30分間アクセスが無いとアイドリング状態になってしまうため、
     * それを回避するための策略。
     */
    @Scheduled(initialDelay = 1200000, fixedDelay = 1200000)
    fun connectionHerokuApp() {
        // 開発環境の場合は処理を実行しない
        if (environment.getProperty("spring.config.name") == DEV_ENV) return

        try {
            // restTemplate.exchange(HEROKU_APP_URL, HttpMethod.GET, null, String::class.java)
            restOperations.getForObject(HEROKU_APP_URL, String::class.java)
        } catch (e: HttpStatusCodeException) {
            // 現状ページの実装はしていないため必ず404で例外発生
            println("定期アクセス ステータスコード:${e.getStatusCode()}")
        } catch (e: RestClientException) {
            println("不明なエラー出た ${e}")
        }
    }
}
