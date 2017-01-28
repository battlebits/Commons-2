package br.com.battlebits.commons.util.mojang;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.battlebits.commons.BattlebitsAPI;

public class UUIDFetcher {
	private JsonParser parser = new JsonParser();

	private ArrayList<String> servers;
	private HashMap<String, Integer> fails;
	private int current;
	private Pattern namePattern;

	private Cache<String, UUID> nameUUID = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS)
			.build(new CacheLoader<String, UUID>() {
				@Override
				public UUID load(String name) throws Exception {
					return loadFromServers(name);
				}
			});

	public UUIDFetcher() {
		servers = new ArrayList<>();
		servers.add("https://api.mojang.com/users/profiles/minecraft/%player-name%#id#name");
		servers.add("https://mcapi.ca/uuid/player/%player-name%#id#name");
		// servers.add("https://us.mc-api.net/v3/uuid/%player-name%#full_uuid#name");
		// URL # CAMPO ID # CAMPO NOME
		fails = new HashMap<>();
		current = 0;
		namePattern = Pattern.compile("[a-zA-Z0-9_]{1,16}");
	}

	private String getNextServer() {
		if (current == (servers.size() - 1)) {
			current = 0;
		} else {
			current += 1;
		}
		return servers.get(current);
	}

	public UUID loadFromServers(String name) {
		UUID id = null;
		String server1 = getNextServer();
		id = load(name, server1);
		if (id == null) {
			id = load(name, getNextServer());
			if (id == null) {
				if (fails.containsKey(server1)) {
					fails.put(server1, fails.get(server1) + 1);
				} else {
					fails.put(server1, 1);
				}
				BattlebitsAPI.getLogger().warning(server1 + " falhou em tentar obter a UUID do jogador " + name);
			}
		}
		server1 = null;
		return id;
	}

	public boolean isValidName(String username) {
		if (username.length() < 3)
			return false;
		if (username.length() > 16)
			return false;
		Matcher matcher = namePattern.matcher(username);
		return matcher.matches();
	}

	public UUID getUUIDFromString(String id) {
		if (id.length() == 36) {
			return UUID.fromString(id);
		} else if (id.length() == 32) {
			return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-"
					+ id.substring(16, 20) + "-" + id.substring(20, 32));
		} else {
			return null;
		}
	}

	public UUID load(String name, String server) {
		UUID id = null;
		try {
			String[] infos = server.split("#");
			String serverUrl = infos[0].replace("%player-name%", name);
			URL url = new URL(serverUrl);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setConnectTimeout(1000);
			InputStream is = huc.getInputStream();
			InputStreamReader streamReader = new InputStreamReader(is, Charset.forName("UTF-8"));
			JsonElement element = parser.parse(streamReader);
			JsonObject object = null;
			if (element.isJsonArray())
				object = element.getAsJsonArray().get(0).getAsJsonObject();
			else
				object = element.getAsJsonObject();
			if (object.has(infos[2]) && object.has(infos[1])
					&& object.get(infos[2]).getAsString().equalsIgnoreCase(name)) {
				id = (getUUIDFromString(object.get(infos[1]).getAsString()));
			}
			streamReader.close();
			is.close();
			object = null;
			streamReader = null;
			is = null;
			url = null;
			serverUrl = null;
			infos = null;
			server = null;
		} catch (Exception ex) {
			return null;
		}
		return id;
	}

	public UUID getUUID(String input) {
		if (input.length() == 32 || input.length() == 36) {
			try {
				UUID uuid = getUUIDFromString(input);
				return uuid;
			} catch (Exception e) {
				return null;
			}
		} else if (isValidName(input)) {
			try {
				return nameUUID.get(input, new Callable<UUID>() {
					@Override
					public UUID call() throws Exception {
						return loadFromServers(input);
					}
				});
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}
}
