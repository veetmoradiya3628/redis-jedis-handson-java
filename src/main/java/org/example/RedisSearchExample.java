package org.example;

import org.example.config.RedisConnectionManager;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.schemafields.*;

public class RedisSearchExample {
    public static void main(String[] args) {
        RedisClient jedis = RedisConnectionManager.getClient();
        System.out.println("Main ping : " + jedis.ping());

        SchemaField[] schema = {
                TextField.of("$.name").as("name"),
                TagField.of("$.category").as("category"),
                NumericField.of("$.price").as("price")
        };

        FTCreateParams indexParams = FTCreateParams.createParams()
                .on(IndexDataType.JSON)
                .addPrefix("item:");

        try {
            jedis.ftCreate("itemIdx", indexParams, schema);
            System.out.println("Index created successfully.");
        } catch (Exception e) {
            // Redis throws an exception if the index already exists
            System.out.println("Index already exists or error: " + e.getMessage());
        }
    }
}
