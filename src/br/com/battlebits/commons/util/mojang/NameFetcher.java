package br.com.battlebits.commons.util.mojang;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.battlebits.commons.BattlebitsAPI;

public class NameFetcher {
	private JsonParser parser = new JsonParser();

	private ArrayList<String> servers;
	private HashMap<String, Integer> fails;
	private int current;

	private Cache<UUID, String> uuidName = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS)
			.build(new CacheLoader<UUID, String>() {
				@Override
				public String load(UUID id) throws Exception {
					return loadFromServers(id);
				}
			});

	public NameFetcher() {
		servers = new ArrayList<>();
		servers.add("https://sessionserver.mojang.com/session/minecraft/profile/%player-uuid%#name#id");
		servers.add("https://craftapi.com/api/user/username/%player-uuid%#username#uuid");
		servers.add("https://us.mc-api.net/v3/name/%player-uuid%#name#uuid");
		servers.add("https://mcapi.ca/name/uuid/%player-uuid%#name#uuid");
		// URL # CAMPO NOME # CAMPO ID
		fails = new HashMap<>();
		current = 0;
	}

	private String getNextServer() {
		if (current == (servers.size() - 1)) {
			current = 0;
		} else {
			current += 1;
		}
		return servers.get(current);
	}

	public String loadFromServers(UUID id) {
		String name = null;
		String server1 = getNextServer();
		name = load(id, server1);
		if (name == null) {
			name = load(id, getNextServer());
			if (name != null) {
				if (fails.containsKey(server1)) {
					fails.put(server1, fails.get(server1) + 1);
				} else {
					fails.put(server1, 1);
				}
				BattlebitsAPI.getLogger()
						.warning(server1 + " falhou em tentar obter o nome do jogador " + id.toString());
			}
		}
		server1 = null;
		return name;
	}

	public String load(UUID id, String server) {
		String name = null;
		try {
			String[] infos = server.split("#");
			String serverUrl = infos[0].replace("%player-uuid%", id.toString().replace("-", ""));
			URL url = new URL(serverUrl);
			InputStream is = url.openStream();
			InputStreamReader streamReader = new InputStreamReader(is, Charset.forName("UTF-8"));
			JsonObject object = parser.parse(streamReader).getAsJsonObject();
			if (object.has(infos[1]) && object.has(infos[2])
					&& object.get(infos[2]).getAsString().equalsIgnoreCase(id.toString().replace("-", ""))) {
				name = object.get(infos[1]).getAsString();
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
			id = null;
		} catch (Exception ex) {
			return null;
		}
		return name;
	}

	public String getUsername(UUID id) {
		try {
			return uuidName.get(id, new Callable<String>() {
				@Override
				public String call() throws Exception {
					return loadFromServers(id);
				}
			});
		} catch (Exception e) {
			return null;
		}
	}
}
