package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Database;
import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = FeedNyHendelseControllerTest.Initializer.class)
@WebMvcTest(value = FeedController.class, secure = false)
public class FeedNyHendelseControllerTest {

    @ClassRule
    public static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>("postgres")
            .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"), "/docker-entrypoint-initdb.d/");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                    "spring.datasource.username=" + postgresContainer.getUsername());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                    "spring.datasource.password=" + postgresContainer.getPassword());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Database service;

    @Test
    public void greetingShouldReturnMessageFromService() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        when(service.fetchAll(any(), any())).thenReturn(List.of(hendelse));

        this.mockMvc.perform(get("/hendelser").param("side", "1")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"hendelser\":[{\"ytelsesType\":null,\"identifikator\":null,\"vedtakId\":\"1234\",\"fom\":null,\"tom\":null}]}")));
    }


    @Test
    public void serviceShouldRequirePageParameter() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        when(service.fetchAll(any(), any())).thenReturn(List.of(hendelse));

        this.mockMvc.perform(get("/hendelser")).andDo(print()).andExpect(status().is4xxClientError());
    }


    @Test
    public void serviceShouldntAcceptTooLargeRequests() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        when(service.fetchAll(any(), any())).thenReturn(List.of(hendelse));

        this.mockMvc.perform(get("/hendelser").param("side", "1").param("antall", "10001")).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Man kan ikke be om flere enn 10000 hendelser.")));
    }

}
