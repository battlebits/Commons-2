package br.com.battlebits.commons.core.data;

import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.party.Party;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class DataParty extends Data
{
	public static <T> T getRedisParty(UUID owner, Class<T> clazz)
	{
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource())
		{
			if (!jedis.exists("party:" + owner.toString())) return null;
			
			Map<String, String> data = jedis.hgetAll("party:" + owner.toString());
			
			if (data != null && !data.isEmpty() && data.size() >= 2)
			{
				return parseRedisTree(data, clazz);
			}
		}
		
		return null;
	}
	
	public static void loadParty(Party party)
	{
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) 
		{
			Pipeline pipeline = jedis.pipelined();
			
			JsonObject publish = new JsonObject();
			publish.addProperty("action", "load");
			publish.add("value", gson.toJsonTree(party));
			publish.addProperty("source", BattlebitsAPI.getServerId());
			
			pipeline.publish("party-action", publish.toString());
			jedis.sync();
		}
	}
	
	public static void unloadParty(Party party)
	{
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) 
		{
			Pipeline pipeline = jedis.pipelined();
			
			JsonObject publish = new JsonObject();
			
			publish.addProperty("action", "unload");
			publish.addProperty("owner", party.getOwner().toString());
			publish.addProperty("source", BattlebitsAPI.getServerId());

			pipeline.publish("party-action", publish.toString());
			jedis.sync();
		}
	}

	public static void saveRedisParty(Party party)
	{
		Map<String, String> tree = toRedisTree(party);

		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) 
		{
			Pipeline pipeline = jedis.pipelined();
			pipeline.hmset("party:" + party.getOwner().toString(), tree);
			jedis.sync();
		}
	}
	
	public static void saveRedisPartyField(Party party, String fieldName) 
	{
		JsonObject jsonObject = (JsonObject) gson.toJsonTree(party);

		if (jsonObject.has(fieldName))
		{
			JsonElement element = jsonObject.get(fieldName);
			
			String value;
			
			if (!element.isJsonPrimitive())
				value = element.toString();
			else
				value = element.getAsString();
			
			try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource())
			{
				Pipeline pipeline = jedis.pipelined();
				
				JsonObject publish = new JsonObject();
				publish.addProperty("owner", party.getOwner().toString());
				publish.addProperty("source", BattlebitsAPI.getServerId());
				publish.addProperty("field", fieldName);
				publish.addProperty("value", value);
			
				pipeline.publish("party-field", publish.toString());
				pipeline.sync();
			}
		}
	}
	
	public static void expire(Party party) 
	{
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource())
		{
			BattlebitsAPI.debug("REDIS > EXPIRE 300");
			jedis.expire("party:" + party.getOwner().toString(), 300);
		}
	}
	
	public static void disbandParty(Party party)
	{
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource())
		{
			BattlebitsAPI.debug("REDIS > DELETE");
			Pipeline pipe = jedis.pipelined();
			pipe.del("party:" + party.getOwner().toString());
			jedis.sync();
		}
	}
	
	public static boolean checkCache(Party party)
	{
		boolean bool = false;
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			String key = "party:" + party.getOwner().toString();
			if (jedis.ttl(key) >= 0) {
				bool = jedis.persist(key) == 1;
			}
		}
		
		if (bool)
			BattlebitsAPI.getLogger().info("REDIS > SHOULD REMOVE");
		else
			BattlebitsAPI.getLogger().info("REDIS > SUB-SERVER");
		
		return bool;
	}
}
