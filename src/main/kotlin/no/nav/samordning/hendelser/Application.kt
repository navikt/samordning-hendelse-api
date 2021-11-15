package no.nav.samordning.hendelser

import no.nav.pensjonsamhandling.maskinporten.validation.annotation.EnableMaskinportenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableMaskinportenValidation
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
