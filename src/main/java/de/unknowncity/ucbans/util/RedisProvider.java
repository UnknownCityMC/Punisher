package de.unknowncity.ucbans.util;

import de.unknowncity.ucbans.configuration.settings.RedisSettings;
import redis.clients.jedis.JedisPooled;

public class RedisProvider {
    private final RedisSettings redisSettings;

    public RedisProvider(RedisSettings redisSettings) {
        this.redisSettings = redisSettings;
    }

    public JedisPooled getNewJedisConnection() {
        return new JedisPooled(redisSettings.hostName(), redisSettings.port(), "default", redisSettings.password());
    }

}
