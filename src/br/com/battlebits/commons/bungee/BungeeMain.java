package br.com.battlebits.commons.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.command.BungeeCommandFramework;
import br.com.battlebits.commons.bungee.listener.AccountListener;
import br.com.battlebits.commons.bungee.listener.ChatListener;
import br.com.battlebits.commons.bungee.listener.LoadBalancerListener;
import br.com.battlebits.commons.bungee.listener.MessageListener;
import br.com.battlebits.commons.bungee.listener.MultiserverTeleport;
import br.com.battlebits.commons.bungee.listener.PartyListener;
import br.com.battlebits.commons.bungee.listener.ScreenshareListener;
import br.com.battlebits.commons.bungee.manager.BanManager;
import br.com.battlebits.commons.bungee.manager.BungeeServerManager;
import br.com.battlebits.commons.bungee.redis.BungeePubSubHandler;
import br.com.battlebits.commons.bungee.util.BungeeUUID;
import br.com.battlebits.commons.core.backend.mongodb.MongoBackend;
import br.com.battlebits.commons.core.backend.redis.PubSubListener;
import br.com.battlebits.commons.core.backend.redis.RedisBackend;
import br.com.battlebits.commons.core.command.CommandLoader;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.server.ServerManager;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

@Getter
public class BungeeMain extends Plugin {
	@Getter
	private static BungeeMain plugin;
	private ServerManager serverManager = new BungeeServerManager();
	private BanManager banManager = new BanManager();

	// CONNECTIONS

	private Configuration config;

	private PubSubListener pubSubListener;

	private String mongoHostname;
	private String mongoDatabase;
	private String mongoUsername;
	private String mongoPassword;
	private int mongoPort = 27017;

	private String redisHostname;
	private String redisPassword;
	private int redisPort = 6379;

	@Override
	public void onLoad() {
		plugin = this;
	}

	@Override
	public void onEnable() {
		try {
			if (!getDataFolder().exists()) {
	            getDataFolder().mkdir();
	        }
	
	        File configFile = new File(getDataFolder(), "config.yml");
	        
	        if (!configFile.exists()) {
	        	try {
	        		configFile.createNewFile();
	        		try (InputStream is = getResourceAsStream("config.yml"); 
	        				OutputStream os = new FileOutputStream(configFile)) {
	                	ByteStreams.copy(is, os);
	        		}
	        	} catch (IOException e) {
	                throw new RuntimeException("Unable to create configuration file", e);
				}
	        }

			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		loadConfiguration();
		
		try {
			MongoBackend mongoBackend = new MongoBackend(mongoHostname, mongoDatabase, mongoUsername, mongoPassword,
					mongoPort);
			RedisBackend redisBackend = new RedisBackend(redisHostname, redisPassword, redisPort);
			mongoBackend.startConnection();
			redisBackend.startConnection();
			BattlebitsAPI.setCommonsMongo(mongoBackend);
			BattlebitsAPI.setCommonsRedis(redisBackend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BattlebitsAPI.setGetter(new BungeeUUID());
		BattlebitsAPI.setLogger(getLogger());
		@SuppressWarnings("deprecation")
		ListenerInfo info = getProxy().getConfig().getListeners().iterator().next();
		BattlebitsAPI.setServerAddress(info.getHost().getHostString() + ":" + info.getHost().getPort());
		BattlebitsAPI.setServerId(DataServer.getServerId(BattlebitsAPI.getServerAddress()));
		BattlebitsAPI.getLogger().info("Battlebits Server carregado. ServerId: " + BattlebitsAPI.getServerId());
		DataServer.newServer(info.getMaxPlayers());
		for (Language lang : Language.values()) {
			Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
		}
		getProxy().registerChannel(BattlebitsAPI.getBungeeChannel());
		loadListeners();
		try {
			new CommandLoader(new BungeeCommandFramework(plugin))
					.loadCommandsFromPackage(getFile(), "br.com.battlebits.commons.bungee.command.register");
		} catch (Exception e) {
			BattlebitsAPI.getLogger().warning("Erro ao carregar o commandFramework!");
			e.printStackTrace();
		}
		getProxy().getScheduler().runAsync(this, pubSubListener = new PubSubListener(new BungeePubSubHandler(),
				"account-field", "clan-field", "party-field", "server-info"));
		for (Entry<String, Map<String, String>> entry : DataServer.getAllServers().entrySet()) {
			try {
				if (!entry.getValue().containsKey("type"))
					continue;
				if (!entry.getValue().containsKey("address"))
					continue;
				if (!entry.getValue().containsKey("maxplayers"))
					continue;
				if (!entry.getValue().containsKey("onlineplayers"))
					continue;
				if (ServerType.getServerType(entry.getValue().get("type").toUpperCase()) == ServerType.NETWORK)
					continue;

				BungeeMain.getPlugin().getServerManager().addActiveServer(entry.getValue().get("address"),
						entry.getKey(), Integer.valueOf(entry.getValue().get("maxplayers")));
				BungeeMain.getPlugin().getServerManager().getServer(entry.getKey())
						.setOnlinePlayers(DataServer.getPlayers(entry.getKey()));
			} catch (Exception e) {
			}
		}

	}

	@Override
	public void onDisable() {
		DataServer.stopServer();
		BattlebitsAPI.getCommonsMongo().closeConnection();
		BattlebitsAPI.getCommonsRedis().closeConnection();
	}

	private void loadListeners() {
		getProxy().getPluginManager().registerListener(this, new AccountListener());
		getProxy().getPluginManager().registerListener(this, new ChatListener());
		getProxy().getPluginManager().registerListener(this, new LoadBalancerListener(serverManager));
		getProxy().getPluginManager().registerListener(this, new MessageListener(serverManager));
		getProxy().getPluginManager().registerListener(this, new MultiserverTeleport());
		getProxy().getPluginManager().registerListener(this, new PartyListener());
		getProxy().getPluginManager().registerListener(this, new ScreenshareListener());
	}

	private void loadConfiguration() {
		mongoHostname = config.getString("mongo.hostname", "localhost");
		mongoPort = config.getInt("mongo.port", 27017);
		mongoDatabase = config.getString("mongo.database", "");
		mongoUsername = config.getString("mongo.username", "");
		mongoPassword = config.getString("mongo.password", "");

		redisHostname = config.getString("redis.hostname", "localhost");
		redisPassword = config.getString("redis.password", "");
		redisPort = config.getInt("redis.port", 6379);
	}

	public boolean serverExists(String paramString) {
		return ProxyServer.getInstance().getServers().containsKey(paramString);
	}

	public void addBungee(final String serverHostName, String ipAddress, int port) {
		final ServerInfo localServerInfo = getProxy().constructServerInfo(serverHostName,
				new InetSocketAddress(ipAddress, port), "Restarting", false);
		if (!serverExists(serverHostName)) {
			BattlebitsAPI.getLogger().info("Server " + serverHostName + " adicionado ao Bungee.");
			getProxy().getServers().put(serverHostName, localServerInfo);
		} else {
			BattlebitsAPI.getLogger().log(Level.WARNING, "Servidor \"" + serverHostName + "\" já existe!");
		}
	}

	public boolean removeBungee(String paramString) {
		if (serverExists(paramString)) {
			BattlebitsAPI.getLogger().info("Removido server " + paramString + " do Bungee.");
			getProxy().getServers().remove(paramString);
			serverManager.removeActiveServer(paramString);
			return true;
		}
		BattlebitsAPI.getLogger().log(Level.WARNING,
				"&cTentado remover servidor \"" + paramString + "\" mas ele nao existe!");
		return false;
	}

}
