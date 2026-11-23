package review;

import static review.TestServer.require;

import edu.csusm.cs370.anticheat.FlightCheck;
import edu.csusm.cs370.anticheat.SpeedCheck;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import review.TestServer.TestPlayer;

final class Scenarios {
    interface Check { void run(TestServer f) throws Exception; }
    record Case(String id, String group, String title, String expected, Check check) {}

    static final List<Case> ALL = List.of(
        new Case("C01", "staff", "Enable and status", "Register both listeners and the five-tick sampler. Report alert-only mode to the administrator.", Scenarios::enable),
        new Case("C02", "staff", "Command permissions", "Deny status, recent and reload to a sender without anticheat.admin.", Scenarios::permissions),
        new Case("S01", "speed", "Ordinary horizontal travel", "At a simulated 4 blocks per second, record zero speed flags with the default limit of 8.", Scenarios::ordinary),
        new Case("S02", "speed", "Two fast samples", "Keep the first fast sample pending and record SPEED on the second consecutive sample.", Scenarios::fast),
        new Case("S03", "speed", "Interrupted speed streak", "A low-speed sample breaks the streak. Two new high samples are needed.", Scenarios::speedStreak),
        new Case("S04", "speed", "Teleport and reconnect", "Clear speed history across a teleport and a quit/join, then detect a fresh two-sample streak.", Scenarios::speedReset),
        new Case("S05", "speed", "Speed exemptions", "With identical fast travel, flag only the ordinary control among the exempt player states.", Scenarios::speedExempt),
        new Case("S06", "speed", "Threshold reload", "After editing the copied config from 8 to 2 and running reload, flag 4 blocks per second in the same instance.", Scenarios::reload),
        new Case("F01", "flight", "Jump and landing", "Leave four ordinary jump-shaped movement traces unflagged.", Scenarios::jump),
        new Case("F02", "flight", "Level airborne travel", "Hold through 14 level, position-changing air moves. Record FLIGHT_HOVER on move 15.", Scenarios::hover),
        new Case("F03", "flight", "Sustained ascent", "Hold through 16 upward moves of 0.12 blocks. Record FLIGHT_ASCEND on move 17.", Scenarios::ascend),
        new Case("F04", "flight", "Flight exemptions", "For the same level air path, flag the ordinary control and spare the selected exempt states.", Scenarios::flightExempt),
        new Case("F05", "flight", "Flight history resets", "Landing, teleport and reconnect clear accumulated air moves before a fresh streak.", Scenarios::flightReset),
        new Case("H01", "history", "From movement to moderator history", "A flight flag reaches authorized chat, a console warning, the disk log and the recent command.", Scenarios::pipeline),
        new Case("H02", "history", "Last 20 and disk persistence", "Append 25 distinct test records. Return the newest 20 in order before and after reopening the same folder.", Scenarios::history),
        new Case("A01", "staff", "Staff alerts and cooldown", "Throttle repeated chat for the same player and check. Keep all four disk records and warnings.", Scenarios::alerts),
        new Case("A02", "staff", "Disable staff chat", "With alerts disabled in the reloaded config, keep warning and saving violations.", Scenarios::muted),
        new Case("A03", "staff", "Cooldown configuration lifetime", "Apply a new cooldown after constructing a fresh plugin instance and retain the old duration across reload.", Scenarios::cooldown),
        new Case("L01", "flight", "Stationary hover coverage", "Probe whether the hover check can flag continued suspension at identical coordinates.", Scenarios::stationary),
        new Case("L02", "speed", "Water exemption in the README", "Probe the README's water exemption for a fast-moving player in water with the swimming flag off.", Scenarios::water)
    );

    private static void enable(TestServer f) {
        require(f.listeners.size() == 2, "Listener count: " + f.listeners.size());
        require(f.listener(FlightCheck.class) != null && f.listener(SpeedCheck.class) != null, "Missing listener");
        require(f.timer != null && f.delay == 5 && f.period == 5, "Unexpected schedule");
        require(f.plugin.getConfig().getDouble("speed.max-blocks-per-second", -1) == 8.0, "Default limit missing");
        String status = f.command(f.moderator, "status").getFirst();
        require(status.contains("Checks: Speed, Flight") && status.contains("Mode: alert only"), "Unexpected status: " + status);
        require(f.command(f.moderator).contains(status), "No-argument status differs");
        f.note("2 listeners registered. Timer delay and period are both 5 ticks in the fixture.");
        f.note(status);
    }

