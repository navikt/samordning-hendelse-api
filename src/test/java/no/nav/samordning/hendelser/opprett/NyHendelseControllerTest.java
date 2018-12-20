package no.nav.samordning.hendelser.opprett;


import no.nav.samordning.hendelser.hendelse.Database;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = NyHendelseControllerTest.Initializer.class)
@WebMvcTest(value = NyHendelseController.class, secure = false)
public class NyHendelseControllerTest {

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
        var req = "{\"ytelsesType\":null,\"identifikator\":null,\"vedtakId\":\"1234\",\"fom\":null,\"tom\":null}";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/hendelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req)
        ).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
