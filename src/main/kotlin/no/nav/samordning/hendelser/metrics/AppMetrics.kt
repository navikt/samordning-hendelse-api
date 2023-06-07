package no.nav.samordning.hendelser.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.nav.samordning.hendelser.database.HendelseService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.*

@Component
class AppMetrics(private val registry: MeterRegistry) {

    private val log = getLogger(javaClass)

    @Autowired
    private lateinit var hendelseService: HendelseService

    private var totalAntallHendelser: Number = 0

    private val hendelserLestCounterList = HashMap<String, Counter>()

    init {
        Gauge.builder("samordning_hendelser_total", ::totalAntallHendelser).register(registry)
    }

    @Bean
    fun totalHendelserCount() {
        val counterTask = object : TimerTask() {
            override fun run() {
                totalAntallHendelser = hendelseService.totalHendelser
            }
        }
        val timer = Timer("Timer")
        val delay = 1000L * 60
        val period = 1000L * 60
        timer.scheduleAtFixedRate(counterTask, delay, period)
    }

    fun incHendelserLest(tpnr: String, antall: Double) {
        try {
            hendelserLestCounterList.getOrPut(tpnr) {
                Counter.builder("samordning_hendelser_lest")
                        .tag("tpnr", tpnr).register(registry)
            }.increment(antall)
        } catch (e: NullPointerException) {
            log.info("No counter for tpnr: $tpnr")
        }
    }
}