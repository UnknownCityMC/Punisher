package de.unknowncity.ucbans.configuration.typeserializer;

import de.unknowncity.ucbans.configuration.Configuration;
import de.unknowncity.ucbans.configuration.settings.DataBaseSettings;
import de.unknowncity.ucbans.configuration.settings.TemplateSettings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ConfigurationTypeSerializer implements TypeSerializer<Configuration> {
    @Override
    public Configuration deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var databaseSettings = node.node("database").get(DataBaseSettings.class);
        var templateSettings = node.node("templates").get(TemplateSettings.class);

        return new Configuration(
                databaseSettings,
                templateSettings
        );
    }

    @Override
    public void serialize(Type type, @Nullable Configuration configuration, ConfigurationNode node) throws SerializationException {
        if (configuration != null) {

        }
    }
}
