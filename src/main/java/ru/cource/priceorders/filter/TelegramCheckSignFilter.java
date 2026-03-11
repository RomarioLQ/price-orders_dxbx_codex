package ru.cource.priceorders.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.MessageDigest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class TelegramCheckSignFilter extends OncePerRequestFilter {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String WEB_APP_DATA_CONSTANT = "WebAppData";
    private static final String INIT_DATA_HEADER = "X-Telegram-Init-Data";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TMA_PREFIX = "tma ";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.init-data.max-age-seconds:86400}")
    private long maxInitDataAgeSeconds;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean skip = "OPTIONS".equalsIgnoreCase(request.getMethod())
                || "/front/price-orders/v1/system-users/whitelist-auth".equals(uri)
                || uri == null
                || !uri.startsWith("/front/price-orders/v1/");
        if (skip) {
            log.debug("TelegramCheckSignFilter skipped. method={}, uri={}", request.getMethod(), uri);
        } else {
            log.debug("TelegramCheckSignFilter enabled. method={}, uri={}", request.getMethod(), uri);
        }
        return skip;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        logRequestDiagnostics(request, "started");
        log.info(
                "TelegramCheckSignFilter started. method={}, uri={}, remoteAddr={}, maxInitDataAgeSeconds={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                maxInitDataAgeSeconds
        );

        if (botToken == null || botToken.isBlank()) {
            log.error("TelegramCheckSignFilter reject: telegram.bot-token is empty");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Telegram bot token is not configured");
            return;
        }

        String initData = extractInitData(request);
        if (initData == null || initData.isBlank()) {
            logRequestDiagnostics(request, "missing_init_data");
            log.warn("TelegramCheckSignFilter reject: missing Telegram initData. uri={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Telegram initData");
            return;
        }

        if (!validateTelegramInitData(initData, botToken, maxInitDataAgeSeconds)) {
            logRequestDiagnostics(request, "invalid_init_data");
            log.warn(
                    "TelegramCheckSignFilter reject: invalid Telegram initData. uri={}, initDataLength={}",
                    request.getRequestURI(),
                    initData.length()
            );
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Telegram initData");
            return;
        }

        TelegramUserData telegramUserData = extractTelegramUserData(initData);
        if (telegramUserData == null) {
            logRequestDiagnostics(request, "invalid_user_payload");
            log.warn("TelegramCheckSignFilter reject: Telegram user id is missing in initData. uri={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Telegram initData user payload");
            return;
        }

        request.setAttribute(TelegramRequestAttributes.TELEGRAM_USER_DATA, telegramUserData);
        logRequestDiagnostics(request, "passed");
        log.info("TelegramCheckSignFilter passed. method={}, uri={}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }

    private static void logRequestDiagnostics(HttpServletRequest request, String stage) {
        String initHeader = request.getHeader(INIT_DATA_HEADER);
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        String initQuery = request.getParameter("initData");

        int initHeaderLength = initHeader == null ? 0 : initHeader.length();
        int initQueryLength = initQuery == null ? 0 : initQuery.length();
        boolean hasTmaAuth = authorization != null
                && authorization.regionMatches(true, 0, AUTHORIZATION_TMA_PREFIX, 0, AUTHORIZATION_TMA_PREFIX.length());
        int authLength = authorization == null ? 0 : authorization.length();
        int queryStringLength = request.getQueryString() == null ? 0 : request.getQueryString().length();

        log.info(
                "TelegramCheckSignFilter diagnostics. stage={}, method={}, uri={}, origin={}, referer={}, secFetchMode={}, secFetchSite={}, acrMethod={}, acrHeaders={}, hasInitHeader={}, initHeaderLength={}, hasInitQuery={}, initQueryLength={}, hasAuthorization={}, hasTmaAuthorization={}, authorizationLength={}, queryStringLength={}",
                stage,
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("Origin"),
                request.getHeader("Referer"),
                request.getHeader("Sec-Fetch-Mode"),
                request.getHeader("Sec-Fetch-Site"),
                request.getHeader("Access-Control-Request-Method"),
                request.getHeader("Access-Control-Request-Headers"),
                initHeader != null && !initHeader.isBlank(),
                initHeaderLength,
                initQuery != null && !initQuery.isBlank(),
                initQueryLength,
                authorization != null && !authorization.isBlank(),
                hasTmaAuth,
                authLength,
                queryStringLength
        );
    }

    private static TelegramUserData extractTelegramUserData(String initData) {
        try {
            Map<String, String> params = parseQueryString(initData);
            String userJson = params.get("user");
            if (userJson == null || userJson.isBlank()) {
                return null;
            }
            JsonNode userNode = OBJECT_MAPPER.readTree(userJson);
            JsonNode idNode = userNode.get("id");
            if (idNode == null || idNode.asText().isBlank()) {
                return null;
            }
            return new TelegramUserData(initData, idNode.asText());
        } catch (Exception ex) {
            log.warn("Telegram user payload parsing failed", ex);
            return null;
        }
    }

    private static String extractInitData(HttpServletRequest request) {
        String fromHeader = request.getHeader(INIT_DATA_HEADER);
        if (fromHeader != null && !fromHeader.isBlank()) {
            log.debug("Telegram initData source: {}", INIT_DATA_HEADER);
            return fromHeader.trim();
        }

        String fromQuery = request.getParameter("initData");
        if (fromQuery != null && !fromQuery.isBlank()) {
            log.debug("Telegram initData source: query parameter initData");
            return fromQuery.trim();
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization != null && authorization.regionMatches(true, 0, AUTHORIZATION_TMA_PREFIX, 0, AUTHORIZATION_TMA_PREFIX.length())) {
            log.debug("Telegram initData source: Authorization header with tma prefix");
            return authorization.substring(AUTHORIZATION_TMA_PREFIX.length()).trim();
        }

        log.debug("Telegram initData source: not found");
        return null;
    }

    private static boolean validateTelegramInitData(String initData, String botToken, long maxInitDataAgeSeconds) {
        try {
            log.debug("Validating Telegram initData. initDataLength={}", initData.length());
            Map<String, String> params = parseQueryString(initData);
            String receivedHash = params.get("hash");
            if (receivedHash == null || receivedHash.isEmpty()) {
                log.warn("Telegram initData validation failed: missing hash");
                return false;
            }

            if (!isAuthDateValid(params.get("auth_date"), maxInitDataAgeSeconds)) {
                log.warn(
                        "Telegram initData validation failed: auth_date is invalid. auth_date={}, maxAgeSeconds={}",
                        params.get("auth_date"),
                        maxInitDataAgeSeconds
                );
                return false;
            }

            params.remove("hash");
            String dataCheckString = createDataCheckString(params);

            byte[] secretKey = computeHmacSha256(
                    WEB_APP_DATA_CONSTANT.getBytes(StandardCharsets.UTF_8),
                    botToken.getBytes(StandardCharsets.UTF_8)
            );

            byte[] signature = computeHmacSha256(
                    secretKey,
                    dataCheckString.getBytes(StandardCharsets.UTF_8)
            );

            byte[] receivedHashBytes = hexToBytes(receivedHash);
            boolean valid = receivedHashBytes != null && MessageDigest.isEqual(signature, receivedHashBytes);
            log.debug(
                    "Telegram initData signature compared. valid={}, paramsCount={}, dataCheckStringLength={}",
                    valid,
                    params.size(),
                    dataCheckString.length()
            );
            return valid;

        } catch (Exception e) {
            log.error("Telegram initData validation failed with exception", e);
            return false;
        }
    }

    private static boolean isAuthDateValid(String authDateRaw, long maxInitDataAgeSeconds) {
        if (authDateRaw == null || authDateRaw.isBlank()) {
            log.debug("Telegram auth_date validation failed: auth_date is blank");
            return false;
        }
        try {
            long authDate = Long.parseLong(authDateRaw);
            long now = Instant.now().getEpochSecond();
            if (authDate > now + 30) {
                log.warn("Telegram auth_date validation failed: auth_date is in future. authDate={}, now={}", authDate, now);
                return false;
            }
            long ageSeconds = now - authDate;
            boolean valid = ageSeconds <= maxInitDataAgeSeconds;
            if (!valid) {
                log.warn(
                        "Telegram auth_date validation failed: expired initData. ageSeconds={}, maxAgeSeconds={}",
                        ageSeconds,
                        maxInitDataAgeSeconds
                );
            }
            return valid;
        } catch (NumberFormatException ex) {
            log.warn("Telegram auth_date validation failed: invalid number '{}'", authDateRaw);
            return false;
        }
    }

    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();

        if (queryString == null || queryString.isEmpty()) {
            return params;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx >= 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);

                key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);

                params.put(key, value);
            } else {
                log.debug("Telegram initData parse skipped malformed pair: {}", pair);
            }
        }

        log.debug("Telegram initData parsed. paramsCount={}, keys={}", params.size(), params.keySet());
        return params;
    }

    private static String createDataCheckString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            sb.append(key).append("=").append(value);

            if (i < keys.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private static byte[] computeHmacSha256(byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM);
        mac.init(secretKeySpec);
        return mac.doFinal(data);
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return null;
        }
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                return null;
            }
            out[i / 2] = (byte) ((high << 4) + low);
        }
        return out;
    }
}
