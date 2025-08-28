package no.nav.samordning.hendelser.common.metrics

import com.ninjasquad.springmockk.SpykBean
import io.mockk.verify
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
internal class AppMetricsTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @SpykBean
    private lateinit var appMetrics: AppMetrics

    @Test
    //@WithMockUser(roles = [ROLE_SAMHANDLER])
    fun hendelser_lest_metrics_are_incremented() {
        mockMvc.perform(get("/hendelser?tpnr=1000"))
                .andExpect(status().isOk)

        verify(exactly = 1) { appMetrics.incHendelserLest(eq("1000"), any()) }
    }

}
