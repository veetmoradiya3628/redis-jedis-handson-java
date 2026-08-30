package org.example;

import org.example.config.RedisConnectionManager;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.List;

public class Main {

    // set, get, del
    private static void testStringCommands(RedisClient jedis){
        for (int i = 1; i <= 10; i++) {
            String key = "test:name:" + i;
            String value = "Veet_" + i;
            jedis.set(key, value, SetParams.setParams().ex(i));
        }

        for (int i = 1; i <= 11; i++) {
            String value = jedis.get("test:name:" + i);
            System.out.println("value for i = "+ i + " is = " + value);
        }

//        for (int i = 1; i <= 11; i++) {
//            String key = "test:name:" + i;
//            jedis.del(key);
//        }

//        jedis.set("test:name:1", "Veet");
//        System.out.println("Stored <K,V> pair");
//        String name = jedis.get("test:name:1");
//        System.out.println("got name: " + name);
    }

    // mset, mget, del
    private static void testStringMultiCommand(RedisClient jedis){
        List<String> inputs = new ArrayList<>();
        for (int i = 1; i <=10; i++) {
            inputs.add("test:bulk:" + i);
            inputs.add("veet_" + i);
        }
        String[] msetArgs = inputs.toArray(new String[0]);
        jedis.mset(msetArgs);

        List<String> keys = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            keys.add("test:bulk:" + i);
        }
        String[] mgetArgs = keys.toArray(new String[0]);
        List<String> values = jedis.mget(mgetArgs);
        System.out.println("Values : " + values);

        jedis.del(mgetArgs);
    }

    private static void testBitCommand(RedisClient jedis){

    }

    public static void main(String[] args) {
        RedisClient jedis = RedisConnectionManager.getClient();
        System.out.println("Main ping : " + jedis.ping());

//        testStringCommands(jedis);
//        testStringMultiCommand(jedis);

    }
}

