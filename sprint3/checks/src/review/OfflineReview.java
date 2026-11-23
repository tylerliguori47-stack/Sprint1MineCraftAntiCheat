package review;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class OfflineReview {
    record Result(Scenarios.Case spec, String status, List<String> notes) {}

    public static void main(String[] args) throws Exception {
        String group = args.length == 0 ? "all" : args[0];
        if (args.length > 1 || !List.of("all", "speed", "flight", "history", "staff").contains(group)) {
            System.out.println("Usage: java -jar sprint3-checks.jar [all|speed|flight|history|staff]");
            System.exit(args.length == 1 && args[0].equals("--help") ? 0 : 2);
        }
        Instant start = Instant.now();
        var stamp = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss").withZone(ZoneOffset.UTC).format(start);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Path out = Path.of("runs", stamp + "-" + suffix);
        Files.createDirectories(out);
        var transcript = new ArrayList<String>();
        var results = new ArrayList<Result>();
        print(transcript, "CS 370 Sprint 3 | Offline Java checks | " + start);
        print(transcript, "Java " + Runtime.version() + " | group " + group);
        print(transcript, "Inputs are simulated. Detection, command handlers and log I/O use the project sources.");
        for (var spec : Scenarios.ALL) {
            if (!group.equals("all") && !group.equals(spec.group())) continue;
            String status = "PASS";
            List<String> notes = new ArrayList<>();
            Path caseDir = out.resolve(spec.id());
            TestServer fixture = null;
            try {
                fixture = new TestServer(caseDir);
                notes = fixture.observations;
                spec.check().run(fixture);
                if (fixture.limitation != null) status = "LIMIT";
            } catch (Exception | AssertionError failure) {
                status = "FAIL";
                notes.add(failure.toString());
                var stack = new StringWriter();
                failure.printStackTrace(new PrintWriter(stack));
                Files.createDirectories(caseDir);
                Files.writeString(caseDir.resolve("failure.txt"), stack.toString());
            }
            if (fixture != null) fixture.saveTranscripts();
            results.add(new Result(spec, status, List.copyOf(notes)));
            print(transcript, "[" + status + "] " + spec.id() + "  " + spec.title());
            for (String note : notes) print(transcript, "  " + note);
        }
        long passed = count(results, "PASS"), limits = count(results, "LIMIT"), failed = count(results, "FAIL");
        print(transcript, passed + " passed | " + limits + " documented limits | " + failed + " unexpected failures");
        Report.write(out, start, results);
        Files.write(out.resolve("transcript.txt"), transcript, StandardCharsets.UTF_8);
        System.out.println("Results folder: " + out);
        System.exit(failed == 0 ? 0 : 1);
    }

    static long count(List<Result> results, String status) {
        return results.stream().filter(r -> r.status().equals(status)).count();
    }

    private static void print(List<String> transcript, String line) {
        transcript.add(line);
        System.out.println(line);
    }
}
