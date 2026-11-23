# CS 370 Minecraft AntiCheat · Sprint 3

This branch contains the team's Sprint 3 Paper plugin and an offline demonstration. The plugin watches for repeated speed and flight anomalies, alerts moderators and saves the latest violations for review.

**November 23 test run:** 18 offline checks passed. Two scenarios reproduced limits. The four original Java classes processed simulated player inputs and wrote real log files. The November 20 Windows Maven build succeeded. Startup and gameplay on Paper 1.21.4 with this Sprint 3 JAR still need a live check.

## Try the demo with a browser

1. [Download this branch as a ZIP](https://github.com/tylerliguori47-stack/Sprint1MineCraftAntiCheat/archive/refs/heads/sprint-3.zip), then extract it.
2. Open `sprint3/OPEN-RESULTS.html` in a browser. You can also download just the [standalone HTML file](sprint3/OPEN-RESULTS.html?raw=true).
3. Click **Run log check**. It compares 25 captured disk records with the 20 rows actually returned by the recent command after reopening the data folder. Expand **S02**, **F02** and **H01** to follow movement through detection, chat and saved history.

The browser check works offline without Minecraft, Java or administrator access. To execute all 20 Java scenarios again, run `java -jar sprint3-checks.jar` from the extracted `sprint3` folder with Java 21, or double-click `RUN-TESTS.cmd` on Windows. A fresh run writes its own report and logs under `runs`.

## Sprint 3 materials

| Material | Contents |
| --- | --- |
| [Sprint backlog](sprint3/BACKLOG.md) | Five stories and 20 planned story points, with acceptance criteria, evidence and remaining work |
| [Presentation and report (PDF)](sprint3/CS370_Sprint3_Backlog_and_Test_Report.pdf) · [editable report (Word)](sprint3/CS370_Sprint3_Backlog_and_Test_Report.docx) | Backlog, test results, build evidence and a two-minute demonstration script |
| [Original plugin source](sprint3/Sprint3_MinecraftAntiCheat/plugin) | Java classes, Maven build and Paper configuration |
| [Test scenarios](sprint3/checks/src/review/Scenarios.java) · [recorded output](sprint3/recorded/transcript.txt) | Inputs, assertions and results for every scenario |
| [Saved log](sprint3/recorded/H02/plugins/MinecraftAntiCheat/violations.log) · [recent command output](sprint3/recorded/H02/recent-after-reopen.txt) | The two files compared by the browser demo |
| [Setup and reproduction](sprint3/START-HERE.txt) | Portable Java instructions, feature-specific runs and rebuilding the test runner |

## Observed behavior

- **Speed.** Ordinary travel produced zero flags. Two consecutive samples above the 8 blocks/s threshold produced a SPEED record. The reloaded 2 blocks/s threshold flagged the same 4 blocks/s input.
- **Flight.** A level airborne path flagged on its 15th position-changing move and sustained ascent on its 17th. Four jump-shaped traces produced zero flags.
- **Moderator controls.** The handler denied all three admin commands to an unauthorized sender. Four flag calls produced four disk records and three staff alerts because repeated chat was throttled.
- **Two limits.** Forty events with identical airborne coordinates produced zero flags. In a modeled water block, a fast-moving player with the swimming state off triggered SPEED. The swimming control was exempt.

The original nine project files are preserved in the Sprint 3 source directory. The review adds test fixtures, a desktop runner, captured evidence, documentation and the browser demo. The plugin uses alert-only enforcement.

The Sprint 3 source builds on the [team's Sprint 1 scaffold](https://github.com/tylerliguori47-stack/Sprint1MineCraftAntiCheat/tree/main) and credits [SJ-coder7's Sprint 2 movement checks](https://github.com/SJ-coder7/Sprint-2-Anti-Cheats-Flight-and-Speed).
