package de.unknowncity.punisher.util;

import de.unknowncity.punisher.PunisherPlugin;
import de.unknowncity.punisher.punishment.Punishment;
import de.unknowncity.punisher.punishment.types.MutePunishment;
import de.unknowncity.punisher.punishment.types.PersistentPunishment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class MuteToChat {
    private final PunisherPlugin plugin;

    public MuteToChat(PunisherPlugin plugin) {
        this.plugin = plugin;
    }

    public void muteToChat(UUID uuid) {
        var jedis = plugin.redisProvider().getNewJedisConnection();
        Optional<Punishment> optionalPunishment = plugin.punishmentService().getCachedActiveMutePlayer(uuid);

        if (optionalPunishment.isEmpty()) {
            jedis.publish(PunisherPlugin.MUTE_MESSAGE_CHANNEL, MuteSerializer.serialize(uuid, LocalDateTime.now()));
            return;
        }

        Punishment mute = optionalPunishment.get();

        var endDate = mute.punishmentDateTime().plus(Duration.ofSeconds(((PersistentPunishment) mute).durationInSeconds()));

        jedis.publish(PunisherPlugin.MUTE_MESSAGE_CHANNEL, MuteSerializer.serialize(uuid, endDate));
    }

    public void muteToChat(MutePunishment mute) {
        var jedis = plugin.redisProvider().getNewJedisConnection();
        var endDate = mute.punishmentDateTime().plus(Duration.ofSeconds(( mute).durationInSeconds()));
        jedis.publish(PunisherPlugin.MUTE_MESSAGE_CHANNEL, MuteSerializer.serialize(mute.playerUniqueId(), endDate));
    }
}
