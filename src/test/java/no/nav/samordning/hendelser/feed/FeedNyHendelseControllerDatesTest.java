package no.nav.samordning.hendelser.feed;

import no.nav.samordning.hendelser.hendelse.Hendelse;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = FeedNyHendelseControllerDatesTest.Initializer.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class FeedNyHendelseControllerDatesTest {

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

    @Test
    public void greetingShouldReturnMessageFromServiceWithFraFomBeforeMinDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=1&ytelsesType=AAP&fraFom=0800-12-12")
                .with(user("srvTest")))
                //.param("side", "3")
                //.param("antall", "1")
                //.param("ytelsesType", "AAP")
                //.param("fraFom", "0800-12-12"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }
    @Test
    public void greetingShouldReturnMessageFromServiceWithFraFomAfterMaxDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=20&ytelsesType=AAP&fraFom=2101-01-01")
                .with(user("srvTest")))
                //.param("side", "3")
                //.param("antall", "1")
                //.param("ytelsesType", "AAP")
                //.param("fraFom", "2101-01-01"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithTilFomBeforeMinDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=2&antall=50&ytelsesType=AAP&tilFom=0800-12-12")
                .with(user("srvTest")))
                //.param("side", "3")
                //.param("antall", "1")
                //.param("ytelsesType", "AAP")
                //.param("tilFom", "0340-03-07"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithTilFomAfterMaxDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=1&antall=10&ytelsesType=AAP&tilFom=2200-12-30")
                .with(user("srvTest")))
                //.param("side", "1")
                //.param("antall", "10")
                //.param("ytelsesType", "AAP")
                //.param("tilFom", "9999-12-30"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithFraTomBeforeMinDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=1&ytelsesType=AAP&fraTom=0800-12-12")
                .with(user("srvTest")))
                //.param("side", "3")
                //.param("antall", "1")
                //.param("ytelsesType", "AAP")
                //.param("fraFom", "0800-12-12"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }
    @Test
    public void greetingShouldReturnMessageFromServiceWithFraTomAfterMaxDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=20&ytelsesType=AAP&fraTom=2101-01-01")
                .with(user("srvTest")))
                //.param("side", "3")
                //.param("antall", "1")
                //.param("ytelsesType", "AAP")
                //.param("fraFom", "2101-01-01"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithTilTomBeforeMinDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=1&ytelsesType=AAP&tilTom=0800-12-12")
                .with(user("srvTest")))
                //.param("side", "3")
                //.param("antall", "1")
                //.param("ytelsesType", "AAP")
                //.param("fraFom", "0800-12-12"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithTilTomAfterMaxDate() throws Exception {

        List<String> excpected = new ArrayList<>();

        this.mockMvc.perform(get("/hendelser?side=3&antall=20&ytelsesType=AAP&tilTom=2101-01-01")
                .with(user("srvTest")))
                //.param("side", "3")
                //.param("antall", "1")
                //.param("ytelsesType", "AAP")
                //.param("fraFom", "2101-01-01"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Du har oppgitt ugyldig dato")));
    }
}
