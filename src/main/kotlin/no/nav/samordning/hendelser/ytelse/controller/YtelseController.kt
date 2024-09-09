package no.nav.samordning.hendelser.ytelse.controller

import no.nav.samordning.hendelser.metrics.AppMetrics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class YtelseController {


    @Autowired
    private lateinit var metrics: AppMetrics

    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String
}