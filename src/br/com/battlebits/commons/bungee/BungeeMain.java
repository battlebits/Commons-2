package br.com.battlebits.commons.bungee;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.command.BungeeCommandFramework;
import br.com.battlebits.commons.bungee.listener.AccountListener;
import br.com.battlebits.commons.bungee.listener.ChatListener;
import br.com.battlebits.commons.bungee.listener.LoadBalancerListener;
import br.com.battlebits.commons.bungee.listener.MessageListener;
import br.com.battlebits.commons.bungee.listener.ScreenshareListener;
import br.com.battlebits.commons.bungee.listener.ServerListener;
import br.com.battlebits.commons.bungee.manager.BanManager;
import br.com.battlebits.commons.bungee.manager.ServerManager;
import br.com.battlebits.commons.bungee.redis.BungeePubSubHandler;
import br.com.battlebits.commons.core.backend.mongodb.MongoBackend;
import br.com.battlebits.commons.core.backend.redis.PubSubListener;
import br.com.battlebits.commons.core.backend.redis.RedisBackend;
import br.com.battlebits.commons.core.backend.sql.MySQLBackend;
import br.com.battlebits.commons.core.command.CommandLoader;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
public class BungeeMain extends Plugin {
	@Getter
	private static BungeeMain plugin;
	private ServerManager serverManager = new ServerManager();
	private BanManager banManager = new BanManager();

	// CONNECTIONS
	private MySQLBackend mysqlBackend;

	@Override
	public void onLoad() {
		plugin = this;
	}

	@Override
	public void onEnable() {
		try {
			MongoBackend mongoBackend = new MongoBackend();
			RedisBackend redisBackend = new RedisBackend();
			mongoBackend.startConnection();
			redisBackend.startConnection();
			BattlebitsAPI.setMongo(mongoBackend);
			BattlebitsAPI.setRedis(redisBackend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BattlebitsAPI.setLogger(getLogger());
		@SuppressWarnings("deprecation")
		ListenerInfo info = getProxy().getConfig().getListeners().iterator().next();
		BattlebitsAPI
				.setServerId(DataServer.getServerId(info.getHost().getHostName() + ":" + info.getHost().getPort()));
		DataServer.newServer(ServerType.NETWORK, BattlebitsAPI.getServerId(), info.getMaxPlayers());
		for (Language lang : Language.values()) {
			Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
		}
		getProxy().registerChannel(BattlebitsAPI.getBungeeChannel());
		loadListeners();
		try {
			new CommandLoader(new BungeeCommandFramework(plugin))
					.loadCommandsFromPackage("br.com.battlebits.commons.bungee.command.register");
		} catch (Exception e) {
			BattlebitsAPI.getLogger().warning("Erro ao carregar o commandFramework!");
			e.printStackTrace();
		}
		getProxy().getScheduler().runAsync(this, new PubSubListener(new BungeePubSubHandler()));
	}

	@Override
	public void onDisable() {
		DataServer.stopServer(ServerType.NETWORK, BattlebitsAPI.getServerId());
		BattlebitsAPI.getMongo().closeConnection();
		BattlebitsAPI.getRedis().closeConnection();
	}

	private void loadListeners() {
		getProxy().getPluginManager().registerListener(this, new AccountListener());
		getProxy().getPluginManager().registerListener(this, new ChatListener());
		getProxy().getPluginManager().registerListener(this, new LoadBalancerListener(serverManager));
		getProxy().getPluginManager().registerListener(this, new MessageListener(serverManager));
		getProxy().getPluginManager().registerListener(this, new ScreenshareListener());
		getProxy().getPluginManager().registerListener(this, new ServerListener());
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
