package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.RedisClient;

public class RedisConnectionManager {
    private static RedisClient redisClient;

    private RedisConnectionManager() {
    }

    public static synchronized RedisClient getClient() {
        if (redisClient == null) {
            Dotenv dotenv = Dotenv.load();

            String host = dotenv.get("REDIS_HOST");
            int port = Integer.parseInt(dotenv.get("REDIS_PORT"));
            String password = dotenv.get("REDIS_PASSWORD");
            String user = dotenv.get("REDIS_USER");

            JedisClientConfig config = DefaultJedisClientConfig.builder()
                    .user(user)
                    .password(password)
                    .build();

            redisClient = RedisClient.builder()
                    .hostAndPort(host, port)
                    .clientConfig(config)
                    .build();
        }
        return redisClient;
    }

    public static synchronized void close() {
        if (redisClient != null) {
            redisClient.close();
            redisClient = null;
        }
    }
}
