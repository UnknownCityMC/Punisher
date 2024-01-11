package de.unknowncity.ucvelocity.core.configuration.typeserializer;

import de.unknowncity.ucvelocity.core.configuration.Configuration;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ConfigurationTypeSerializer implements TypeSerializer<Configuration> {
    @Override
    public Configuration deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return new Configuration(

        );
    }

    @Override
    public void serialize(Type type, @Nullable Configuration configuration, ConfigurationNode node) throws SerializationException {
        if (configuration != null) {

        }
    }
}
