package review;

import edu.csusm.cs370.anticheat.FlightCheck;
import edu.csusm.cs370.anticheat.MinecraftAntiCheatPlugin;
import edu.csusm.cs370.anticheat.SpeedCheck;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

/** Supplies player states and invokes the listeners and timer registered by the real plugin. */
final class TestServer {
    final Path dir;
    final Path pluginFolder;
    final List<TestPlayer> online = new ArrayList<>();
    final List<String> console = new ArrayList<>();
    final List<String> commands = new ArrayList<>();
    final List<String> observations = new ArrayList<>();
    final List<Listener> listeners = new ArrayList<>();
    final TestPlayer player;
    final TestPlayer moderator;
    MinecraftAntiCheatPlugin plugin;
    Runnable timer;
    long delay, period;
    String limitation;
    private final Logger logger = Logger.getAnonymousLogger();

    TestServer(Path dir) throws IOException {
        this.dir = dir;
        this.pluginFolder = dir.resolve("plugins/MinecraftAntiCheat");
        Files.createDirectories(pluginFolder);
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            public void publish(LogRecord r) { console.add(r.getLevel() + " " + r.getMessage()); }
            public void flush() {}
            public void close() {}
        });
        player = add("TestPlayer");
        moderator = add("Moderator");
        moderator.permissions.add("anticheat.admin");
        moderator.permissions.add("anticheat.alerts");
        startPlugin();
    }

    void startPlugin() {
        listeners.clear();
        timer = null;
        Bukkit.useFixture(online, (owner, callback, delayTicks, periodTicks) -> {
            timer = callback;
            delay = delayTicks;
            period = periodTicks;
            return new BukkitTask() {};
        });
        plugin = new MinecraftAntiCheatPlugin();
        plugin.attachFixture(pluginFolder, () -> (listener, owner) -> listeners.add(listener), logger);
        plugin.onEnable();
    }

    TestPlayer add(String name) {
        TestPlayer p = new TestPlayer(name);
        online.add(p);
        return p;
    }

    <T> T listener(Class<T> type) {
        return listeners.stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    void note(String message) { observations.add(message); }
    void limit(String message) { limitation = message; observations.add(message); }

    void sample() {
        require(timer != null, "No scheduled sampling callback");
        timer.run();
    }

    // Use measured elapsed time for the input distance. The actual detector uses its own clock.
    void travel(Collection<TestPlayer> players, double blocksPerSecond) throws InterruptedException {
        long start = System.nanoTime();
        Thread.sleep(250);
        double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;
        for (TestPlayer p : players) p.shift(blocksPerSecond * elapsed, 0);
        sample();
    }

    void travel(double speed) throws InterruptedException { travel(List.of(player), speed); }

    void move(TestPlayer p, double dx, double dy) {
        Location from = p.getLocation();
        p.shift(dx, dy);
        listener(FlightCheck.class).onMove(new PlayerMoveEvent(p, from, p.getLocation()));
    }

    void air(TestPlayer p, int count, double dy) {
        p.ground = false;
        for (int i = 0; i < count; i++) move(p, 0.2, dy);
    }

    void teleport(TestPlayer p) {
        Location from = p.getLocation();
        p.shift(100, 0);
        var event = new PlayerTeleportEvent(p, from, p.getLocation());
        listener(SpeedCheck.class).onTeleport(event);
        listener(FlightCheck.class).onTeleport(event);
    }

    void reconnect(TestPlayer p) {
        var quit = new PlayerQuitEvent(p);
        listener(SpeedCheck.class).onQuit(quit);
        listener(FlightCheck.class).onQuit(quit);
        online.remove(p);
        online.add(p);
        listener(SpeedCheck.class).onJoin(new PlayerJoinEvent(p));
    }

    List<String> command(TestPlayer sender, String... args) {
        int before = sender.messages.size();
        boolean handled = plugin.onCommand(sender, new Command("anticheat"), "anticheat", args);
        require(handled, "Command handler returned false");
        List<String> replies = List.copyOf(sender.messages.subList(before, sender.messages.size()));
        commands.add(sender.name + " > /anticheat " + String.join(" ", args));
        commands.addAll(replies);
        return replies;
    }

    void editSetting(String before, String after) throws IOException {
        Path config = pluginFolder.resolve("config.yml");
        String original = Files.readString(config);
        require(original.contains(before), "Expected setting missing from config: " + before);
        Files.writeString(config, original.replace(before, after));
    }

    void flag(TestPlayer who, String check, double observed, double threshold) {
        plugin.getViolations().flag(who, check, observed, threshold);
    }

    List<String> recent() { return plugin.getViolations().recent(); }
    List<String> disk() throws IOException {
        Path log = pluginFolder.resolve("violations.log");
        return Files.exists(log) ? Files.readAllLines(log) : List.of();
    }
    long warnings() { return console.stream().filter(line -> line.startsWith("WARNING AntiCheat:")).count(); }

    void saveTranscripts() throws IOException {
        Files.write(dir.resolve("console.txt"), console);
        Files.write(dir.resolve("commands.txt"), commands);
        var chat = new ArrayList<String>();
        for (TestPlayer p : online) {
            for (String message : p.messages) chat.add(p.name + " > " + message);
        }
        Files.write(dir.resolve("messages.txt"), chat);
    }

    static final class TestPlayer implements Player {
        final String name;
        private final UUID id;
        final List<String> messages = new ArrayList<>();
        final Set<String> permissions = new HashSet<>();
        final Set<PotionEffectType> effects = EnumSet.noneOf(PotionEffectType.class);
        GameMode mode = GameMode.SURVIVAL;
        Material material = Material.AIR;
        boolean ground = true, dead, flightAllowed, flying, riding, gliding, swimming, climbing;
        private final World world = location -> new Block() {
            public Material getType() { return material; }
            public boolean isLiquid() { return material == Material.WATER; }
        };
        private Location location = new Location(world, 0, 64, 0);

        TestPlayer(String name) {
            this.name = name;
            this.id = UUID.nameUUIDFromBytes(("CS370-fixture/" + name).getBytes(StandardCharsets.UTF_8));
        }
        void shift(double dx, double dy) {
            location = new Location(world, location.getX() + dx, location.getY() + dy, location.getZ());
        }
        long alerts() { return messages.stream().filter(s -> s.contains(" failed ")).count(); }
        public UUID getUniqueId() { return id; }
        public String getName() { return name; }
        public Location getLocation() { return location.clone(); }
        public GameMode getGameMode() { return mode; }
        public boolean isDead() { return dead; }
        public boolean getAllowFlight() { return flightAllowed; }
        public boolean isFlying() { return flying; }
        public boolean isInsideVehicle() { return riding; }
        public boolean isGliding() { return gliding; }
        public boolean isSwimming() { return swimming; }
        public boolean isOnGround() { return ground; }
        public boolean isClimbing() { return climbing; }
        public boolean hasPotionEffect(PotionEffectType effect) { return effects.contains(effect); }
        public boolean hasPermission(String permission) { return permissions.contains(permission); }
        public void sendMessage(String message) { messages.add(message.replaceAll("§[0-9a-fk-or]", "")); }
    }
}
