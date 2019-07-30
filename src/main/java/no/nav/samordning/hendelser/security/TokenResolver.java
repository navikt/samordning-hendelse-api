package no.nav.samordning.hendelser.security;

import com.auth0.jwt.JWT;
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import no.nav.samordning.hendelser.metrics.AppMetrics;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenResolver implements BearerTokenResolver {

    private static final Logger logger = LoggerFactory.getLogger(TokenResolver.class);

    @Autowired
    private TpregisteretConsumer tpregisteretConsumer;

    @Autowired
    private AppMetrics metrics;

    @Value("${service.user}")
    private String srvUser;

    @Value("${service.user.iss}")
    private String srvUserIss;

    @Override
    public String resolve(HttpServletRequest request) {
        var token = new DefaultBearerTokenResolver().resolve(request);

        if (token == null) {
            logger.debug("Rejected invalid Bearer token");
            metrics.rejectRequest();
            return token;
        }

        var claims = getClaims(token);
        var tpnr = request.getParameter("tpnr");

        if (claims.containsKey("azp") && claims.containsKey("iss")) {
            if (claims.get("azp").toString().equals(srvUser) &&
                claims.get("iss").toString().equals(srvUserIss)) {
                logger.debug("Accepted valid service token from " + claims.get("azp").toString());
                metrics.acceptRequest();
                return token;
            }
        }

        var requiredParams = List.of("client_id", "client_orgno", "iss", "scope");
        for (var param : requiredParams) {
            if (!claims.containsKey(param)) throw tokenError("Missing parameter: " + param);
        }

        if (tpregisteretConsumer.validateOrganisation(claims.get("client_orgno").toString(), tpnr)) {
            metrics.acceptRequest();
            return token;
        }

        logger.debug("Rejected token claims from " + claims.get("client_id"));
        metrics.rejectRequest();
        return null;
    }

    private Map<String, Object> getClaims(String token) {
        var decoded = JWT.decode(token);
        var payload = new String(Base64.getUrlDecoder().decode(decoded.getPayload()), StandardCharsets.UTF_8);
        var json = new JSONObject(payload);

        var map = new HashMap<String, Object>();
        var keys = json.keys();

        while (keys.hasNext()) {
            var key = keys.next();
            map.put(key, json.get(key));
        }

        return map;
    }

    private OAuth2AuthenticationException tokenError(String message) {
        var error = new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, message,
            "https://tools.ietf.org/html/rfc6750#section-3.1");
        return new OAuth2AuthenticationException(error);
    }
}
