package br.com.battlebits.commons.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bson.Document;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.util.GeoIpUtils.IpCityResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class DataPlayer extends Data {

	public static BattlePlayer getPlayer(UUID uuid) {
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			player = getRedisPlayer(uuid);
			if (player == null)
				player = getMongoPlayer(uuid);
		}
		return player;
	}

	public static BattlePlayer getMongoPlayer(UUID uuid) {
		MongoDatabase database = BattlebitsAPI.getCommonsMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("account");

		Document found = collection.find(Filters.eq("uniqueId", uuid.toString())).first();
		if (found == null) {
			return null;
		}
		return BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), BattlePlayer.class);
	}

	public static BattlePlayer createIfNotExistMongo(UUID uuid, String name, String address, IpCityResponse ipResponse) {
		try {
			MongoDatabase database = BattlebitsAPI.getCommonsMongo().getClient().getDatabase("commons");
			MongoCollection<Document> collection = database.getCollection("account");

			Document found = collection.find(Filters.eq("uniqueId", uuid.toString())).first();
			BattlePlayer player = null;
			if (found == null) {
				player = new BattlePlayer(name, uuid, address, ipResponse);
				found = Document.parse(BattlebitsAPI.getGson().toJson(player));
				collection.insertOne(found);
				BattlebitsAPI.debug("MONGO > INSERTED");
			} else {
				player = BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), BattlePlayer.class);
				BattlebitsAPI.debug("MONGO > LOADED");
			}
			return player;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BattlePlayer getRedisPlayer(UUID uuid) {
		BattlePlayer player = null;
		try (Jedis jedis = BattlebitsAPI.getCommonsRedis().getPool().getResource()) {
			if (!jedis.exists("account:" + uuid.toString()))
				return null;
			Map<String, String> fields = jedis.hgetAll("account:" + uuid.toString());
			if (fields == null || fields.isEmpty() || fields.size() < 40)
				return null;
			JsonObject obj = new JsonObject();
			for (Entry<String, String> entry : fields.entrySet()) {
				obj.add(entry.getKey(), BattlebitsAPI.getParser().parse(entry.getValue()));
			}
			player = gson.fromJson(obj.toString(), BattlePlayer.class);
		}
		return player;
	}

	public static BukkitPlayer getMongoBukkitPlayer(UUID uuid) {
		MongoDatabase database = BattlebitsAPI.getCommonsMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("account");

		Document found = collection.find(Filters.eq("uniqueId", uuid.toString())).first();
		if (found == null) {
			return null;
		}
		return BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), BukkitPlayer.class);
	}

	public static void cacheRedisPlayer(UUID uuid) {
		try (Jedis jedis = BattlebitsAPI.getCommonsRedis().getPool().getResource()) {
			BattlebitsAPI.debug("REDIS > EXPIRE 300");
			jedis.expire("account:" + uuid.toString(), 300);
		}
	}

	public static boolean checkCache(UUID uuid) {
		boolean bol = false;
		try (Jedis jedis = BattlebitsAPI.getCommonsRedis().getPool().getResource()) {
			String key = "account:" + uuid.toString();
			if (jedis.ttl(key) >= 0) {
				bol = jedis.persist(key) == 1;
			}
		}
		if (bol)
			BattlebitsAPI.debug("REDIS > SHOULD REMOVE");
		else
			BattlebitsAPI.debug("REDIS > SUB-SERVER");
		return bol;
	}

	public static BukkitPlayer createIfNotExistMongoBukkit(UUID uuid, String name, String address, IpCityResponse response) {
		MongoDatabase database = BattlebitsAPI.getCommonsMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("account");

		Document found = collection.find(Filters.eq("uniqueId", uuid.toString())).first();
		BukkitPlayer player = null;
		if (found == null) {
			player = new BukkitPlayer(name, uuid, address, response);
			found = Document.parse(BattlebitsAPI.getGson().toJson(player));
			collection.insertOne(found);
			BattlebitsAPI.debug("MONGO > INSERTED");
		} else {
			player = BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), BukkitPlayer.class);
			BattlebitsAPI.debug("MONGO > LOADED");
		}
		return player;
	}

	public static BukkitPlayer getRedisBukkitPlayer(UUID uuid) {
		BukkitPlayer player = null;
		try (Jedis jedis = BattlebitsAPI.getCommonsRedis().getPool().getResource()) {
			Map<String, String> fields = jedis.hgetAll("account:" + uuid.toString());
			if (fields == null || fields.isEmpty() || fields.size() < 40)
				return null;
			JsonObject obj = new JsonObject();
			for (Entry<String, String> entry : fields.entrySet()) {
				try {
					obj.add(entry.getKey(), BattlebitsAPI.getParser().parse(entry.getValue()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			player = BattlebitsAPI.getGson().fromJson(obj.toString(), BukkitPlayer.class);
		}
		return player;
	}

	public static void saveRedisPlayer(BattlePlayer player) {
		JsonObject jsonObject = BattlebitsAPI.getParser().parse(gson.toJson(player)).getAsJsonObject();
		Map<String, String> playerElements = new HashMap<>();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			playerElements.put(entry.getKey(), gson.toJson(entry.getValue()));
		}
		try (Jedis jedis = BattlebitsAPI.getCommonsRedis().getPool().getResource()) {
			jedis.hmset("account:" + player.getUniqueId().toString(), playerElements);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BattlebitsAPI.debug("REDIS > SAVE SUCCESS");
	}

	public static void saveBattlePlayer(BattlePlayer player, String fieldName) {
		saveBattleFieldRedis(player, fieldName);
		saveBattleFieldMongo(player, fieldName);
	}

	private static void saveBattleFieldRedis(BattlePlayer player, String fieldName) {
		JsonObject jsonObject = BattlebitsAPI.getParser().parse(gson.toJson(player)).getAsJsonObject();
		if (!jsonObject.has(fieldName))
			return;
		JsonElement element = jsonObject.get(fieldName);
		try (Jedis jedis = BattlebitsAPI.getCommonsRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			jedis.hset("account:" + player.getUniqueId().toString(), fieldName, gson.toJson(element));

			JsonObject json = new JsonObject();
			json.add("uniqueId", new JsonPrimitive(player.getUniqueId().toString()));
			json.add("source", new JsonPrimitive(BattlebitsAPI.getServerId()));
			json.add("field", new JsonPrimitive(fieldName));
			json.add("value", element);
			pipe.publish("account-field", json.toString());

			pipe.sync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveBattleFieldMongo(BattlePlayer player, String fieldName) {
		JsonObject jsonObject = BattlebitsAPI.getParser().parse(BattlebitsAPI.getGson().toJson(player))
				.getAsJsonObject();
		if (!jsonObject.has(fieldName))
			return;
		JsonElement element = jsonObject.get(fieldName);

		Object value = null;
		if (!element.isJsonPrimitive()) {
			value = Document.parse(element.toString());
		} else {
			if (element.getAsJsonPrimitive().isBoolean()) {
				value = element.getAsBoolean();
			} else if (element.getAsJsonPrimitive().isNumber()) {
				try {
					value = Long.parseLong(element.getAsString());
				} catch (Exception e2) {
					try {
						value = Byte.parseByte(element.getAsString());
					} catch (Exception e3) {
						try {
							value = Short.parseShort(element.getAsString());
						} catch (Exception e4) {
							try {
								value = Integer.parseInt(element.getAsString());
							} catch (Exception e5) {
								try {
									value = Double.parseDouble(element.getAsString());
								} catch (Exception e) {
									try {
										value = Float.parseFloat(element.getAsString());
									} catch (Exception e1) {
									}
								}
							}
						}
					}
				}
			} else {
				value = element.getAsString();
			}
		}
		BattlebitsAPI.debug("SAVING MONGO FIELD");
		try {
			MongoDatabase database = BattlebitsAPI.getCommonsMongo().getClient().getDatabase("commons");
			MongoCollection<Document> collection = database.getCollection("account");
			collection.updateOne(Filters.eq("uniqueId", player.getUniqueId().toString()),
					new Document("$set", new Document(fieldName, value)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveConfigField(BattlePlayer player, String fieldName) {
		saveBattleFieldRedis(player, "configuration");
		saveConfigFieldMongo(player, fieldName);
	}

	private static void saveConfigFieldMongo(BattlePlayer player, String fieldName) {
		JsonObject jsonObject = BattlebitsAPI.getParser()
				.parse(BattlebitsAPI.getGson().toJson(player.getConfiguration())).getAsJsonObject();
		if (!jsonObject.has(fieldName))
			return;
		JsonElement element = jsonObject.get(fieldName);
		Object value = null;
		if (!element.isJsonPrimitive()) {
			value = Document.parse(element.toString());
		} else {
			if (element.getAsJsonPrimitive().isBoolean()) {
				value = element.getAsBoolean();
			} else if (element.getAsJsonPrimitive().isNumber()) {
				try {
					value = Long.parseLong(element.getAsString());
				} catch (Exception e2) {
					try {
						value = Byte.parseByte(element.getAsString());
					} catch (Exception e3) {
						try {
							value = Short.parseShort(element.getAsString());
						} catch (Exception e4) {
							try {
								value = Integer.parseInt(element.getAsString());
							} catch (Exception e5) {
								try {
									value = Double.parseDouble(element.getAsString());
								} catch (Exception e) {
									try {
										value = Float.parseFloat(element.getAsString());
									} catch (Exception e1) {
									}
								}
							}
						}
					}
				}
			} else {
				value = element.getAsString();
			}
		}

		MongoDatabase database = BattlebitsAPI.getCommonsMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("account");
		collection.updateOne(Filters.eq("uniqueId", player.getUniqueId().toString()),
				new Document("$set", new Document("configuration." + fieldName, value)));
	}

	public static void saveCompleteBattlePlayer(BattlePlayer player) {
		// TODO
	}
}
