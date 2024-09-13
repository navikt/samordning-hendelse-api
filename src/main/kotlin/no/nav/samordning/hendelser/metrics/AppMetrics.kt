package no.nav.samordning.hendelser.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.nav.samordning.hendelser.database.HendelseService
import no.nav.samordning.hendelser.ytelse.service.YtelseService
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.*

@Component
class AppMetrics(private val registry: MeterRegistry,
    private val hendelseService: HendelseService,
    private val ytelseService: YtelseService) {

    private val log = getLogger(javaClass)

    private var totalAntallHendelser: Number = 0
    private var totalAntallHendelserTpYtelser: Number = 0

    private val hendelserLestCounterList = HashMap<String, Counter>()
    private val hendelserTpYtelserLestCounterList = HashMap<String, Counter>()

    init {
        Gauge.builder("samordning_hendelser_total", ::totalAntallHendelser).register(registry)
        Gauge.builder("samordning_hendelser_tp_ytelser_total", ::totalAntallHendelserTpYtelser).register(registry)
    }

    @Bean
    fun totalHendelserCount() {
        scheduleTask { totalAntallHendelser = hendelseService.totalHendelser }
    }

    @Bean
    fun totalHendelserTpYtelserCount() {
        scheduleTask { totalAntallHendelserTpYtelser = ytelseService.totalHendelsertpYtelser }
    }

    private fun scheduleTask(task: TimerTask.() -> Unit) {
        val counterTask = object : TimerTask() {
            override fun run() {
                task()
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

    fun incHendelserTpYtelserLest(tpnr: String, antall: Double) {
        try {
            hendelserTpYtelserLestCounterList.getOrPut(tpnr) {
                Counter.builder("samordning_hendelser_tp_ytelser_lest")
                    .tag("tpnr", tpnr).register(registry)
            }.increment(antall)
        } catch (e: NullPointerException) {
            log.info("No counter for tpnr: $tpnr")
        }
    }
}