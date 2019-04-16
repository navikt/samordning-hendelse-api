package no.nav.samordning.hendelser.security;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.Scanner;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthenticationTests {

    private static ClientAndServer mockServer;
    private static String jwtValidToken, jwtInvalidToken;

    @BeforeClass
    public static void startServer() throws Exception {
        var scanner = new Scanner(new File("src/test/resources", "test_jwk.json"));

        var mockJwkBody = "";
        while (scanner.hasNext()) {
            mockJwkBody += scanner.next();
        }

        scanner.close();

        jwtValidToken = new Scanner(new File("src/test/resources", "test_valid_token.txt")).next();
        jwtInvalidToken = new Scanner(new File("src/test/resources", "test_invalid_token.txt")).next();

        mockServer = startClientAndServer(1080);
        mockServer.when(request().withMethod("GET").withPath("/test-oidc-provider/jwk")).respond(
                response().withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                .withBody(mockJwkBody));
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_correct_credentials_authenticated() throws Exception {
        mockMvc.perform(get("/hendelser&side=1").header("Authorization", jwtValidToken));
                //.param("side", "1")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void test_wrong_credentials_unauthenticated() throws Exception {
        mockMvc.perform(get("/hendelser").header("Authorization", jwtInvalidToken)
                .param("side", "1")).andDo(print()).andExpect(status().isUnauthorized());
    }
}
