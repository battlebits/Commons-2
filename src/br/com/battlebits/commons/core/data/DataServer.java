package br.com.battlebits.commons.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class DataServer {
	// TRANSLATIONS
	@SuppressWarnings("unchecked")
	public static Map<String, String> loadTranslation(Language language) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("translation");
		Document found = collection.find(Filters.eq("language", language.toString())).first();
		if (found != null) {
			return (Map<String, String>) found.get("map");
		}
		collection.insertOne(new Document("language", language.toString()).append("map", new HashMap<>()));
		return new HashMap<>();
	}

	public static void addTranslationTag(Language language, String tag) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("translation");
		Document found = collection.find(Filters.eq("language", language.toString())).first();
		if (found != null) {
			collection.updateOne(Filters.eq("language", language.toString()),
					new Document("$set", new Document("map." + tag, tag.replace("-", " "))));
		} else {
			HashMap<String, String> str = new HashMap<>();
			str.put(tag, "[NOT FOUND: '" + tag + "']");
			collection.insertOne(new Document("language", language.toString()).append("map", str));
		}
	}

	// SERVER STATUS

	public static String getServerId(String ipAddress) {
		try {
			MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
			MongoCollection<Document> collection = database.getCollection("serverId");
			Document found = collection.find(Filters.eq("address", ipAddress)).first();
			if (found != null) {
				return found.getString("hostname");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ipAddress;
	}

	private static long EXPIRE_TIME = Long.MIN_VALUE;

	public static void newServer(ServerType serverType, String serverId) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.sadd("server:type:" + serverType.toString().toLowerCase(), serverId);
			pipe.del("server:" + serverId + ":players");
			pipe.sync();
		}
	}

	public static void stopServer(ServerType serverType, String serverId) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.srem("server:type:" + serverType.toString().toLowerCase(), serverId);
			pipe.del("server:" + serverId + ":players");
			pipe.sync();
		}
	}

	public static void joinPlayer(String serverId, UUID uuid) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.sadd("server:" + serverId + ":players", uuid.toString());
			pipe.expire("server:" + serverId + ":players", 300);
			EXPIRE_TIME = System.currentTimeMillis() + 300000;
			pipe.sync();
		}
	}

	public static void leavePlayer(String serverId, UUID uuid) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.srem("server:" + serverId + ":players", uuid.toString());
			pipe.expire("server:" + serverId + ":players", 300);
			EXPIRE_TIME = System.currentTimeMillis() + 300000;
			pipe.sync();
		}
	}

	public static long getPlayerCount(String serverId) {
		long number = 0l;
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			number = jedis.scard("server:" + serverId + ":players");
		}
		return number;
	}

	public static long getPlayerCount(ServerType serverType) {
		long number = 0l;
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Set<String> servers = jedis.smembers("server:type:" + serverType.toString().toLowerCase());
			for (String serverId : servers) {
				number += jedis.scard("server:" + serverId + ":players");
			}
		}
		return number;
	}

	public static long getTimeToExpire() {
		return EXPIRE_TIME - System.currentTimeMillis();
	}

}
