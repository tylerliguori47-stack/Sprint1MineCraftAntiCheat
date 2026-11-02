# Sprint 1 — initial program and evidence

**Goal:** establish a working local Minecraft server environment, connect a player, and verify plugin loading.

## Results backed by the Sprint 1 report

1. Server console showed Minecraft world preparation and successful server startup.
2. A Minecraft client connected via **Direct Connection → `localhost`**, entering the world.
3. The server console showed whitelist messages / access management activity.
4. Running `/plugins` displayed `MinecraftAntiCheat`.

These tests establish a runnable server and plugin integration. They **do not** demonstrate an implemented movement-detection algorithm, violation logging, moderation alerts, or enforcement.

## Sprint backlog (reported)

| ID | Acceptance criterion | Points | Reported result |
| --- | --- | ---: | --- |
| S1-01 | Start server locally; verify startup logs | 5 | Demonstrated |
| S1-02 | Connect client via localhost and spawn | 5 | Demonstrated |
| S1-03 | Exercise whitelist/access-management commands | 3 | Demonstrated |
| S1-04 | Plugin appears in `/plugins` | 5 | Demonstrated |
| S1-05 | Complete repeatable setup documentation | 2 | Partially documented |

The report described 18 of 20 story points as having demonstrable acceptance criteria, with the 2-point documentation item needing verification. Story points are not grading points.

## Original evidence to add

If you have the original `Sprint1.pdf` screenshot pages, save them into `docs/evidence/` after checking that no private identifiers, IP addresses, or secrets are visible. Do not claim screenshots were added if they are missing.

## Next Sprint

Implement and verify movement rule(s); test exemptions; record violation logs; add staff alerts; document reproducible test steps.
