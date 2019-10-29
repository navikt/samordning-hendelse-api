package no.nav.samordning.hendelser.nais

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NaisEndpoints {

    @GetMapping("/isAlive")
    fun isAlive() {
    }

    @GetMapping("/isReady")
    fun isReady() {
    }
}