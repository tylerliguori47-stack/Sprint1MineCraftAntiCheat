import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.*;
import javax.tools.ToolProvider;

/** Rebuild the desktop verification JAR from the included project and fixture sources. */
class Build {
    public static void main(String[] args) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (Runtime.version().feature() != 21 || compiler == null)
            throw new IllegalStateException("Use a Java 21 JDK to rebuild the review runner.");
        Path project = Path.of("Sprint3_MinecraftAntiCheat", "plugin");
        Path fixtures = Path.of("checks", "src");
        Path build = Files.createTempDirectory(Path.of("."), "review-build-");
        try {
            var sources = new ArrayList<String>();
            sources.addAll(javaFiles(project.resolve("src/main/java")));
            sources.addAll(javaFiles(fixtures));
            var options = new ArrayList<>(List.of("-encoding", "UTF-8", "-d", build.toString()));
            options.addAll(sources);
            if (compiler.run(null, System.out, System.err, options.toArray(String[]::new)) != 0)
                throw new IllegalStateException("Compilation failed");
            Path meta = Files.createDirectories(build.resolve("reviewed"));
            List<Path> inputs = new ArrayList<>();
            try (var files = Files.walk(project)) {
                files.filter(Files::isRegularFile)
                     .filter(p -> p.startsWith(project.resolve("src/main")) || p.equals(project.resolve("pom.xml")))
                     .sorted().forEach(inputs::add);
            }
            var hashes = new ArrayList<String>();
            for (Path input : inputs) {
                var sha = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input));
                String relative = project.relativize(input).toString().replace('\\', '/');
                hashes.add(HexFormat.of().formatHex(sha) + "  " + relative);
            }
            Files.write(meta.resolve("source-hashes.txt"), hashes, StandardCharsets.UTF_8);
            for (String file : List.of("config.yml", "plugin.yml"))
                Files.copy(project.resolve("src/main/resources").resolve(file), meta.resolve(file));
            Files.copy(Path.of("checks", "report-template.html"), build.resolve("report-template.html"));
            var manifest = new Manifest();
            var attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attributes.put(Attributes.Name.MAIN_CLASS, "review.OfflineReview");
            Path staged = build.resolveSibling(build.getFileName() + ".jar");
            try {
                try (var jar = new JarOutputStream(Files.newOutputStream(staged), manifest);
                     var files = Files.walk(build)) {
                    for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                        jar.putNextEntry(new JarEntry(build.relativize(file).toString().replace('\\', '/')));
                        Files.copy(file, jar);
                        jar.closeEntry();
                    }
                }
                Files.move(staged, Path.of("sprint3-checks.jar"), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(staged);
            }
            System.out.println("Built sprint3-checks.jar from the project sources and test fixtures.");
        } finally {
            try (var paths = Files.walk(build)) {
                for (var path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }
    private static List<String> javaFiles(Path root) throws IOException {
        try (var files = Files.walk(root)) {
            return files.filter(p -> p.toString().endsWith(".java")).sorted().map(Path::toString).toList();
        }
    }
}
