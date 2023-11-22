package no.nav.samordning.hendelser

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
