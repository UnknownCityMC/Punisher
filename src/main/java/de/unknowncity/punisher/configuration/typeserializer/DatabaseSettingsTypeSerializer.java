package de.unknowncity.punisher.configuration.typeserializer;

import de.unknowncity.punisher.configuration.settings.DataBaseSettings;
import de.unknowncity.punisher.data.database.DBDriver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class DatabaseSettingsTypeSerializer implements TypeSerializer<DataBaseSettings> {
    @Override
    public DataBaseSettings deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var driver = DBDriver.valueOf(Objects.requireNonNull(node.node("driver").getString()).toUpperCase());
        var port = node.node("port").getInt();
        var host = node.node("host").getString();
        var username = node.node("username").getString();
        var db = node.node("db").getString();
        var password = node.node("password").getString();

        var maxPoolSize = node.node("hikari", "max-pool-size").getInt();
        var minIdleConnections = node.node("hikari", "min-idle-connections").getInt();

        return new DataBaseSettings(
                driver,
                port,
                host,
                db,
                username,
                password,
                maxPoolSize,
                minIdleConnections
        );
    }

    @Override
    public void serialize(Type type, @Nullable DataBaseSettings obj, ConfigurationNode node) throws SerializationException {
        // Config currently does not need to be saved (Replace with AstraLib when it's ready for Velocity)
    }
}