    private static void permissions(TestServer f) {
        for (String cmd : List.of("status", "recent", "reload")) {
            List<String> reply = f.command(f.player, cmd);
            require(reply.size() == 1 && reply.getFirst().contains("don't have permission"), "Allowed " + cmd);
        }
        require(f.command(f.moderator, "recent").getFirst().contains("Last 0 violation"), "Empty history response");
        f.note("3 of 3 direct command-handler calls denied. Admin recent returned 0 entries.");
    }

    private static void ordinary(TestServer f) throws Exception {
        f.sample();
        for (int i = 0; i < 4; i++) f.travel(4);
        require(f.disk().isEmpty(), "Unexpected ordinary-movement record: " + f.disk());
        f.note("4 samples at approximately 4 blocks/s with an 8 blocks/s limit. Disk records = 0.");
    }

    private static String onlyRecord(TestServer f, String check) throws IOException {
        List<String> disk = f.disk();
        require(disk.size() == 1, "Expected one " + check + " record, got " + disk);
        String[] fields = fields(disk.getFirst());
        require(fields[2].equals(check), "Expected " + check + ", found " + fields[2]);
        require(fields[1].equals(f.player.getUniqueId().toString()), "Incorrect test player UUID");
        return disk.getFirst();
    }

    private static String[] fields(String line) {
        String[] parts = line.split(" \\| ", -1);
        require(parts.length == 5, "Wrong record shape: " + line);
        Instant.parse(parts[0]);
        UUID.fromString(parts[1]);
        require(Double.isFinite(Double.parseDouble(parts[3])), "Observed value must be finite");
        require(Double.isFinite(Double.parseDouble(parts[4])), "Threshold must be finite");
        return parts;
    }

    private static void fast(TestServer f) throws Exception {
        f.sample();
        f.travel(20);
        require(f.disk().isEmpty(), "Flagged after a single sample");
        f.travel(20);
        String record = onlyRecord(f, "SPEED");
        String[] fields = fields(record);
        require(Double.parseDouble(fields[3]) > 8 && Double.parseDouble(fields[4]) == 8, "Unexpected measurement/limit");
        f.note("Disk records after fast samples 1 and 2 = 0, then 1.");
        f.note(record);
    }

    private static void speedStreak(TestServer f) throws Exception {
        f.sample();
        for (double speed : List.of(20.0, 4.0, 20.0)) f.travel(speed);
        require(f.disk().isEmpty(), "A separated pair of fast samples produced a flag");
        f.travel(20);
        onlyRecord(f, "SPEED");
        f.note("20, 4, 20 blocks/s produced 0 flags. One more 20 blocks/s sample produced 1.");
    }

    private static void speedReset(TestServer f) throws Exception {
        f.sample();
        f.travel(20);
        f.teleport(f.player);
        f.sample();
        f.travel(20);
        require(f.disk().isEmpty(), "Teleport retained a previous speed streak");
        f.reconnect(f.player);
        f.sample();
        f.travel(20);
        require(f.disk().isEmpty(), "Reconnect retained a previous speed streak");
        f.travel(20);
        onlyRecord(f, "SPEED");
        f.note("0 flags across both resets. Two fresh high samples produced 1 SPEED record.");
    }

    private static void speedExempt(TestServer f) throws Exception {
        f.add("Creative").mode = GameMode.CREATIVE;
        f.add("Spectator").mode = GameMode.SPECTATOR;
        f.add("Rider").riding = true;
        f.add("Gliding").gliding = true;
        f.add("Swimming").swimming = true;
        f.add("SpeedEffect").effects.add(PotionEffectType.SPEED);
        f.add("DolphinEffect").effects.add(PotionEffectType.DOLPHINS_GRACE);
        f.add("PermittedFlight").flightAllowed = true;
        f.add("Flying").flying = true;
        f.add("Bypass").permissions.add("anticheat.bypass");
        f.add("Dead").dead = true;
        List<TestPlayer> movers = f.online.stream().filter(p -> p != f.moderator).toList();
        f.sample();
        f.travel(movers, 20);
        f.travel(movers, 20);
        onlyRecord(f, "SPEED");
        f.note("Ordinary control = 1 flag. All 11 exempt states = 0 flags.");
    }

