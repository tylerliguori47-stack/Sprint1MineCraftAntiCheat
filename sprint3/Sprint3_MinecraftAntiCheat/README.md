# CS 370 Minecraft Anti-Cheat — Sprint 3 implementation

Based on the team's Sprint 1 Maven plugin scaffold and SJ-coder7's Sprint 2 `SpeedCheck.java` and `FlightCheck.java` implementations. Sprint 2 source: https://github.com/SJ-coder7/Sprint-2-Anti-Cheats-Flight-and-Speed

## Delivered in source
- Integrated horizontal speed check (real elapsed time, two consecutive violations, exemptions).
- Integrated hover/extended-rise flight check with exemptions and configurable parameters.
- Persist last violations in `plugins/MinecraftAntiCheat/violations.log` and retain recent 20 entries in memory.
- Authorized player alerts with a cooldown. `/anticheat status`, `/anticheat recent`, `/anticheat reload`.
- Safe alert-only mode: no automatic kick, ban or dangerous teleport.

## Build on Windows
1. Install Java 21 and Maven; run `java -version` and `mvn -version`.
2. In the `plugin` folder run `mvn clean package` (requires internet access for Paper API).
3. Copy `target/MinecraftAntiCheat-0.3.0-SNAPSHOT.jar` into the Paper 1.21.4 server's `plugins` folder and restart.
4. As server operator, use `/anticheat status`, `/anticheat recent` and `/anticheat reload`.
5. Tune `plugins/MinecraftAntiCheat/config.yml` for controlled test conditions.

## Manual testing
- Survival normal walking/running/jumping: check no alerts.
- Creative or spectator flight: exempt.
- Speed effect, elytra, vehicle, water: exempt.
- Controlled altered-speed test client on **your own server**: speed alert should appear to OP and in log.
- Controlled flight/hover test on your own server: flight alert should appear.
- Disconnect, teleport, restart: no crash; old log lines remain after restart.
- Non-OP tries `/anticheat recent`: blocked.
- Verify console has no stack traces; review alert frequency and adjust thresholds.

## Verification boundary
Source-level implementation prepared. This environment has Java 21 but lacks Maven/Paper dependencies and a running Minecraft/Paper server. **A successful Maven build and in-game behavior are not yet verified.** Record actual video evidence and team self-assessments after executing tests.

## Known limitations
- Flight heuristics are intentionally simple and can produce false positives on complex terrain/movement mechanics.
- `PlayerMoveEvent` frequency varies; air-move counts are not true server ticks.
- Persistent history is append-only plain text; it should eventually rotate files and add retention limits.
- History is loaded in full on startup; rotate oversized files before production use.
- Reload affects thresholds immediately but not alert cooldown until restart.
