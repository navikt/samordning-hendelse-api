package no.nav.samordning.hendelser.security;

import com.auth0.jwt.JWT;
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

    private Logger logger = LoggerFactory.getLogger(TokenResolver.class);

    @Autowired
    private TpregisteretConsumer tpregisteretConsumer;

    @Value("${service.user}")
    private String srvUser;

    @Value("${service.user.iss}")
    private String srvUserIss;

    @Override
    public String resolve(HttpServletRequest request) {
        logger.info("Before: " + request.getHeader(HttpHeaders.AUTHORIZATION));
        var token = new DefaultBearerTokenResolver().resolve(request);
        logger.info("After: " + token);
        if (token == null) {
            return token;
        }

        var claims = getClaims(token);
        var tpnr = request.getParameter("tpnr");

        if (claims.containsKey("azp") && claims.containsKey("iss")) {
            if (claims.get("azp").toString().equals(srvUser) &&
                claims.get("iss").toString().equals(srvUserIss)) {
                logger.info("Valid srvuser token");
                return token;
            }
        }

        var requiredParams = List.of("client_id", "client_orgno", "iss", "scope");
        for (var param : requiredParams) {
            if (!claims.containsKey(param)) throw tokenError("Missing parameter: " + param);
        }

        if (tpregisteretConsumer.validateOrganisation(claims.get("client_orgno").toString(), tpnr)) {
            logger.info("Validated tpnr " + tpnr + " for client_id " + claims.get("client_id"));
            return token;
        } else {
            logger.info("Unvalid tpnr " + tpnr + " for client_id " + claims.get("client_id"));
        }

        logger.info("Invalid token");
        return null;
    }

    private Map<String, Object> getClaims(String token) {
        logger.info("Decoding...");
        var decoded = JWT.decode(token);
        logger.info("Decoded!");
        var payload = new String(Base64.getUrlDecoder().decode(decoded.getPayload()), StandardCharsets.UTF_8);
        var json = new JSONObject(payload);

        var map = new HashMap<String, Object>();
        var keys = json.keys();
        logger.info("Getting keys...");
        while (keys.hasNext()) {
            var key = keys.next();
            map.put(key, json.get(key));
        }
        logger.info("Loaded keys");
        return map;
    }

    private OAuth2AuthenticationException tokenError(String message) {
        var error = new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST, HttpStatus.BAD_REQUEST, message,
            "https://tools.ietf.org/html/rfc6750#section-3.1");
        return new OAuth2AuthenticationException(error);
    }
}
