package me.morpheus.lighteconomy.config;

import me.morpheus.lighteconomy.LightEconomy;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

public final class ConfigUtil {

    static {
        final PluginContainer plugin = Sponge.getPluginManager().getPlugin(LightEconomy.ID).get();
        final ConfigRoot metropolis = Sponge.getConfigManager().getPluginConfig(plugin);

        ROOT = metropolis.getDirectory();
        CONF = metropolis.getConfigPath();
    }


    public static final Path ROOT;
    public static final Path CONF;

    private ConfigUtil() {}
}

