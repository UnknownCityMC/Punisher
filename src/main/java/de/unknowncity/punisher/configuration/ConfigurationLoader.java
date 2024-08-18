package de.unknowncity.punisher.configuration;

import de.unknowncity.punisher.PunisherPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @deprecated
 * Will be replaced by AstraLib in the near future
 */
@Deprecated
public class ConfigurationLoader {
    private final PunisherPlugin plugin;
    private ConfigurationNode mainConfigRootNode;
    private ConfigurationNode messageConfigRootNode;

    public ConfigurationLoader(PunisherPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads a configuration from a config loader
     * @param configurationLoader the config loader from which the config is to be loaded
     * @return a configuration node representing the root of the config tree
     */
    public Optional<ConfigurationNode> loadConfiguration(org.spongepowered.configurate.loader.ConfigurationLoader<ConfigurationNode> configurationLoader) {
        try {
            return Optional.of(configurationLoader.load());
        } catch (ConfigurateException e) {
            plugin.logger().log(Level.SEVERE, "Failed to load messages.yml", e);
        }
        return Optional.empty();
    }

    /**
     * Saves a config file from resources to plugin data directory
     * @param filePath path to the files location in the resource directory
     */
    public void saveDefaultConfigFile(Path filePath) {
        var completePath = plugin.dataDirectory().resolve(filePath);
        if (Files.exists(completePath) && !PunisherPlugin.IS_DEV_BUILD) {
            return;
        }

        try (var resourceAsStream = getClass().getResourceAsStream("/" + filePath)) {
            if (resourceAsStream == null) {
                plugin.logger().log(
                        Level.SEVERE, "Failed to save " + filePath + ". " +
                        "The plugin developer tried to save a file that does not exist in the plugins jar file!"
                );
                return;
            }
            Files.createDirectories(completePath.getParent());
            try (var outputStream = Files.newOutputStream(completePath)) {
                resourceAsStream.transferTo(outputStream);
            }
        } catch (IOException e) {
            plugin.logger().log(Level.SEVERE, "Failed to save " + filePath, e);
        }
    }

    /**
     * Save a configuration when changes are made through code
     * @param configurationNode a config node to which the changes are to be saved
     * @param configuration the config that changes were made to and that needs to be saved
     * @param configurationLoader the config loader through which the config was loaded
     */
    public void saveConfiguration(
            ConfigurationNode configurationNode,
            Configuration configuration,
            org.spongepowered.configurate.loader.ConfigurationLoader<ConfigurationNode> configurationLoader
    ) {
        try {
            configurationNode.set(configuration);
            configurationLoader.save(configurationNode);
        } catch (ConfigurateException e) {
            plugin.logger().log(Level.SEVERE, "Failed to save configuration", e);
        }
    }
}
