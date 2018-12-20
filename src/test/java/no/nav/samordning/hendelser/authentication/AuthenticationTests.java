package no.nav.samordning.hendelser.authentication;

import io.jsonwebtoken.*;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

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
    public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("samordninghendelser");

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
    public void test_correct_credentials_authenticated() throws Exception {
        Map<String, Key> keys = load_rsa_keys();
        PrivateKey privateKey = (PrivateKey) keys.get("private");

        var token = Jwts.builder()
                .setId("TestId")
                .signWith(SignatureAlgorithm.RS512, privateKey)
                .compact();

        this.mockMvc.perform(get("/hendelser").header("Authorization", token)
                .param("side", "1")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void test_wrong_credentials_unauthenticated() throws Exception {
        Map<String, Key> keys = generate_rsa_keys();
        PrivateKey privateKey = (PrivateKey) keys.get("private");

        var token = Jwts.builder()
                .setId("TestId")
                .signWith(SignatureAlgorithm.RS512, privateKey)
                .compact();

        this.mockMvc.perform(get("/hendelser").header("Authorization", token))
                .andDo(print()).andExpect(status().isUnauthorized());
    }

    private Map<String, Key> load_rsa_keys() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Map<String, Key> keys = new HashMap<>();

        byte[] keyBytes = Files.readAllBytes(Path.of("src/test/resources/test_id_rsa"));
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        keys.put("private", keyFactory.generatePrivate(privateKeySpec));

        keyBytes = Files.readAllBytes(Path.of("src/test/resources/test_id_rsa.pub"));
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);
        keys.put("public", keyFactory.generatePublic(publicKeySpec));

        return keys;
    }

    private Map<String, Key> generate_rsa_keys() throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(1024);

        KeyPair keyPair = keyGenerator.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        Map<String, Key> keys = new HashMap<>();
        keys.put("private", privateKey);
        keys.put("public", publicKey);

        return keys;
    }
}