    private static void reload(TestServer f) throws Exception {
        f.sample();
        f.travel(4);
        f.travel(4);
        require(f.disk().isEmpty(), "Default settings flagged the input");
        f.editSetting("max-blocks-per-second: 8.0", "max-blocks-per-second: 2.0");
        String reply = f.command(f.moderator, "reload").getFirst();
        require(reply.contains("reloaded"), "Missing reload acknowledgment");
        f.travel(4);
        f.travel(4);
        String record = onlyRecord(f, "SPEED");
        require(Double.parseDouble(fields(record)[4]) == 2.0, "Old threshold still in use");
        f.note("Same 4 blocks/s input. Threshold 8 = 0 flags. Threshold 2 after reload = 1 flag.");
        f.note(record);
    }

    private static void jump(TestServer f) throws Exception {
        double[] arc = {0.42, 0.333, 0.248, 0.165, 0.083, 0.003, -0.075, -0.152, -0.227, -0.302, -0.373};
        for (int jump = 0; jump < 4; jump++) {
            f.player.ground = false;
            for (double dy : arc) f.move(f.player, 0.15, dy);
            f.player.ground = true;
            f.move(f.player, 0.15, -0.123);
        }
        require(f.disk().isEmpty(), "Ordinary jump trace produced a flag");
        f.note("4 scripted jumps with a landing between each. Disk records = 0.");
    }

    private static void hover(TestServer f) throws Exception {
        f.air(f.player, 14, 0);
        require(f.disk().isEmpty(), "Early hover flag");
        f.air(f.player, 1, 0);
        String record = onlyRecord(f, "FLIGHT_HOVER");
        f.note("14 level air moves = 0 records. Move 15 = 1 FLIGHT_HOVER.");
        f.note(record);
    }

    private static void ascend(TestServer f) throws Exception {
        f.air(f.player, 16, 0.12);
        require(f.disk().isEmpty(), "Early ascent flag");
        f.air(f.player, 1, 0.12);
        String record = onlyRecord(f, "FLIGHT_ASCEND");
        f.note("16 rising moves = 0 records. Move 17 = 1 FLIGHT_ASCEND.");
        f.note(record);
    }

    private static void flightExempt(TestServer f) throws Exception {
        f.add("Creative").mode = GameMode.CREATIVE;
        f.add("Spectator").mode = GameMode.SPECTATOR;
        f.add("Riding").riding = true;
        f.add("Gliding").gliding = true;
        f.add("Swimming").swimming = true;
        f.add("Climbing").climbing = true;
        f.add("InWater").material = Material.WATER;
        f.add("Scaffolding").material = Material.SCAFFOLDING;
        f.add("PermittedFlight").flightAllowed = true;
        f.add("Levitation").effects.add(PotionEffectType.LEVITATION);
        f.add("SlowFall").effects.add(PotionEffectType.SLOW_FALLING);
        f.add("JumpBoost").effects.add(PotionEffectType.JUMP_BOOST);
        f.add("Bypass").permissions.add("anticheat.bypass");
        for (TestPlayer p : f.online) {
            if (p != f.moderator) f.air(p, 16, 0);
        }
        onlyRecord(f, "FLIGHT_HOVER");
        f.note("Ordinary control = 1 hover flag. All 13 selected exempt states = 0 flags.");
    }

    private static void flightReset(TestServer f) throws Exception {
        f.air(f.player, 14, 0);
        f.player.ground = true;
        f.move(f.player, 0.2, 0);
        f.air(f.player, 14, 0);
        f.teleport(f.player);
        f.air(f.player, 14, 0);
        f.reconnect(f.player);
        f.air(f.player, 14, 0);
        require(f.disk().isEmpty(), "An air-move streak survived a reset");
        f.air(f.player, 1, 0);
        onlyRecord(f, "FLIGHT_HOVER");
        f.note("0 flags through landing, teleport and reconnect. Fresh move 15 produced 1 hover flag.");
    }

    private static void pipeline(TestServer f) throws Exception {
        f.air(f.player, 15, 0);
        String line = onlyRecord(f, "FLIGHT_HOVER");
        require(f.moderator.alerts() == 1 && f.player.alerts() == 0, "Wrong alert recipients");
        require(f.warnings() == 1, "Missing console warning");
        var recent = f.command(f.moderator, "recent");
        require(recent.size() == 2 && recent.get(1).equals(line), "History differs from disk");
        f.note("1 registered-listener flag, 1 staff chat, 0 ordinary-player chats, 1 console warning. Recent exactly matched disk.");
        f.note(f.moderator.messages.getFirst());
        f.note(line);
    }

