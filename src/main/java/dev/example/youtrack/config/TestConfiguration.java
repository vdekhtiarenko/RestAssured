package dev.example.youtrack.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record TestConfiguration(String baseUrl, String token) {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    public static TestConfiguration load() {
        Map<String, String> dotenv = readDotEnv(Path.of(".env"));
        String baseUrl = resolve("youtrack.baseUrl", "YOUTRACK_BASE_URL", dotenv, "YOUTRACK_BASE_URL");
        String token = resolve("youtrack.token", "YOUTRACK_TOKEN", dotenv, "YOUTRACK_TOKEN");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("YouTrack token is missing. Set youtrack.token, YOUTRACK_TOKEN, or YOUTRACK_TOKEN in .env.");
        }
        return new TestConfiguration(baseUrl.replaceAll("/+$", ""), token);
    }

    @Override
    public String toString() {
        return "TestConfiguration[baseUrl=" + baseUrl
                + ", token=<redacted>]";
    }

    private static String resolve(String propertyName, String environmentName,
                                  Map<String, String> dotenv, String dotenvName) {
        String value = System.getProperty(propertyName);
        if (present(value)) return value;
        value = System.getenv(environmentName);
        if (present(value)) return value;
        return dotenv.get(dotenvName);
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }

    private static Map<String, String> readDotEnv(Path path) {
        Map<String, String> values = new HashMap<>();
        if (!Files.isRegularFile(path)) return values;
        try {
            for (String rawLine : Files.readAllLines(path)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int separator = line.indexOf('=');
                if (separator < 1) continue;
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read .env configuration file", e);
        }
        return values;
    }
}
