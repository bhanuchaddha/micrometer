package com.metakoder.micrometer

import com.github.javafaker.Faker
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.Thread.sleep
import java.time.Duration
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.annotation.PostConstruct
import kotlin.concurrent.thread
import kotlin.random.Random


fun main(args: Array<String>) {
    runApplication<MicrometerApplication>(*args)
}

data class Player(val name: String, val county: String)

@Component
class Game(val registry: MeterRegistry){
    fun start(){
        val nameFaker = Faker.instance()
        val countries = Locale.getISOCountries().map { Locale("",it).displayName }.subList(0,20)
        val players = (1..100).map { Player(name = nameFaker.name().fullName(), county = countries.random() ) }
        thread {
            while (true){
                slap(players.random(), players.random())
                sleep(ThreadLocalRandom.current().nextLong(1000, 3000))
            }
        }

        thread {
            while (true){
                slap(players.random(), players.random())
                sleep(ThreadLocalRandom.current().nextLong(1000, 3000))
            }
        }

        thread {
            while (true){
                slap(players.random(), players.random())
                sleep(ThreadLocalRandom.current().nextLong(1000, 3000))
            }
        }


    }

    fun slap(playerFrom: Player, playerTo: Player){

        val hit = ThreadLocalRandom.current().nextBoolean()
        //println("${playerFrom.name} slapped ${playerTo.name} with success $hit")
        val result = if (hit) "hit" else "miss"
        // rate of increase of total slaps
        // rate of increase of total slaps hit
        // rate of increase of total slaps miss
        registry.counter("slaps.completed",
                "result",result
        ).increment()

        if (hit){
            // top winning countries
            registry.counter("slaps.win.country",
                    "country", playerFrom.county
            ).increment()

            // individual looser
            registry.counter("slaps.win.player",
                    "player", playerFrom.name
            ).increment()

        }else{
            // top loosing countries

            registry.counter("slaps.loss.country",
                    "country", playerFrom.county
            ).increment()

            // individual looser
            registry.counter("slaps.loss.player",
                    "player", playerTo.name
            ).increment()
        }
        val timer = Timer.builder("slaps.duration")
                .minimumExpectedValue(Duration.ofMillis(1000))
                .maximumExpectedValue(Duration.ofMillis(3000))
                .register(registry)
        timer.record{
            sleep(ThreadLocalRandom.current().nextLong(1000, 3000))
        }


    }
}


@SpringBootApplication
@RestController
class MicrometerApplication(val game: Game) {

    //@PostConstruct fun onStart() = game.start()
    @GetMapping("/hello")
    fun greeting(@RequestParam secondWord: String) = "Hello $secondWord"
}
