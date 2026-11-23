package review;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/** Writes a self-contained HTML view and the machine-readable evidence used to render it. */
final class Report {
    static void write(Path out, Instant start, List<OfflineReview.Result> results) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\"started\":").append(quote(start.toString()));
        json.append(",\"runtime\":").append(quote(Runtime.version().toString()));
        json.append(",\"sourceHashes\":").append(quote(resource("/reviewed/source-hashes.txt")));
        json.append(",\"cases\":[");
        boolean first = true;
        for (var r : results) {
            if (!first) json.append(',');
            first = false;
            var c = r.spec();
            json.append("{\"id\":").append(quote(c.id()));
            json.append(",\"group\":").append(quote(c.group()));
            json.append(",\"title\":").append(quote(c.title()));
            json.append(",\"expected\":").append(quote(c.expected()));
            json.append(",\"status\":").append(quote(r.status()));
            json.append(",\"notes\":[");
            json.append(r.notes().stream().map(Report::quote).collect(Collectors.joining(",")));
            json.append("]}");
        }
        json.append("],\"diskLog\":").append(quote(readIfPresent(out.resolve("H02/plugins/MinecraftAntiCheat/violations.log"))));
        json.append(",\"recentAfterReopen\":").append(quote(readIfPresent(out.resolve("H02/recent-after-reopen.txt"))));
        json.append("}");
        Files.writeString(out.resolve("results.json"), json);
        Files.writeString(out.resolve("report.html"), resource("/report-template.html").replace("{{RUN_DATA}}", json));
    }

    private static String readIfPresent(Path file) throws IOException {
        return Files.exists(file) ? Files.readString(file) : "";
    }

    private static String resource(String name) throws IOException {
        try (var stream = Objects.requireNonNull(Report.class.getResourceAsStream(name), name)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String quote(String text) {
        StringBuilder escaped = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 32 || c == '<' || c == '>' || c == '&' || c == 0x2028 || c == 0x2029)
                        escaped.append(String.format(Locale.ROOT, "\\u%04x", (int)c));
                    else escaped.append(c);
                }
            }
        }
        return escaped.append('"').toString();
    }
}
