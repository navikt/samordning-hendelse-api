package no.nav.samordning.hendelser.authentication;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = AuthenticationTests.Initializer.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class AuthenticationTests {

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

    private final String jwtToken = "eyJraWQiOiJtcVQ1QTNMT1NJSGJwS3JzY2IzRUhHcnItV0lGUmZMZGFxWl81SjlHUjlzIiwiYWxnIjoiUlMyNTYifQ.eyJhdWQiOiJvaWRjX25hdl9wb3J0YWxfdGVzdGtvbnN1bWVudCIsInNjb3BlIjoibmF2OnRlc3RhcGkyIG5hdjp0ZXN0YXBpIiwiaXNzIjoiaHR0cHM6XC9cL29pZGMtdmVyMi5kaWZpLm5vXC9pZHBvcnRlbi1vaWRjLXByb3ZpZGVyXC8iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiZXhwIjoxNTQ3NzI4NzQ1LCJpYXQiOjE1NDc3MjgzODUsImNsaWVudF9vcmdubyI6Ijg4OTY0MDc4MiIsImp0aSI6IlJsOEstMU9qeUFuVkN6VjJ4MGxpdXBiMW41bFJpWlZ6aXVkc1YtQ1doNWM9In0.PpMTcpuPzPVMK7U72Tcp_WKxAyRy6v-fAvo6JbtRkuk55Va6hs2LWrDQlqirZCuXa_NUNGb3DHi0JzLnKWA51VPCndzCbed9J35kzq5OggnNJu7kL2KkxsV2CF6PcB9Fw6dnGtHu1jPbT0oMwRCLyuRfMpJc2WqB0ZvzFpgelhDZLbr6nobCtVCN4aYxzEgCoqLN3OypUnakBPIkedZ9JyaLefcSJUIINcL4yVg8_8_1R5DtQViXmL_DQvOXr8xkxxQKUzOcLSaEFUkuPkLslBBLTwRDp60uW3XWIZpReuub8mShLVoeOsYczLiL8EPhoeXtkwUqnSv1cjGacvVy2A";

//    @Test
//    public void test_correct_credentials_authenticated() throws Exception {
//
//        this.mockMvc.perform(get("/hendelser").header("Authorization", jwtToken)
//                .param("side", "1")).andDo(print()).andExpect(status().isOk());
//    }

    @Test
    public void test_wrong_credentials_unauthenticated() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", jwtToken))
                .andDo(print()).andExpect(status().isUnauthorized());
    }
}
