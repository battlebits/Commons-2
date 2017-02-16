package br.com.battlebits.commons.core.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bson.Document;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.clan.Clan;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class DataClan extends Data {

	public static Clan getClan(UUID uuid) {
		Clan clan = BattlebitsAPI.getClanCommon().getClan(uuid);
		if (clan == null) {
			clan = getRedisClan(uuid);
			if (clan == null)
				clan = getMongoClan(uuid);
		}
		return clan;
	}

	public static Clan getClan(String name) {
		Clan clan = BattlebitsAPI.getClanCommon().getClan(name);
		if (clan == null) {
			clan = getRedisClan(name);
			if (clan == null) {
				clan = getMongoClan(name);
			}
		}
		return clan;
	}

	public static Clan getMongoClan(UUID uuid) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("uniqueId", uuid)).first();
		if (found == null)
			return null;
		return BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), Clan.class);
	}

	public static Clan getMongoClan(String name) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("name", name)).first();
		if (found == null)
			return null;
		return BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), Clan.class);
	}

	public static Clan getRedisClan(String name) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			String str = jedis.get("clan:uniqueId:" + name);
			if (str != null && !str.isEmpty()) {
				return getRedisClan(UUID.fromString(str));
			}
		}
		return null;
	}

	public static Clan getRedisClan(UUID uniqueId) {
		Clan clan = null;
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			if (!jedis.exists("account:" + "clan:" + uniqueId.toString()))
				return null;
			Map<String, String> fields = jedis.hgetAll("clan:" + uniqueId.toString());
			if (fields == null || fields.isEmpty())
				return null;
			JsonObject obj = new JsonObject();
			for (Entry<String, String> entry : fields.entrySet()) {
				obj.add(entry.getKey(), BattlebitsAPI.getParser().parse(entry.getValue()));
			}
			clan = BattlebitsAPI.getGson().fromJson(obj.toString(), Clan.class);
		}
		return clan;
	}

	public static void saveClanField(Clan clan, String fieldName) {
		saveMongoClanField(clan, fieldName);
		saveRedisClanField(clan, fieldName);
	}

	public static void saveMongoClanField(Clan clan, String fieldName) {
		JsonObject jsonObject = BattlebitsAPI.getParser().parse(BattlebitsAPI.getGson().toJson(clan)).getAsJsonObject();
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
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		collection.updateOne(Filters.eq("uniqueId", clan.getUniqueId().toString()),
				new Document("$set", new Document(fieldName, value)));
	}

	public static void saveRedisClanField(Clan clan, String fieldName) {
		JsonObject jsonObject = BattlebitsAPI.getParser().parse(gson.toJson(clan)).getAsJsonObject();
		if (!jsonObject.has(fieldName))
			return;
		JsonElement element = jsonObject.get(fieldName);
		String value;
		if (!element.isJsonPrimitive()) {
			value = element.toString();
		} else {
			value = element.getAsString();
		}
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			jedis.hset("clan:" + clan.getUniqueId().toString(), fieldName, value);

			JsonObject json = new JsonObject();
			json.add("uniqueId", new JsonPrimitive(clan.getUniqueId().toString()));
			json.add("source", new JsonPrimitive(BattlebitsAPI.getServerId()));
			json.add("field", new JsonPrimitive(fieldName));
			json.add("value", element);
			pipe.publish("clan-field", json.toString());

			pipe.sync();
		}
	}

	public static void saveMongoClan(Clan clan) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("uniqueId", clan.getUniqueId())).first();
		if (found == null) {
			collection.insertOne(Document.parse(BattlebitsAPI.getGson().toJson(clan)));
		} else {
			collection.findOneAndUpdate(Filters.eq("uniqueId", clan.getUniqueId()),
					Document.parse(BattlebitsAPI.getGson().toJson(clan)));
		}
	}

	public static void saveRedisClan(Clan clan) {
		JsonObject jsonObject = BattlebitsAPI.getParser().parse(gson.toJson(clan)).getAsJsonObject();
		Map<String, String> playerElements = new HashMap<>();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value;
			if (!entry.getValue().isJsonPrimitive()) {
				value = entry.getValue().toString();
			} else {
				value = entry.getValue().getAsString();
			}
			playerElements.put(key, value);
		}

		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.hmset("clan:" + clan.getUniqueId().toString(), playerElements);
			pipe.set("clan:uniqueId:" + clan.getName(), clan.getName());

			jedis.sync();
		}
	}

	public static boolean clanNameExists(String clanName) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("clanName", clanName)).first();
		return found != null;
	}

	public static boolean clanAbbreviationExists(String abbreviation) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("abbreviation", abbreviation)).first();
		return found != null;
	}

	public static void disbandMongoClan(Clan clan) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		collection.deleteOne(Filters.eq("uniqueId", clan.getUniqueId()));
	}

	public static void disbandRedisClan(Clan clan) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.del("clan:" + clan.getUniqueId().toString());
			pipe.del("clan:uniqueId:" + clan.getName());

			jedis.sync();
		}
	}

	public static void cacheRedisClan(UUID uuid, String clanName) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			BattlebitsAPI.debug("REDIS > EXPIRE 300");
			jedis.expire("clan:" + uuid.toString(), 300);
			jedis.expire("clan:uniqueId:" + clanName, 300);
		}
	}

	public static boolean checkCache(UUID uuid) {
		boolean bol = false;
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			String key = "clan:" + uuid.toString();
			bol = jedis.persist(key) == 1;
		}
		if (bol)
			BattlebitsAPI.debug("REDIS > SHOULD REMOVE");
		else
			BattlebitsAPI.debug("REDIS > SUB-SERVER");
		return bol;
	}

	public static UUID getNewUniqueId() {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		UUID uuid;
		do {
			uuid = UUID.randomUUID();
		} while (collection.find(Filters.eq("uniqueId", uuid.toString())).first() != null);
		return uuid;
	}

}
