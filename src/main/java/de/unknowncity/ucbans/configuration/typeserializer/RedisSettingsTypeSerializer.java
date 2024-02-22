package de.unknowncity.ucbans.configuration.typeserializer;

import de.unknowncity.ucbans.configuration.settings.RedisSettings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class RedisSettingsTypeSerializer implements TypeSerializer<RedisSettings> {
    @Override
    public RedisSettings deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var hostName = node.node("host").getString();
        var port = node.node("port").getInt();
        var password = node.node("password").getString();

        return new RedisSettings(hostName, port, password);
    }

    @Override
    public void serialize(Type type, @Nullable RedisSettings obj, ConfigurationNode node) throws SerializationException {

    }
}
