package no.nav.samordning.hendelser.hendelse;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.time.LocalDate;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = DatabaseTests.Initializer.class)
public class DatabaseTests {

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
    private Database db;

    @Test
    public void fetchTest() {

        Hendelse expected = new Hendelse();
        expected.setYtelsesType("AAP");
        expected.setIdentifikator("12345678901");
        expected.setVedtakId("ABC123");
        expected.setFom(LocalDate.of(2020, 01, 01));

        Hendelse result = db.fetch(0, 20, "AAP", "2030-01-01","2031-02-02").get(0);

        assertThat(expected, samePropertyValuesAs(result));

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void fetchTestWithInvalidYtelsesType() {

        Hendelse expected = new Hendelse();
        expected.setYtelsesType("Alderstrygd");
        expected.setIdentifikator("12345678901");
        expected.setVedtakId("ABC123");
        expected.setFom(LocalDate.of(2020, 01, 01));

        Hendelse result = db.fetch(0, 0, "Alderstrygd", "2030-01-01", "2031-02-02").get(0);

        assertThat(expected, samePropertyValuesAs(result));
    }
}
