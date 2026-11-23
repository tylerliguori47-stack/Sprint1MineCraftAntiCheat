package org.bukkit.configuration.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Reads the two-level scalar mappings used by the reviewed config.yml. */
public final class FileConfiguration {
    private final Map<String, String> values = new HashMap<>();

    public FileConfiguration(Path file) throws IOException {
        String section = "";
        for (String line : Files.readAllLines(file)) {
            String text = line.strip();
            if (text.isEmpty() || text.startsWith("#")) continue;
            int split = text.indexOf(':');
            if (split < 0) throw new IOException("Unsupported configuration line: " + line);
            String key = text.substring(0, split).trim();
            String value = text.substring(split + 1).trim();
            if (value.isEmpty()) section = key;
            else values.put(section + "." + key, value);
        }
    }

    public int getInt(String key, int fallback) {
        return values.containsKey(key) ? Integer.parseInt(values.get(key)) : fallback;
    }
    public long getLong(String key, long fallback) {
        return values.containsKey(key) ? Long.parseLong(values.get(key)) : fallback;
    }
    public double getDouble(String key, double fallback) {
        return values.containsKey(key) ? Double.parseDouble(values.get(key)) : fallback;
    }
    public boolean getBoolean(String key, boolean fallback) {
        return values.containsKey(key) ? Boolean.parseBoolean(values.get(key)) : fallback;
    }
}
