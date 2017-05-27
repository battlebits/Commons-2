package br.com.battlebits.commons.bungee.redis;

import java.lang.reflect.Field;
import java.util.UUID;

import br.com.battlebits.commons.core.data.payload.DataServerMessage;
import br.com.battlebits.commons.core.data.payload.DataServerMessage.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.party.BungeeParty;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.core.party.Party;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.server.loadbalancer.server.BattleServer;
import br.com.battlebits.commons.core.server.loadbalancer.server.MinigameServer;
import br.com.battlebits.commons.util.reflection.Reflection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class BungeePubSubHandler extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		if (channel.equals("account-field")) {
			JsonObject obj = (JsonObject) BattlebitsAPI.getParser().parse(message);
			if (obj.getAsJsonPrimitive("source").getAsString().equalsIgnoreCase(BattlebitsAPI.getServerId())) {
				return;
			}
			UUID uuid = UUID.fromString(obj.getAsJsonPrimitive("uniqueId").getAsString());
			ProxiedPlayer p = BungeeMain.getPlugin().getProxy().getPlayer(uuid);
			if (p == null)
				return;
			String field = obj.getAsJsonPrimitive("field").getAsString();
			BattlePlayer player = BattlePlayer.getPlayer(uuid);
			if (player == null)
				return;
			try {
				Field f = Reflection.getField(BattlePlayer.class, field);
				f.setAccessible(true);
				Object object = BattlebitsAPI.getGson().fromJson(obj.get("value"), f.getGenericType());
				f.set(player, object);
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if (channel.equals("clan-field")) {
			JsonObject jsonObject = BattlebitsAPI.getParser().parse(message).getAsJsonObject();
			String source = jsonObject.get("source").getAsString();
			if (source.equals(BattlebitsAPI.getServerId()))
				return;
			UUID uuid = UUID.fromString(jsonObject.get("uniqueId").getAsString());
			Clan clan = BattlebitsAPI.getClanCommon().getClan(uuid);
			if (clan == null)
				return;
			try {
				Field f = Reflection.getField(Clan.class, jsonObject.get("field").getAsString());
				f.setAccessible(true);
				Object object = BattlebitsAPI.getGson().fromJson(jsonObject.get("value"), f.getGenericType());
				f.set(clan, object);
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if (channel.equals("party-field")) {
			JsonObject jsonObject = BattlebitsAPI.getParser().parse(message).getAsJsonObject();
			String source = jsonObject.get("source").getAsString();
			if (source.equals(BattlebitsAPI.getServerId()))
				return;
			UUID uuid = UUID.fromString(jsonObject.get("owner").getAsString());
			Party party = BattlebitsAPI.getPartyCommon().getByOwner(uuid);
			if (party == null)
				return;
			try {
				Field field = getField(BungeeParty.class, jsonObject.get("field").getAsString());
				Object object = BattlebitsAPI.getGson().fromJson(jsonObject.get("value"), field.getGenericType());
				field.set(party, object);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (channel.equals("server-info")) {
			JsonObject jsonObject = BattlebitsAPI.getParser().parse(message).getAsJsonObject();
			String source = jsonObject.get("source").getAsString();
			if (source.equals(BattlebitsAPI.getServerId()))
				return;
			ServerType sourceType = ServerType.valueOf(jsonObject.get("serverType").getAsString());
			Action action = Action.valueOf(jsonObject.get("action").getAsString());
			switch (action) {
			case JOIN: {
				DataServerMessage<JoinPayload> payload = BattlebitsAPI.getGson().fromJson(jsonObject,
						new TypeToken<DataServerMessage<JoinPayload>>() {
						}.getType());
				if (sourceType == ServerType.NETWORK) {
					break;
				}
				BattleServer server = BungeeMain.getPlugin().getServerManager().getServer(source);
				server.joinPlayer(payload.getPayload().getUniqueId());
				break;
			}
			case LEAVE: {
				DataServerMessage<LeavePayload> payload = BattlebitsAPI.getGson().fromJson(jsonObject,
						new TypeToken<DataServerMessage<LeavePayload>>() {
						}.getType());
				if (sourceType == ServerType.NETWORK) {
					break;
				}
				BattleServer server = BungeeMain.getPlugin().getServerManager().getServer(source);
				server.leavePlayer(payload.getPayload().getUniqueId());
				break;
			}
			case JOIN_ENABLE: {
				DataServerMessage<JoinEnablePayload> payload = BattlebitsAPI.getGson().fromJson(jsonObject,
						new TypeToken<DataServerMessage<JoinEnablePayload>>() {
						}.getType());
				if (sourceType == ServerType.NETWORK) {
					break;
				}
				BungeeMain.getPlugin().getServerManager().getServer(source)
						.setJoinEnabled(payload.getPayload().isEnable());
				break;
			}
			case START: {
				DataServerMessage<StartPayload> payload = BattlebitsAPI.getGson().fromJson(jsonObject,
						new TypeToken<DataServerMessage<StartPayload>>() {
						}.getType());
				if (sourceType == ServerType.NETWORK) {
					break;
				}
				BungeeMain.getPlugin().getServerManager().addActiveServer(payload.getPayload().getServerAddress(),
						payload.getPayload().getServer().getServerId(), sourceType,
						payload.getPayload().getServer().getMaxPlayers());
				break;
			}
			case STOP: {
				DataServerMessage<StopPayload> payload = BattlebitsAPI.getGson().fromJson(jsonObject,
						new TypeToken<DataServerMessage<StopPayload>>() {
						}.getType());
				if (sourceType == ServerType.NETWORK) {
					break;
				}
				BungeeMain.getPlugin().getServerManager().removeActiveServer(payload.getPayload().getServerId());
				BungeeMain.getPlugin().removeBungee(payload.getPayload().getServerId());
				break;
			}
			case UPDATE: {
				DataServerMessage<UpdatePayload> payload = BattlebitsAPI.getGson().fromJson(jsonObject,
						new TypeToken<DataServerMessage<UpdatePayload>>() {
						}.getType());
				if (sourceType == ServerType.NETWORK) {
					break;
				}
				BattleServer server = BungeeMain.getPlugin().getServerManager().getServer(source);
				if (server instanceof MinigameServer) {
					((MinigameServer) server).setState(payload.getPayload().getState());
					((MinigameServer) server).setTime(payload.getPayload().getTime());
				}
				break;
			}
			default:
				break;
			}
		}
	}
	
	private Field getField(Class<?> clazz, String fieldName) {
		while ((clazz != null) && (clazz != Object.class)) {
			try {
				Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;				
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}
}
