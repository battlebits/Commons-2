package br.com.battlebits.commons.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.item.ActionItemListener;
import br.com.battlebits.commons.api.menu.MenuListener;
import br.com.battlebits.commons.bukkit.command.BukkitCommandFramework;
import br.com.battlebits.commons.bukkit.generator.VoidGenerator;
import br.com.battlebits.commons.bukkit.injector.ActionItemInjector;
import br.com.battlebits.commons.bukkit.injector.TranslationInjector;
import br.com.battlebits.commons.bukkit.listener.AccountListener;
import br.com.battlebits.commons.bukkit.listener.AntiAFK;
import br.com.battlebits.commons.bukkit.listener.ChatListener;
import br.com.battlebits.commons.bukkit.listener.PlayerListener;
import br.com.battlebits.commons.bukkit.listener.ScoreboardListener;
import br.com.battlebits.commons.bukkit.permission.PermissionManager;
import br.com.battlebits.commons.bukkit.redis.BukkitPubSubHandler;
import br.com.battlebits.commons.bukkit.scheduler.UpdateScheduler;
import br.com.battlebits.commons.bukkit.scoreboard.tagmanager.TagManager;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.backend.mongodb.MongoBackend;
import br.com.battlebits.commons.core.backend.redis.PubSubListener;
import br.com.battlebits.commons.core.backend.redis.RedisBackend;
import br.com.battlebits.commons.core.command.CommandLoader;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.core.translate.Translate;
import br.com.battlebits.commons.util.updater.AutoUpdater;
import lombok.Getter;
import lombok.Setter;

@Getter
public class BukkitMain extends JavaPlugin {
	private ProtocolManager procotolManager;
	private PermissionManager permissionManager;
	private TagManager tagManager;
	@Getter
	private static BukkitMain plugin;
	private boolean oldTag = false;
	@Setter
	private boolean tagControl = true;
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
		new AutoUpdater(this, "vAPS4jf?&R_}E25T").run();
		plugin = this;
		procotolManager = ProtocolLibrary.getProtocolManager();
		new TranslationInjector().inject(this);
		new ActionItemInjector().inject(this);
	}

	@Override
	public void onEnable() {
		loadConfiguration();
		try {
			MongoBackend mongoBackend = new MongoBackend(mongoHostname, mongoDatabase, mongoUsername, mongoPassword,
					mongoPort);
			RedisBackend redisBackend = new RedisBackend(redisHostname, redisPassword, redisPort);
			mongoBackend.startConnection();
			redisBackend.startConnection();
			BattlebitsAPI.setMongo(mongoBackend);
			BattlebitsAPI.setRedis(redisBackend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		BattlebitsAPI.setLogger(getLogger());
		BattlebitsAPI.setServerAddress(Bukkit.getIp() + ":" + Bukkit.getPort());
		BattlebitsAPI.setServerId(DataServer.getServerId(BattlebitsAPI.getServerAddress()));
		BattlebitsAPI.getLogger().info("Battlebits Server carregado. ServerId: " + BattlebitsAPI.getServerId());
		DataServer.newServer(Bukkit.getMaxPlayers());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, BattlebitsAPI.getBungeeChannel());
		for (Language lang : Language.values()) {
			Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
		}
		registerListeners();
		registerCommonManagement();
		enableCommonManagement();
		getServer().getScheduler().runTaskAsynchronously(this,
				pubSubListener = new PubSubListener(new BukkitPubSubHandler(), "account-field", "clan-field"));
		getServer().getScheduler().runTaskTimer(this, new UpdateScheduler(), 1, 1);
		try {
			new CommandLoader(new BukkitCommandFramework(plugin))
					.loadCommandsFromPackage("br.com.battlebits.commons.bukkit.command.register");
		} catch (Exception e) {
			BattlebitsAPI.getLogger().warning("Erro ao carregar o commandFramework!");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		DataServer.stopServer();
		BattlebitsAPI.getMongo().closeConnection();
		BattlebitsAPI.getRedis().closeConnection();
	}

	private void loadConfiguration() {
		mongoHostname = getConfig().getString("mongo.hostname");
		mongoPort = getConfig().getInt("mongo.port");
		mongoDatabase = getConfig().getString("mongo.database");
		mongoUsername = getConfig().getString("mongo.username");
		mongoPassword = getConfig().getString("mongo.password");

		redisHostname = getConfig().getString("redis.hostname");
		redisPassword = getConfig().getString("redis.password");
		redisPort = getConfig().getInt("redis.port");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new AntiAFK(), this);
		getServer().getPluginManager().registerEvents(new AccountListener(), this);
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new ScoreboardListener(), this);

		// APIs
		getServer().getPluginManager().registerEvents(new ActionItemListener(), this);
		getServer().getPluginManager().registerEvents(new MenuListener(), this);
	}

	public static void broadcastMessage(String id, String[]... replace) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BattlePlayer bp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player);
			if (bp != null) {
				Language lang = bp.getLanguage();
				player.sendMessage(T.t(lang, id, replace));
			}
		}
	}

	public static void broadcastMessage(String id, String[] target, String[] replace) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BattlePlayer bp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player);
			if (bp != null) {
				Language lang = bp.getLanguage();
				player.sendMessage(T.t(lang, id, target, replace));
			}
		}
	}

	public static void broadcastMessage(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(message);
		}
	}

	private void registerCommonManagement() {
		permissionManager = new PermissionManager(this);
		tagManager = new TagManager(this);
	}

	private void enableCommonManagement() {
		permissionManager.onEnable();
		tagManager.onEnable();
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new VoidGenerator();
	}

}
