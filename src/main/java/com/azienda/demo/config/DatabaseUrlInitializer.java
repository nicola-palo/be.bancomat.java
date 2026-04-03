package com.azienda.demo.config;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * In production platforms (Render/Heroku style), DB URL is often exposed as DATABASE_URL
 * using postgres://... instead of Spring's jdbc:postgresql://... format.
 */
public class DatabaseUrlInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String PROPERTY_SOURCE_NAME = "databaseUrlInitializer";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        String configuredDatasourceUrl = environment.getProperty("spring.datasource.url");
        String databaseUrl = firstNonBlank(
                configuredDatasourceUrl,
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("JDBC_DATABASE_URL"),
                environment.getProperty("DATABASE_URL"));

        String jdbcUrl = toJdbcPostgresUrl(databaseUrl);
        if (!StringUtils.hasText(jdbcUrl)) {
            return;
        }

        boolean mustOverrideUrl = !StringUtils.hasText(configuredDatasourceUrl)
                || !configuredDatasourceUrl.equals(jdbcUrl);

        if (!mustOverrideUrl
                && StringUtils.hasText(environment.getProperty("spring.datasource.username"))
                && StringUtils.hasText(environment.getProperty("spring.datasource.password"))) {
            return;
        }

        Map<String, Object> overrides = new LinkedHashMap<>();
        if (mustOverrideUrl) {
            overrides.put("spring.datasource.url", jdbcUrl);
        }

        if (!StringUtils.hasText(environment.getProperty("spring.datasource.username"))) {
            String explicitUsername = firstNonBlank(
                    environment.getProperty("SPRING_DATASOURCE_USERNAME"),
                    environment.getProperty("DB_USER"));
            String parsedUsername = extractUsername(databaseUrl);
            String username = firstNonBlank(explicitUsername, parsedUsername);
            if (StringUtils.hasText(username)) {
                overrides.put("spring.datasource.username", username);
            }
        }

        if (!StringUtils.hasText(environment.getProperty("spring.datasource.password"))) {
            String explicitPassword = firstNonBlank(
                    environment.getProperty("SPRING_DATASOURCE_PASSWORD"),
                    environment.getProperty("DB_PASSWORD"));
            String parsedPassword = extractPassword(databaseUrl);
            String password = firstNonBlank(explicitPassword, parsedPassword);
            if (StringUtils.hasText(password)) {
                overrides.put("spring.datasource.password", password);
            }
        }

        if (!overrides.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
        }
    }

    static String toJdbcPostgresUrl(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return null;
        }

        // Keep only JDBC PostgreSQL URLs as-is; anything else must be normalized or ignored.
        if (rawUrl.startsWith("jdbc:postgresql://")) {
            return rawUrl;
        }
        if (rawUrl.startsWith("jdbc:")) {
            return null;
        }

        String normalized = rawUrl;
        if (normalized.startsWith("postgres://")) {
            normalized = "postgresql://" + normalized.substring("postgres://".length());
        }

        if (!normalized.startsWith("postgresql://")) {
            return null;
        }

        URI uri = URI.create(normalized);
        String host = uri.getHost();
        if (!StringUtils.hasText(host)) {
            return null;
        }

        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = StringUtils.hasText(uri.getPath()) ? uri.getPath() : "/postgres";

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                .append(host)
                .append(':')
                .append(port)
                .append(path);

        if (StringUtils.hasText(uri.getQuery())) {
            jdbc.append('?').append(uri.getQuery());
        }

        if (!hasSslMode(uri.getQuery())) {
            jdbc.append(StringUtils.hasText(uri.getQuery()) ? '&' : '?').append("sslmode=require");
        }

        return jdbc.toString();
    }

    private static boolean hasSslMode(String query) {
        if (!StringUtils.hasText(query)) {
            return false;
        }
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("sslmode=")) {
                return true;
            }
        }
        return false;
    }

    private static String extractUsername(String rawUrl) {
        URI uri = safeUri(rawUrl);
        if (uri == null || !StringUtils.hasText(uri.getUserInfo())) {
            return null;
        }
        String[] parts = uri.getUserInfo().split(":", 2);
        return parts.length > 0 ? parts[0] : null;
    }

    private static String extractPassword(String rawUrl) {
        URI uri = safeUri(rawUrl);
        if (uri == null || !StringUtils.hasText(uri.getUserInfo())) {
            return null;
        }
        String[] parts = uri.getUserInfo().split(":", 2);
        return parts.length == 2 ? parts[1] : null;
    }

    private static URI safeUri(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return null;
        }

        String normalized = rawUrl;
        if (normalized.startsWith("jdbc:")) {
            normalized = normalized.substring("jdbc:".length());
        }
        if (normalized.startsWith("postgres://")) {
            normalized = "postgresql://" + normalized.substring("postgres://".length());
        }

        if (!normalized.startsWith("postgresql://")) {
            return null;
        }

        return URI.create(normalized);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}

