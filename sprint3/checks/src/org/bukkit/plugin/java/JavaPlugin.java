package org.bukkit.plugin.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public abstract class JavaPlugin implements Plugin {
    private Path folder;
    private Server server;
    private Logger logger;
    private FileConfiguration config;

    public final void attachFixture(Path folder, Server server, Logger logger) {
        this.folder = folder;
        this.server = server;
        this.logger = logger;
    }

    public final File getDataFolder() { return folder.toFile(); }
    public final Server getServer() { return server; }
    public final Logger getLogger() { return logger; }
    public final FileConfiguration getConfig() { return config; }

    public final void saveDefaultConfig() {
        Path file = folder.resolve("config.yml");
        try {
            Files.createDirectories(folder);
            if (!Files.exists(file)) {
                try (InputStream stream = getClass().getResourceAsStream("/reviewed/config.yml")) {
                    Files.copy(Objects.requireNonNull(stream), file);
                }
            }
            reloadConfig();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public final void reloadConfig() {
        try {
            config = new FileConfiguration(folder.resolve("config.yml"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void onEnable() {}
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}
