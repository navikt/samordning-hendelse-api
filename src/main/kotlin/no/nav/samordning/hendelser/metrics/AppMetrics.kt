package no.nav.samordning.hendelser.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.nav.samordning.hendelser.database.Database
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.*

@Component
class AppMetrics(
    private val registry: MeterRegistry,
    private val database: Database
) {


    private var totalAntallHendelser: Number = 0

    @get:Bean
    val hendelserLestCounterMap = HashMap<String, Counter>()

    init {
        Gauge.builder("samordning_hendelser_total", ::totalAntallHendelser).register(registry)
    }

    @Bean
    fun totalHendelserCount() {
        val counterTask = object : TimerTask() {
            override fun run() {
                totalAntallHendelser = database.totalHendelser!!.toInt()
            }
        }
        val timer = Timer("Timer")
        val delay = 1000L * 60
        val period = 1000L * 60
        timer.scheduleAtFixedRate(counterTask, delay, period)
    }

    fun incHendelserLest(tpnr: String, antall: Double) {
        try {
            hendelserLestCounterMap.getOrPut(tpnr) {
                Counter.builder("samordning_hendelser_lest")
                    .tag("tpnr", tpnr).register(registry)
            }.increment(antall)
        } catch (e: NullPointerException) {
            LOG.info("No counter for tpnr: $tpnr")
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(AppMetrics::class.java)
    }
}