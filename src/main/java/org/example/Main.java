package org.example;

import org.example.config.RedisConnectionManager;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static void testBitCommand(RedisClient jedis) {
        String key = "active_users:2026-08-30";

        // userId, active/not_active
        jedis.setbit(key, 1, true);
        jedis.setbit(key, 2, true);
        jedis.setbit(key, 1001, true);
        jedis.setbit(key, 5050, true);
        jedis.setbit(key, 9999, true);

        System.out.println("Is user 1001 active : " + jedis.getbit(key, 1001));
        System.out.println("Is user 1002 active : " + jedis.getbit(key, 1002));

        long totalActiveUsers = jedis.bitcount(key);
        System.out.println("Total active users : " + totalActiveUsers);
    }

    // hset, hget, hgetAll
    private static void testHashCommand(RedisClient jedis){
        String key = "users:profile:user1";
        Map<String, String> profileData = new HashMap<>();
        profileData.put("name", "veet");
        profileData.put("age", "24");
        profileData.put("gender", "male");

        long resp1 = jedis.hset(key, profileData);
        System.out.println("response1 : "+ resp1);

        profileData.put("occupation", "software engineer");
        long resp2 = jedis.hset(key, profileData);
        System.out.println("response2 : "+ resp2);

        String returnAge = jedis.hget(key, "age");
        System.out.println("returned Age " + returnAge);

        String returnUnknown = jedis.hget(key, "unknown_field");
        System.out.println("returned Unknown " + returnUnknown);

        Map<String, String> returnProfile = jedis.hgetAll(key);
        System.out.println("returned Profile " + returnProfile);

        jedis.hdel(key, "age");
        returnProfile = jedis.hgetAll(key);
        System.out.println("returned Profile " + returnProfile);

        jedis.del(key);
        returnProfile = jedis.hgetAll(key);
        System.out.println("returned Profile " + returnProfile);
    }

    // lpush, rpush, lpop, rpop, lrange, llen
    private static void testListCommand(RedisClient jedis){
        String key = "message:svc1";
        for (int i = 0; i < 5; i++) {
            jedis.lpush(key, String.valueOf(i));
        }

        List<String> values = jedis.lrange(key, 0, -1);
        System.out.println("Values : " + values);

        long lengthOfItems = jedis.llen(key);
        System.out.println("Length: " + lengthOfItems);

        for (int i = 0; i < 5; i++) {
            jedis.rpush(key, String.valueOf(10 - i));
        }
        lengthOfItems = jedis.llen(key);
        System.out.println("Length post addition: " + lengthOfItems);

        jedis.lpop(key);
        lengthOfItems = jedis.llen(key);
        System.out.println("Length post addition: " + lengthOfItems);

        jedis.rpop(key);
        lengthOfItems = jedis.llen(key);
        System.out.println("Length post addition: " + lengthOfItems);

        jedis.del(key);
    }

    public static void main(String[] args) {
        RedisClient jedis = RedisConnectionManager.getClient();
        System.out.println("Main ping : " + jedis.ping());

//        testStringCommands(jedis);
//        testStringMultiCommand(jedis);
//        testBitCommand(jedis);
//        testHashCommand(jedis);
        testListCommand(jedis);


    }
}

