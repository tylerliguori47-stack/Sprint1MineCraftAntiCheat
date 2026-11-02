# Local server startup and demonstration

These are **reconstruction instructions**, not a recovered command log from the user's machine. Exact startup JAR filename, Paper version, Java version, port, and existing file paths were **not preserved** in the provided Sprint 1 report.

## 1. Locate your existing server directory

This is the folder with the Paper server JAR, `server.properties`, `plugins/`, and world folders. **Do not upload an entire server installation, world, player data or private configuration into GitHub.**

## 2. Start the server

In a Windows PowerShell terminal inside the server folder:

```powershell
java -version
java -Xms1G -Xmx2G -jar paper-YOUR_VERSION.jar nogui
```

Replace `paper-YOUR_VERSION.jar` with the **exact filename you own**. Your original startup command may have used different RAM arguments; do not describe this example as your original command.

Wait until the console reports that startup is complete.

## 3. Connect in Minecraft

- Open the matching Minecraft Java Edition version.
- Multiplayer → Direct Connection → `localhost`.
- Enter the world. The player connection should appear in the server console.

## 4. Confirm whitelist and plugin

At the **server console** (omit leading `/`):

```text
whitelist list
plugins
```

In **Minecraft chat**, enter:

```text
/plugins
```

The Sprint 1 screenshot showed a plugin named `MinecraftAntiCheat` in the output. Commands listed here are demonstrations and may require permissions.

## 5. Build/test the newly provided example source (optional)

```powershell
cd plugin
mvn clean package
```

JAR output: `target/MinecraftAntiCheat-0.1.0-SNAPSHOT.jar`. Stop the server, back up and remove any **previous copy of the same plugin** from its `plugins/` folder, then copy the newly built JAR into that folder. Restart and test `/anticheat status`.

The example plugin is not the original code. It does not detect cheats.

## 6. Evidence for GitHub

Capture separate redacted screenshots of startup completion, successful localhost join, whitelist command and `/plugins`. Keep logs free of access tokens, sensitive paths and private player information.