    private static void history(TestServer f) throws Exception {
        // Seed the reporting interface directly with unique measurements to make order observable.
        for (int i = 1; i <= 25; i++) f.flag(f.player, "SPEED", 8 + i, 8);
        var lines = f.disk();
        require(lines.size() == 25, "Expected 25 append-only disk records, got " + lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String[] p = fields(lines.get(i));
            require(p[2].equals("SPEED") && Double.parseDouble(p[3]) == 9.0 + i &&
                    Double.parseDouble(p[4]) == 8.0, "Missing or reordered input at index " + i);
        }
        var tail = lines.subList(5, 25);
        require(f.recent().equals(tail), "Wrong in-memory history");
        var before = f.command(f.moderator, "recent");
        require(before.size() == 21 && before.subList(1, 21).equals(tail), "Pre-reopen command mismatch");
        f.startPlugin();
        var after = f.command(f.moderator, "recent");
        require(after.equals(before) && f.recent().equals(tail) && f.disk().equals(lines), "Reopen changed history");
        Files.write(f.dir.resolve("recent-after-reopen.txt"), after.subList(1, 21));
        f.note("25 records on disk. The command returned rows 6–25 in order both times. New instance loaded the same 20 from the file.");
        f.note("First retained observation = 14.000. Last retained observation = 33.000.");
    }

    private static void alerts(TestServer f) throws Exception {
        var adminOnly = f.add("AdminWithoutAlerts");
        adminOnly.permissions.add("anticheat.admin");
        f.flag(f.player, "SPEED", 16, 8);
        f.flag(f.player, "SPEED", 17, 8);
        f.flag(f.player, "FLIGHT_HOVER", 0, 0.03);
        f.flag(adminOnly, "SPEED", 18, 8);
        require(f.moderator.alerts() == 3 && f.player.alerts() == 0 && adminOnly.alerts() == 0, "Chat routing or cooldown mismatch");
        require(f.warnings() == 4 && f.disk().size() == 4, "Logging unexpectedly throttled");
        f.note("4 flags = 4 disk entries, 4 warnings, 3 staff alerts, 0 messages to either sender without the alerts permission.");
    }

    private static void muted(TestServer f) throws Exception {
        f.editSetting("enabled: true", "enabled: false");
        f.command(f.moderator, "reload");
        f.flag(f.player, "SPEED", 16, 8);
        onlyRecord(f, "SPEED");
        require(f.warnings() == 1 && f.moderator.alerts() == 0, "Muted chat affected warning or routing");
        f.note("With alerts.enabled=false, 0 staff alerts, 1 warning and 1 disk entry.");
    }

    private static void cooldown(TestServer f) throws Exception {
        f.editSetting("cooldown-ms: 5000", "cooldown-ms: 60000");
        f.startPlugin();
        f.flag(f.player, "SPEED", 16, 8);
        f.editSetting("cooldown-ms: 60000", "cooldown-ms: 1000");
        f.command(f.moderator, "reload");
        Thread.sleep(1200);
        f.flag(f.player, "SPEED", 16, 8);
        require(f.moderator.alerts() == 1, "Reload changed cooldown early");
        f.startPlugin();
        long before = f.moderator.alerts();
        f.flag(f.player, "SPEED", 16, 8);
        Thread.sleep(1200);
        f.flag(f.player, "SPEED", 16, 8);
        require(f.moderator.alerts() - before == 2, "New instance did not adopt the shorter cooldown");
        f.note("Flags 1.2 seconds apart produced 1 chat after reload and 2 chats with a fresh instance.");
    }

    private static void stationary(TestServer f) throws Exception {
        f.air(f.player, 1, 0);
        for (int i = 0; i < 40; i++) f.move(f.player, 0, 0);
        if (f.disk().isEmpty()) {
            f.limit("1 airborne movement followed by 40 identical-position events produced 0 flags. The current hover check needs position changes.");
        } else {
            require(f.disk().stream().allMatch(s -> s.contains(" | FLIGHT_HOVER | ")), "Unexpected stationary flag type");
            f.note("Stationary input produced a hover record.");
        }
    }

    private static void water(TestServer f) throws Exception {
        f.player.material = Material.WATER;
        TestPlayer swimmer = f.add("Swimmer");
        swimmer.swimming = true;
        swimmer.material = Material.WATER;
        f.sample();
        f.travel(List.of(f.player, swimmer), 20);
        f.travel(List.of(f.player, swimmer), 20);
        if (f.disk().isEmpty()) {
            f.note("Both modeled water states were exempt at the test speed.");
        } else {
            String line = onlyRecord(f, "SPEED");
            f.limit("At 20 blocks/s in a water block, the non-swimming input produced 1 SPEED flag. The swimming input produced 0. The README describes water more broadly.");
            f.note(line);
        }
    }
}
