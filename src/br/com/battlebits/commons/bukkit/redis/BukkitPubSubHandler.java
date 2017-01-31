package br.com.battlebits.commons.bukkit.redis;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.event.account.PlayerUpdateFieldEvent;
import br.com.battlebits.commons.bukkit.event.account.PlayerUpdatedFieldEvent;
import br.com.battlebits.commons.bukkit.event.redis.RedisPubSubMessageEvent;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.util.reflection.Reflection;
import redis.clients.jedis.JedisPubSub;

public class BukkitPubSubHandler extends JedisPubSub {

	@Override
	public void onMessage(String channel, String message) {
		Bukkit.getPluginManager().callEvent(new RedisPubSubMessageEvent(channel, message));
		if (channel.equals("account-field")) {
			JsonObject obj = (JsonObject) BattlebitsAPI.getParser().parse(message);
			if (obj.getAsJsonPrimitive("source").getAsString().equalsIgnoreCase(BattlebitsAPI.getServerId())) {
				return;
			}
			UUID uuid = UUID.fromString(obj.getAsJsonPrimitive("uniqueId").getAsString());
			Player p = BukkitMain.getPlugin().getServer().getPlayer(uuid);
			if (p == null)
				return;
			String field = obj.getAsJsonPrimitive("field").getAsString();
			BukkitPlayer player = (BukkitPlayer) BattlePlayer.getPlayer(uuid);
			try {
				Field f = Reflection.getField(BattlePlayer.class, field);
				f.setAccessible(true);
				Object object = BattlebitsAPI.getGson().fromJson(obj.get("value"), f.getGenericType());
				PlayerUpdateFieldEvent event = new PlayerUpdateFieldEvent(p, player, field, object);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					f.set(player, event.getObject());
					PlayerUpdatedFieldEvent event2 = new PlayerUpdatedFieldEvent(p, player, field, object);
					Bukkit.getPluginManager().callEvent(event2);
				}
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
		}
	}

	public static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (Exception e2) {
				if (clazz.getSuperclass() != null)
					return getField(clazz.getSuperclass(), name);
			}
		}
		return null;
	}

}
