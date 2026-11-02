# Requirements summary (based on course SRS)

## Core functional requirements (planned)

- **R.1** Observe movement events; calculate horizontal movement; detect speed / flight-like behavior; exempt permitted flight and teleports.
- **R.2** Save violations with identifier, player identifier, timestamp, check, observed value, threshold and action. Notify authorized staff and let them retrieve 20 most recent records.
- **R.3** Allow authorized administrators to configure checks, threshold validation, alert-only mode and kick escalation after a configured count/time window.
- **R.4** Continue monitoring when one player disconnects or a single sample is invalid.
- **R.5** Optional performance optimization.

## Non-functional / quality targets

- Test with at least 20 connected players in the team's environment.
- Under this workload, 95% of alerts within 2 seconds.
- Persist logs across normal restart; report logging errors without crashing the Minecraft server.
- Restrict sensitive commands and server logs to authorized staff.

## Use-case focus after feedback

- UC-01 Detect and Record Suspicious Activity
- UC-02 Review Player Violations
- UC-03 Configure Detection Rules
- UC-04 Manage Enforcement Policy
- UC-05 Review Anti-Cheat Status

These are **requirements and designs**, not a claim of implementation. The SRS also notes the selected server API/version should be confirmed.
