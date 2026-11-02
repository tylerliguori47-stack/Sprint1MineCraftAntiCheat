# Test plan

| Test | Procedure | Expected result | Status from evidence |
| --- | --- | --- | --- |
| Server startup | Start local server; inspect console | Server/world initialization completes | Demonstrated |
| Local join | Join using `localhost` | Client enters world | Demonstrated |
| Whitelist | Run permitted whitelist command | Console logs whitelist activity | Demonstrated |
| Plugin loading | Run `/plugins` | `MinecraftAntiCheat` appears | Demonstrated |
| Basic example command | Build example plugin; run `/anticheat status` | Status message | Not run / example only |
| Movement/speed | Simulate exceeding configured speed | Violation stored | Planned |
| Exempt teleport/flight | Move with valid exemptions | No false violation | Planned |
| Alerts | Trigger violation with moderator online | Alert reaches staff | Planned |
| History persistence | Restart after violation | Previously stored record available | Planned |
| Permissions | Non-admin runs restricted command | Access denied | Planned |

Record actual date, environment, screenshot/log, result and tester for each new run.
