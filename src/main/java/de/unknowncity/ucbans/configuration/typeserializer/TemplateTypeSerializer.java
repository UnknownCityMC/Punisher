package de.unknowncity.ucbans.configuration.typeserializer;

import de.unknowncity.ucbans.punishment.PunishmentLevel;
import de.unknowncity.ucbans.punishment.PunishmentTemplate;
import de.unknowncity.ucbans.punishment.PunishmentType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

public class TemplateTypeSerializer implements TypeSerializer<PunishmentTemplate> {
    @Override
    public PunishmentTemplate deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var reason = node.node("reason").getString();
        var levelNodes = node.node("levels").childrenMap();

        var levels = levelNodes.keySet().stream().map(key -> new PunishmentLevel(
                Integer.parseInt(key.toString()),
                levelNodes.get(key).node("duration").getInt(),
                PunishmentType.valueOf(levelNodes.get(key).node("type").getString()))).collect(Collectors.toSet());

        return new PunishmentTemplate(reason, levels);
    }

    @Override
    public void serialize(Type type, @Nullable PunishmentTemplate punishmentTemplate, ConfigurationNode node) throws SerializationException {

    }
}
