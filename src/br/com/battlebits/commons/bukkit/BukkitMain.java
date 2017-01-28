package br.com.battlebits.commons.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.command.BukkitCommandFramework;
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
import lombok.Getter;
import lombok.Setter;

@Getter
public class BukkitMain extends JavaPlugin {
	private ProtocolManager procotolManager;
	private PermissionManager permissionManager;
	private TagManager tagManager;
	@Getter
	private static BukkitMain plugin;
	private PubSubListener pubSubListener;
	private boolean oldTag = false;
	@Setter
	private boolean tagControl = true;

	@Override
	public void onLoad() {
		plugin = this;
		procotolManager = ProtocolLibrary.getProtocolManager();
		new TranslationInjector().inject(this);
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
		BattlebitsAPI.setServerId(Bukkit.getIp() + ":" + Bukkit.getPort());
		BattlebitsAPI.setLogger(getLogger());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, BattlebitsAPI.getBungeeChannel());
		for (Language lang : Language.values()) {
			Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
		}
		registerListeners();
		registerCommonManagement();
		enableCommonManagement();
		pubSubListener = new PubSubListener(new BukkitPubSubHandler());
		getServer().getScheduler().runTaskAsynchronously(this, pubSubListener);
		// this.getServer().getMessenger().registerIncomingPluginChannel(this,
		// BattlebitsAPI.getBungeeChannel(), new MessageListener());
		getServer().getScheduler().runTaskTimer(this, new UpdateScheduler(), 1, 1);
		try {
			new CommandLoader(new BukkitCommandFramework(plugin))
					.loadCommandsFromPackage("br.com.battlebits.commons.bukkit.command.register");
		} catch (Exception e) {
			BattlebitsAPI.getLogger().warning("Erro ao carregar o commandFramework!");
			e.printStackTrace();
		}
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new AntiAFK(), this);
		getServer().getPluginManager().registerEvents(new AccountListener(), this);
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new ScoreboardListener(), this);
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

}
