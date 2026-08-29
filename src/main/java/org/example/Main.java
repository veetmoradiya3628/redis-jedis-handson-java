package org.example;

import org.example.config.RedisConnectionManager;
import redis.clients.jedis.RedisClient;

public class Main {
    public static void main(String[] args) {
        RedisClient jedis = RedisConnectionManager.getClient();
        System.out.println("Main ping : " + jedis.ping());
    }
}