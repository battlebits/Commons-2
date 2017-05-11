package br.com.battlebits.commons.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.bossbar.BossBarAPI;
import br.com.battlebits.commons.api.item.ActionItemListener;
import br.com.battlebits.commons.api.menu.MenuListener;
import br.com.battlebits.commons.bukkit.command.BukkitCommandFramework;
import br.com.battlebits.commons.bukkit.generator.VoidGenerator;
import br.com.battlebits.commons.bukkit.injector.ActionItemInjector;
import br.com.battlebits.commons.bukkit.injector.ServerInfoInjector;
import br.com.battlebits.commons.bukkit.injector.TranslationInjector;
import br.com.battlebits.commons.bukkit.listener.AccountListener;
import br.com.battlebits.commons.bukkit.listener.AntiAFK;
import br.com.battlebits.commons.bukkit.listener.ChatListener;
import br.com.battlebits.commons.bukkit.listener.PlayerNBTListener;
import br.com.battlebits.commons.bukkit.listener.PlayerListener;
import br.com.battlebits.commons.bukkit.listener.ScoreboardListener;
import br.com.battlebits.commons.bukkit.messenger.BungeeCordMessenger;
import br.com.battlebits.commons.bukkit.permission.PermissionManager;
import br.com.battlebits.commons.bukkit.protocol.ProtocolHook;
import br.com.battlebits.commons.bukkit.redis.BukkitPubSubHandler;
import br.com.battlebits.commons.bukkit.scheduler.UpdateScheduler;
import br.com.battlebits.commons.bukkit.scoreboard.tagmanager.TagManager;
import br.com.battlebits.commons.bukkit.util.BukkitUUID;
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
	private static BukkitMain instance;
	private boolean oldTag = false;
	@Setter
	private boolean tagControl = true;
	private PubSubListener pubSubListener;
	@Setter
	private boolean antiAfkEnabled = true;

	private boolean removePlayerDat = true;
	
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
		instance = this;
		new AutoUpdater(this, "vAPS4jf?&R_}E25T").run();
		Plugin plugin = getServer().getPluginManager().getPlugin("ViaVersion");
		if (plugin != null)
			new AutoUpdater(plugin, "Yw7~=#/7Uw(L:;QG").run();
		plugin = getServer().getPluginManager().getPlugin("ProtocolSupport");
		if (plugin != null)
			new AutoUpdater(plugin, "Y285jZEB<-CPs{8x").run();
		plugin = getServer().getPluginManager().getPlugin("ProtocolLib");
		if (plugin != null)
			new AutoUpdater(plugin, "LV3SMrVwM_-BR~q7").run();
		plugin = getServer().getPluginManager().getPlugin("ProtocolSupportLegacyHologram");
		if (plugin != null)
			new AutoUpdater(plugin, "r'E9q(@P?2dVc4Ng").run();
		plugin = this;
		procotolManager = ProtocolLibrary.getProtocolManager();
		new TranslationInjector().inject(this);
		new ActionItemInjector().inject(this);
		new ServerInfoInjector().inject(this);
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
			BattlebitsAPI.setCommonsMongo(mongoBackend);
			BattlebitsAPI.setCommonsRedis(redisBackend);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ProtocolHook.hook();
		BattlebitsAPI.setGetter(new BukkitUUID());
		BattlebitsAPI.setLogger(getLogger());
		BattlebitsAPI.setServerAddress(Bukkit.getIp() + ":" + Bukkit.getPort());
		BattlebitsAPI.setServerId(DataServer.getServerId(BattlebitsAPI.getServerAddress()));
		BattlebitsAPI.getLogger().info("Battlebits Server carregado. ServerId: " + BattlebitsAPI.getServerId());
		DataServer.newServer(Bukkit.getMaxPlayers());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, BattlebitsAPI.getBungeeChannel());
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordMessenger());
		for (Language lang : Language.values()) {
			Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
		}
		registerListeners();
		registerCommonManagement();
		enableCommonManagement();
		getServer().getScheduler().runTaskAsynchronously(this,
				pubSubListener = new PubSubListener(new BukkitPubSubHandler(), "account-field", "clan-field", "party-field", "party-action"));
		getServer().getScheduler().runTaskTimer(this, new UpdateScheduler(), 1, 1);
		try {
			new CommandLoader(new BukkitCommandFramework(this))
					.loadCommandsFromPackage(getFile(), "br.com.battlebits.commons.bukkit.command.register");
		} catch (Exception e) {
			BattlebitsAPI.getLogger().warning("Erro ao carregar o commandFramework!");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		DataServer.stopServer();
		BattlebitsAPI.getCommonsMongo().closeConnection();
		BattlebitsAPI.getCommonsRedis().closeConnection();
	}

	private void loadConfiguration() {
		saveDefaultConfig();
		removePlayerDat = getConfig().getBoolean("remove-player-dat", false);
		
		mongoHostname = getConfig().getString("mongo.hostname", "localhost");
		mongoPort = getConfig().getInt("mongo.port", 27017);
		mongoDatabase = getConfig().getString("mongo.database", "");
		mongoUsername = getConfig().getString("mongo.username", "");
		mongoPassword = getConfig().getString("mongo.password", "");

		redisHostname = getConfig().getString("redis.hostname", "localhost");
		redisPassword = getConfig().getString("redis.password", "");
		redisPort = getConfig().getInt("redis.port", 6379);
	}

	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(new AntiAFK(), this);
		pm.registerEvents(new AccountListener(), this);
		pm.registerEvents(new ChatListener(), this);
		pm.registerEvents(new PlayerNBTListener(), this);
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new ScoreboardListener(), this);
	
		// APIs
		pm.registerEvents(new ActionItemListener(), this);
		pm.registerEvents(new BossBarAPI(), this);
		pm.registerEvents(new MenuListener(), this);
	}

	public static void broadcastMessage(String id, String[]... replace) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BattlePlayer bp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());
			if (bp != null) {
				Language lang = bp.getLanguage();
				player.sendMessage(T.t(lang, id, replace));
			}
		}
	}

	public static void broadcastMessage(String id, String[] target, String[] replace) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BattlePlayer bp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());
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
