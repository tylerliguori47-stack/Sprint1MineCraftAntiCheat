# Minecraft Anti-Cheat — CS 370

A team course project for a server-side Minecraft Java Edition anti-cheat plugin.

> **Project status:** Sprint 1 verified a locally running Minecraft server, localhost client connection, whitelist activity, and a plugin named `MinecraftAntiCheat` visible in the server's plugin list. **Cheat detection, violation logging, alerting, and enforcement were not demonstrated by the provided evidence.**

## What was demonstrated in Sprint 1

| Task | Evidence / result |
| --- | --- |
| Start a local Minecraft server | Startup console completed world loading |
| Join the server | Minecraft client used Direct Connection to `localhost` and entered world |
| Basic access control | Whitelist activity appeared in server console |
| Verify plugin loaded | `/plugins` displayed `MinecraftAntiCheat` |

See [docs/SPRINT1.md](docs/SPRINT1.md) and [docs/SETUP.md](docs/SETUP.md).

## Repository contents

- `plugin/`: **newly created, minimal example** Paper plugin scaffold for future development. **Not the original Sprint 1 plugin source.** It logs on startup and implements `/anticheat status`.
- `docs/`: course project progress, proposed features, setup and testing instructions, and requirement mappings.
- `docs/usecase_diagram.png`: revised use-case diagram, if included.

## Getting started

1. Install a compatible Java JDK and a Paper Minecraft server. **Confirm the actual server version used by the team.** The example scaffold below targets Paper **1.21.4 / Java 21**; adjust as necessary.
2. Read [docs/SETUP.md](docs/SETUP.md) for your current Minecraft server startup steps.
3. To build the **example source**: `cd plugin && mvn clean package` (requires Maven and network access for first-time dependencies).
4. Copy `plugin/target/MinecraftAntiCheat-0.1.0-SNAPSHOT.jar` into the server's `plugins/` folder and restart the server.
5. In-game or console: `/plugins`; then `/anticheat status` (the second command exists only in the **new example scaffold**, not verified in the Sprint 1 plugin).

**Do not put an old and a newly built plugin JAR with the same plugin name in `plugins/` simultaneously.** Back up your original JAR first.

## Planned functionality (not yet evidenced)

- Detect abnormal movement, with permitted-flight / teleport exemptions.
- Record timestamped player violations and details.
- Alert authorized moderators and show recent violation history.
- Configure detection thresholds, alert-only mode, and escalation rules.

## Project documentation

The [SRS](docs/REQUIREMENTS.md) defines these goals. The revised Lab 7 use cases cover detecting violations, reviewing player violations, configuring detection, managing enforcement, and viewing system status.

## Academic accuracy

This repository combines **verified Sprint 1 progress**, **documented future requirements**, and a **fresh example plugin scaffold**. It does not claim the source of the original installed plugin has been recovered. Add the actual team's server scripts, plugin source, test output, screenshots, and team-approved contribution details when available.
