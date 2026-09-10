package com.assessment.banking.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = TestConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "config.properties not found under src/test/resources");
            }

            PROPERTIES.load(input);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private TestConfig() {
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);

        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String value = PROPERTIES.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing configuration property: " + key);
        }

        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);

        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String value = PROPERTIES.getProperty(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static String baseUrl() {
        String url = get("baseUrl");

        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }
}
