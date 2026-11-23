# Sprint 3 backlog

**Goal.** Integrate the speed and flight checks with moderator alerts and a durable violation history. The five stories total **20 planned story points**.

| Story | Points | Acceptance criteria | Evidence from the offline run | Open work |
| --- | ---: | --- | --- | --- |
| S3-01 · Speed | 5 | Flag consecutive horizontal speed samples above the configured limit. Clear interrupted streaks and exempt supported movement. | S01–S06 passed. The first high sample stayed pending and the second flagged. | Test real terrain. Reconcile the README water exemption with L02. |
| S3-02 · Flight | 5 | Flag sustained airborne hover or rise, while accepting ordinary jumps and supported flight states. | F01–F05 passed. A moving hover flagged on move 15 and ascent on move 17. | Review stationary hover in L01 and repeat with a real client. |
| S3-03 · Violation history | 5 | Append each flag to disk and return the newest 20 records after reopening. | H01–H02 passed. The history case saved 25 records and recovered rows 6–25 in order. | Check a complete Paper server restart. |
| S3-04 · Moderator alerts and configuration | 3 | Gate commands and chat by permission, throttle repeats, retain every record and support threshold reload. | C01–C02, A01–A03 and S06 passed. Four flags produced four warnings and three staff alerts. | Verify server permission assignments and real chat delivery. |
| S3-05 · Documentation and demonstration | 2 | Provide repeatable evidence and a narrated walk-through of backlog progress. | Browser check, Java runner, logs, report and two-minute script are available. | Record the team narration and attach the video. |

The fixture supplies player states, permission values and events. The production classes make the decisions and write the files. Team ownership and individual scores await confirmation. Full case outputs appear in the [test transcript](recorded/transcript.txt).
