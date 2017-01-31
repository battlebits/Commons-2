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
import br.com.battlebits.commons.bungee.loadbalancer.server.BattleServer;
import br.com.battlebits.commons.bungee.loadbalancer.server.MinigameState;
import br.com.battlebits.commons.core.data.DataServer.DataServerMessage.Action;
import br.com.battlebits.commons.core.data.DataServer.DataServerMessage.JoinEnablePayload;
import br.com.battlebits.commons.core.data.DataServer.DataServerMessage.JoinPayload;
import br.com.battlebits.commons.core.data.DataServer.DataServerMessage.LeavePayload;
import br.com.battlebits.commons.core.data.DataServer.DataServerMessage.StartPayload;
import br.com.battlebits.commons.core.data.DataServer.DataServerMessage.StopPayload;
import br.com.battlebits.commons.core.data.DataServer.DataServerMessage.UpdatePayload;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class DataServer extends Data {
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

	public static void newServer(ServerType serverType, String serverId, int maxPlayers) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.sadd("server:type:" + serverType.toString().toLowerCase(), serverId);
			HashMap<String, String> map = new HashMap<>();
			map.put("type", serverType.toString().toLowerCase());
			map.put("maxplayers", maxPlayers + "");
			map.put("joinenabled", "true");
			pipe.hmset("server:" + serverId, map);
			pipe.del("server:" + serverId + ":players");
			BattleServer server = new BattleServer(serverId, 0, maxPlayers, true);
			pipe.publish("server-info",
					BattlebitsAPI.getGson().toJson(new DataServerMessage<StartPayload>(BattlebitsAPI.getServerId(),
							Action.START, new StartPayload(BattlebitsAPI.getServerAddress(), server))));
			pipe.sync();
		}
	}

	public static void updateStatus(String serverId, MinigameState state, int time) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.hset("server:" + serverId, "state", state.toString().toLowerCase());
			pipe.hset("server:" + serverId, "time", time + "");
			pipe.publish("server-info",
					BattlebitsAPI.getGson().toJson(new DataServerMessage<UpdatePayload>(BattlebitsAPI.getServerId(),
							Action.UPDATE, new UpdatePayload(time, state))));
			pipe.sync();
		}
	}

	public static void setJoinEnabled(String serverId, boolean bol) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.hset("server:" + serverId, "joinenabled", bol + "");
			pipe.publish("server-info",
					BattlebitsAPI.getGson().toJson(new DataServerMessage<JoinEnablePayload>(BattlebitsAPI.getServerId(),
							Action.JOIN_ENABLE, new JoinEnablePayload(bol))));
			pipe.sync();
		}
	}

	public static void stopServer(ServerType serverType, String serverId) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.srem("server:type:" + serverType.toString().toLowerCase(), serverId);
			pipe.del("server:" + serverId);
			pipe.del("server:" + serverId + ":players");
			pipe.publish("server-info",
					BattlebitsAPI.getGson().toJson(new DataServerMessage<StopPayload>(BattlebitsAPI.getServerId(),
							Action.STOP, new StopPayload(serverId))));
			pipe.sync();
		}
	}

	public static void joinPlayer(String serverId, UUID uuid) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.sadd("server:" + serverId + ":players", uuid.toString());
			pipe.publish("server-info",
					BattlebitsAPI.getGson().toJson(new DataServerMessage<JoinPayload>(BattlebitsAPI.getServerId(),
							Action.JOIN, new JoinPayload(uuid))));
			pipe.sync();
		}
	}

	public static void leavePlayer(String serverId, UUID uuid) {
		try (Jedis jedis = BattlebitsAPI.getRedis().getPool().getResource()) {
			Pipeline pipe = jedis.pipelined();
			pipe.srem("server:" + serverId + ":players", uuid.toString());
			pipe.publish("server-info",
					BattlebitsAPI.getGson().toJson(new DataServerMessage<LeavePayload>(BattlebitsAPI.getServerId(),
							Action.LEAVE, new LeavePayload(uuid))));
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

	@Getter
	@RequiredArgsConstructor
	public static class DataServerMessage<T> {
		private final String source;
		private final Action action;
		private final T payload;

		public static enum Action {
			START, STOP, UPDATE, JOIN_ENABLE, JOIN, LEAVE,
		}

		@Getter
		@RequiredArgsConstructor
		public static class StartPayload {
			private final String serverAddress;
			private final BattleServer server;
		}

		@Getter
		@RequiredArgsConstructor
		public static class StopPayload {
			private final String serverId;
		}

		@Getter
		@RequiredArgsConstructor
		public static class UpdatePayload {
			private final int time;
			private final MinigameState state;
		}

		@Getter
		@RequiredArgsConstructor
		public static class JoinEnablePayload {
			private final boolean enable;
		}

		@Getter
		@RequiredArgsConstructor
		public static class JoinPayload {
			private final UUID uniqueId;
		}

		@Getter
		@RequiredArgsConstructor
		public static class LeavePayload {
			private final UUID uniqueId;
		}
	}

}
