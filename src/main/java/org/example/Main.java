package org.example;

import org.example.config.RedisConnectionManager;
import redis.clients.jedis.*;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.ZRangeParams;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.StreamEntry;

import java.util.*;

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

    // sadd, smembers, srem, sismember, scard, sinter, sunion
    private static void testSetCommand(RedisClient jedis){
        String key = "tags:article:1";
        String[] tags = new String[]{"Java", "Programming", "System Engineering", "DevOps", "Java"};

        jedis.sadd(key, tags);

        long sizeOfSet = jedis.scard(key);
        System.out.println("Items in set : " + sizeOfSet);

        System.out.println("Is Coding part of set : " + jedis.sismember(key, "Coding"));
        System.out.println("Is Java part of set : " + jedis.sismember(key, "Java"));

        String key2 = "tags:article:2";
        String[] article2Tags = new String[]{"Java", "Spring boot", "Spring Data JPA", "DevOps"};
        jedis.sadd(key2, article2Tags);

        System.out.println("Intersection of both articles : " + jedis.sinter(key, key2));
        System.out.println("Union of both articles : " + jedis.sunion(  key, key2));

        jedis.srem(key, new String[]{"Java", "Coding"});
        System.out.println("Item in article1 tags : " + jedis.scard(key));

        jedis.del(key);
        jedis.del(key2);
    }

    // zadd, zrange, zrevrange (deprecated), zrem, zscore, zcard
    private static void testSortedSetCommand(RedisClient jedis){
        String key = "leaderboard";

        jedis.zadd(key, 1000, "player1");
        jedis.zadd(key, 900, "player2");
        jedis.zadd(key, 1100, "player3");

        System.out.println("leaderboard : " + jedis.zrange(key, 0, -1));
        System.out.println("leaderboard reverse order : " + jedis.zrange(key, new ZRangeParams(0, -1).rev()));

        jedis.zadd(key, 800, "player4");
        System.out.println("leaderboard : " + jedis.zrange(key, 0, -1));

        jedis.zrem(key, "player4");
        System.out.println("After removing Player4 leaderboard : " + jedis.zrange(key, 0, -1));

        jedis.zrem(key, "player2");
        System.out.println("After removing Player2 leaderboard : " + jedis.zrange(key, 0, -1));

    }

    // pfadd, pfcount
    private static void testHyperLogLog(RedisClient jedis) {
        String key = "unique_visitors:2026-08-30";
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add("user_" + String.valueOf(i));
        }
        jedis.pfadd(key, items.toArray(new String[0]));
        System.out.println("Total users : " + jedis.pfcount(key));
        jedis.del(key);
    }

    private static void testGeoCommands(RedisClient jedis){
        String geoKey = "bengaluru:landmarks";

        System.out.println("Populating geospatial data...");
        jedis.geoadd(geoKey, 77.6200, 13.0475, "Manyata Tech Park");
        jedis.geoadd(geoKey, 77.6223, 13.0454, "Lumbini Gardens");
        jedis.geoadd(geoKey, 77.5853, 13.0413, "Hebbal Lake");
        jedis.geoadd(geoKey, 77.5921, 12.9985, "Bangalore Palace");
        jedis.geoadd(geoKey, 77.6083, 12.9822, "Commercial Street");
        jedis.geoadd(geoKey, 77.5511, 13.0098, "ISKCON Temple");

        System.out.println("\nSearching for nearby places...");

        GeoSearchParam searchParams = new GeoSearchParam()
                .fromMember("Manyata Tech Park") // Center of the search
                .byRadius(15, GeoUnit.KM)        // Search radius (15 kilometers)
                .withDist()                      // Return distance from center
                .withCoord()                     // Return the coordinates
                .asc()                           // Sort nearest to farthest
                .count(6);                       // Limit results (5 places + Manyata)

        List<GeoRadiusResponse> nearbyPlaces = jedis.geosearch(geoKey, searchParams);
        System.out.printf("%-20s | %-12s | %s%n", "Location", "Distance", "Coordinates (Lon, Lat)");
        System.out.println("------------------------------------------------------------------");

        for (GeoRadiusResponse place : nearbyPlaces) {
            String name = place.getMemberByString();
            double distance = place.getDistance();
            double lon = place.getCoordinate().getLongitude();
            double lat = place.getCoordinate().getLatitude();

            // Skip printing Manyata Tech Park's distance to itself (0.0 km) if desired
            if (name.equals("Manyata Tech Park")) {
                System.out.printf("%-20s | %-12s | [%.4f, %.4f] (Origin)%n", name, "0.0000 km", lon, lat);
            } else {
                System.out.printf("%-20s | %.4f km    | [%.4f, %.4f]%n", name, distance, lon, lat);
            }
        }

        // Clean up test data
        jedis.del(geoKey);
    }

    private static void testRedisStreams(RedisClient jedis){
        String streamKey = "app:notifications";
        String groupName = "notification-workers";
        String consumerName = "worker-1";

        jedis.del(streamKey);

        System.out.println("1. Initializing Consumer Group...");
        jedis.xgroupCreate(streamKey, groupName, StreamEntryID.XGROUP_LAST_ENTRY, true);

        System.out.println("2. Publishing events to stream...");
        Map<String, String> event1 = new HashMap<>();
        event1.put("type", "EMAIL_WELCOME");
        event1.put("user_id", "1001");
        jedis.xadd(streamKey, StreamEntryID.NEW_ENTRY, event1);

        Map<String, String> event2 = new HashMap<>();
        event2.put("type", "SMS_ALERT");
        event2.put("user_id", "5050");
        jedis.xadd(streamKey, StreamEntryID.NEW_ENTRY, event2);

        System.out.println("   -> Messages appended successfully.\n");
        System.out.println("3. Consumer '" + consumerName + "' checking for work...");

        Map<String, StreamEntryID> streamQuery = Collections.singletonMap(streamKey, StreamEntryID.XREADGROUP_UNDELIVERED_ENTRY);
        XReadGroupParams readParams = XReadGroupParams.xReadGroupParams()
                .count(10)      // Fetch up to 10 messages at once
                .block(2000);   // Block/wait for up to 2 seconds if stream is empty

        List<Map.Entry<String, List<StreamEntry>>> results = jedis.xreadGroup(groupName, consumerName, readParams, streamQuery);

        if (results != null && !results.isEmpty()) {
            for (Map.Entry<String, List<StreamEntry>> stream : results) {
                List<StreamEntry> entries = stream.getValue();

                for (StreamEntry entry : entries) {
                    System.out.println("   [RECEIVED] Message ID: " + entry.getID());
                    System.out.println("   [PAYLOAD]  " + entry.getFields());

                    // Simulate work...
                    System.out.println("     -> Processing " + entry.getFields().get("type") + " for user " + entry.getFields().get("user_id"));

                    // XACK removes the message from the consumer's Pending Entries List (PEL)
                    jedis.xack(streamKey, groupName, entry.getID());
                    System.out.println("     -> Acknowledged (XACK)\n");
                }
            }
        } else {
            System.out.println("   No new messages found.");
        }

        // Cleanup test data
        jedis.del(streamKey);
    }

    private static void testPipelining(RedisClient jedis){
        Map<String, String> mpp = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            mpp.put("Key_" + i, "Value_" + i);
        }

        Pipeline pipeline = jedis.pipelined();

        for(Map.Entry<String, String> entry: mpp.entrySet()){
            pipeline.set(entry.getKey(), entry.getValue());
        }

        pipeline.sync();

        for (String key: mpp.keySet()){
            pipeline.del(key);
        }
        pipeline.sync();
    }

    // transaction, watch
    private static void testTransactions(RedisClient jedis){
        String accBob = "account:bob";
        String accAlice = "account:alice";

        jedis.set(accBob, "100");
        jedis.set(accAlice, "100");
        try (AbstractTransaction trans = jedis.transaction(false)) {
            trans.watch(accBob);
            trans.multi();
            trans.decrBy(accBob, 50);
            trans.incrBy(accAlice, 50);

            List<Object> results = trans.exec();
            if (results == null) {
                System.out.println("Transaction Aborted! The watched key was modified.");
            } else {
                System.out.println("Transaction Results: " + results);
            }
        }

        jedis.del(accBob);
        jedis.del(accBob);
    }



    public static void main(String[] args) {
        RedisClient jedis = RedisConnectionManager.getClient();
        System.out.println("Main ping : " + jedis.ping());

//        testStringCommands(jedis);
//        testStringMultiCommand(jedis);
//        testBitCommand(jedis);
//        testHashCommand(jedis);
//        testListCommand(jedis);
//        testSetCommand(jedis);
//        testSortedSetCommand(jedis);
//        testHyperLogLog(jedis);
//        testGeoCommands(jedis);
//        testRedisStreams(jedis);
//        testPipelining(jedis);
        testTransactions(jedis);
    }
}

