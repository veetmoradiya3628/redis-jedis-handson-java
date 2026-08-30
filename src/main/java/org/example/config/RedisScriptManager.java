package org.example.config;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.exceptions.JedisDataException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RedisScriptManager {
    private final RedisClient jedis;
    private final String scriptContent;
    private String scriptSha;

    public RedisScriptManager(RedisClient client, String scriptPath) throws IOException {
        this.jedis = client;
        this.scriptContent = loadScriptFromClasspath(scriptPath);
        this.scriptSha = jedis.scriptLoad(this.scriptContent);
        System.out.println("Script loaded initially with SHA: " + this.scriptSha);
    }

    private String loadScriptFromClasspath(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Script file not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public Object execute(List<String> keys, List<String> args) {
        try {
            return jedis.evalsha(scriptSha, keys, args);
        } catch (JedisDataException e) {
            if (e.getMessage().contains("NOSCRIPT")) {
                System.out.println("Cache miss (NOSCRIPT). Reloading script into Redis...");
                // Reload the script to get the SHA again
                this.scriptSha = jedis.scriptLoad(scriptContent);
                // Retry execution
                return jedis.evalsha(scriptSha, keys, args);
            }
            throw e;
        }
    }
}
