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

import java.time.LocalDate;
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
@ContextConfiguration(initializers = FeedNyHendelseControllerTest.Initializer.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
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

    @Test
    public void greetingShouldReturnMessageFromService() throws Exception {
        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "1"))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }


    @Test
    public void greetingShouldReturnMessageFromServiceWithFirstRecord() throws Exception {
        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "1")
                .param("antall", "1")
                .param("ytelsesType", "AAP"))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].fom").value("2020-01-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].ytelsesType").value("AAP"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].identifikator").value("12345678901"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[0].vedtakId").value("ABC123"));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithSizeCheck() throws Exception {
        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "0")
                .param("antall", "1")
                .param("ytelsesType", "AAP"))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser", hasSize(20)));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithBasicFilter() throws Exception {

        List<String> excpected = new ArrayList<>();

        for(int i=0; i<3; i++) {
            excpected.add("2040-01-01");
        }

        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "2")
                .param("antall", "1")
                .param("ytelsesType", "AAP")
                .param("fom", "2020-01-01")
                .param("tom", "2070-01-01"))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[?(@.fom=='2040-01-01')].fom").value(excpected));
    }

    @Test
    public void greetingShouldReturnMessageFromServiceWithFromToFilter() throws Exception {
        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "2")
                .param("antall", "1")
                .param("ytelsesType", "AAP"))
                .andDo(print())
                .andExpect(status()
                .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[?(@.fom>'2020-01-01' && @.fom<'2050-01-01')].fom").value(containsInAnyOrder("2030-01-01", "2040-01-01",
                        "2030-01-01", "2040-01-01",
                        "2030-01-01", "2040-01-01"
                )));
    }

    @Test(expected = AssertionError.class)
    public void greetingShouldReturnMessageFromServiceWithInvalidParam() throws Exception {

        List<String> excpected = new ArrayList<>();

        for(int i=0; i<25; i++) {
            excpected.add("2040-01-01");
        }

        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "3")
                .param("antall", "1")
                .param("ytelsesType", "Trygd"))
                .andDo(print())
                .andExpect(status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[?(@.fom=='2100-01-01')].fom").value(excpected));
    }

    @Test(expected = AssertionError.class)
    public void greetingShouldReturnMessageFromServiceWithInvalidFoms() throws Exception {

        List<String> excpected = new ArrayList<>();
        for(int i=0; i<25; i++) {
            excpected.add("2040-01-01");
        }

        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "3")
                .param("antall", "1")
                .param("ytelsesType", "Trygd"))
                .andDo(print())
                .andExpect(status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.hendelser[?(@.fom=='2100-01-01')].fom").value(excpected));
    }

    @Test
    public void serviceShouldRequirePageParameter() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        this.mockMvc.perform(get("/hendelser")).andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    public void serviceShouldntAcceptTooLargeRequests() throws Exception {
        var hendelse = new Hendelse();
        hendelse.setVedtakId("1234");

        this.mockMvc.perform(get("/hendelser")
                .with(user("srvTest"))
                .param("side", "1")
                .param("antall", "10001"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Man kan ikke be om flere enn 10000 hendelser.")));
    }
}
