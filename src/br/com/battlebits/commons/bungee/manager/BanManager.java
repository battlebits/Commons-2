package br.com.battlebits.commons.bungee.manager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.punish.Ban;
import br.com.battlebits.commons.core.punish.Mute;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.core.twitter.Twitter;
import br.com.battlebits.commons.core.twitter.TwitterAccount;
import br.com.battlebits.commons.util.DateUtils;
import br.com.battlebits.commons.util.timezone.TimeZone;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import twitter4j.TwitterException;

public class BanManager {
	private Cache<String, Entry<UUID, Ban>> banCache;

	public BanManager() {
		banCache = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES)
				.build(new CacheLoader<String, Entry<UUID, Ban>>() {
					@Override
					public Entry<UUID, Ban> load(String name) throws Exception {
						return null;
					}
				});
	}

	public void ban(BattlePlayer player, Ban ban) {
		player.getPunishHistoric().getBanHistory().add(ban);
		for (ProxiedPlayer proxiedP : BungeeMain.getPlugin().getProxy().getPlayers()) {
			BattlePlayer pl = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedP.getUniqueId());
			if (pl.hasGroupPermission(Group.TRIAL) && pl.getConfiguration().isAlertsEnabled()) {
				String banSuccess = "";
				if (ban.isPermanent()) {
					banSuccess = T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-ban-prefix") + " "
							+ T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-ban-success");
				} else {
					banSuccess = T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-tempban-prefix") + " "
							+ T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-tempban-success");
				}
				banSuccess = banSuccess.replace("%player%",
						player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
				banSuccess = banSuccess.replace("%banned-By%", ban.getBannedBy());
				banSuccess = banSuccess.replace("%reason%", ban.getReason());
				banSuccess = banSuccess.replace("%duration%",
						DateUtils.formatDifference(pl.getLanguage(), ban.getDuration() / 1000));
				proxiedP.sendMessage(TextComponent.fromLegacyText(banSuccess));
			}
		}
		if (ban.isPermanent()) {
			// TODO BanPlayer in Reports
			try {
				Twitter.tweet(TwitterAccount.BATTLEBANS,
						"Jogador banido: " + player.getName() + "\nBanido por: " + ban.getBannedBy() + "\nMotivo: "
								+ ban.getReason() + "\n\nServidor: " + player.getServerConnected());
			} catch (TwitterException e) {

			}
		}
		DataPlayer.saveBattlePlayer(player, "punishHistoric");
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			if (ban.isPermanent()) {
				if (player.getIpAddress() != null)
					banCache.put(player.getIpAddress(),
							new AbstractMap.SimpleEntry<UUID, Ban>(player.getUniqueId(), ban));
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Ban");
				if (pPlayer.getServer() != null)
					pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
			}
			pPlayer.disconnect(getBanKickMessageBase(ban, player.getLanguage(), player.getTimeZone()));
		}
	}

	public void unban(BattlePlayer bannedByPlayer, BattlePlayer player, Ban ban) {
		if (bannedByPlayer != null)
			ban.unban(bannedByPlayer);
		else
			ban.unban();
		for (ProxiedPlayer proxiedP : BungeeMain.getPlugin().getProxy().getPlayers()) {
			BattlePlayer pl = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedP.getUniqueId());
			if (pl.hasGroupPermission(Group.TRIAL) && pl.getConfiguration().isAlertsEnabled()) {
				String unbanSuccess = T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-unban-prefix") + " "
						+ T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-unban-success");
				unbanSuccess = unbanSuccess.replace("%player%",
						player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
				unbanSuccess = unbanSuccess.replace("%unbannedBy%", ban.getUnbannedBy());
				proxiedP.sendMessage(TextComponent.fromLegacyText(unbanSuccess));
			}
		}
		DataPlayer.saveBattlePlayer(player, "punishHistoric");
	}

	public void mute(BattlePlayer player, Mute mute) {
		player.getPunishHistoric().getMuteHistory().add(mute);
		for (ProxiedPlayer proxiedP : BungeeMain.getPlugin().getProxy().getPlayers()) {
			BattlePlayer pl = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedP.getUniqueId());
			if (pl.hasGroupPermission(Group.HELPER) && pl.getConfiguration().isAlertsEnabled()) {
				String banSuccess = "";
				if (mute.isPermanent()) {
					banSuccess = T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-mute-prefix") + " "
							+ T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-mute-success");
				} else {
					banSuccess = T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-tempmute-prefix") + " "
							+ T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-tempmute-success");
				}
				banSuccess = banSuccess.replace("%player%",
						player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
				banSuccess = banSuccess.replace("%muted-By%", mute.getMutedBy());
				banSuccess = banSuccess.replace("%reason%", mute.getReason());
				banSuccess = banSuccess.replace("%duration%",
						DateUtils.formatDifference(pl.getLanguage(), mute.getDuration() / 1000));
				proxiedP.sendMessage(TextComponent.fromLegacyText(banSuccess));
			}
		}
		if (mute.isPermanent()){
			// TODO Mute Player in Reports
		}
		DataPlayer.saveBattlePlayer(player, "punishHistoric");
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Mute");
			out.writeUTF(BattlebitsAPI.getGson().toJson(mute));
			if (pPlayer.getServer() != null)
				pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
		}
	}

	public void unmute(BattlePlayer mutedByPlayer, BattlePlayer player, Mute mute) {
		if (mutedByPlayer != null)
			mute.unmute(mutedByPlayer);
		else
			mute.unmute();
		for (ProxiedPlayer proxiedP : BungeeMain.getPlugin().getProxy().getPlayers()) {
			BattlePlayer pl = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedP.getUniqueId());
			if (pl.hasGroupPermission(Group.HELPER) && pl.getConfiguration().isAlertsEnabled()) {
				String unbanSuccess = T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-unmute-prefix") + " "
						+ T.t(BungeeMain.getPlugin(),pl.getLanguage(), "command-unmute-success");
				unbanSuccess = unbanSuccess.replace("%player%",
						player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
				unbanSuccess = unbanSuccess.replace("%unmutedBy%", mute.getUnmutedBy());
				proxiedP.sendMessage(TextComponent.fromLegacyText(unbanSuccess));
			}
		}
		DataPlayer.saveBattlePlayer(player, "punishHistoric");
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			if (mutedByPlayer != null) {
				out.writeUTF("Unmute");
				out.writeUTF(mutedByPlayer.getUniqueId().toString());
				out.writeUTF(mutedByPlayer.getName());
			} else {
				out.writeUTF("UnmuteConsole");
			}
			if (pPlayer.getServer() != null)
				pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
		}
	}

	public Entry<UUID, Ban> getIpBan(String address) {
		return banCache.asMap().get(address);
	}

	public static String getBanKickMessage(Ban ban, Language lang, TimeZone zone) {
		String reason = "";
		if (ban.isPermanent()) {
			reason = T.t(BungeeMain.getPlugin(),lang, "banned-permanent");
		} else {
			reason = T.t(BungeeMain.getPlugin(),lang, "banned-temp");
		}
		Date date = new Date(ban.getBanTime());
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		reason = reason.replace("%day%", df.format(date));
		reason = reason.replace("%banned-By%", ban.getBannedBy());
		reason = reason.replace("%reason%", ban.getReason());
		reason = reason.replace("%duration%",
				DateUtils.formatDifference(lang, (ban.getExpire() - System.currentTimeMillis()) / 1000));
		reason = reason.replace("%forum%", BattlebitsAPI.FORUM_WEBSITE);
		reason = reason.replace("%store%", BattlebitsAPI.STORE);
		return reason;
	}

	public static String getMuteMessage(Mute mute, Language lang) {
		String message = "";
		if (mute.isPermanent()) {
			message = T.t(BungeeMain.getPlugin(),lang, "command-mute-prefix") + " "
					+ T.t(BungeeMain.getPlugin(),lang, "command-mute-muted");
		} else {
			message = T.t(BungeeMain.getPlugin(),lang, "command-unmute-prefix") + " "
					+ T.t(BungeeMain.getPlugin(),lang, "command-tempmute-muted");
		}
		message = message.replace("%duration%",
				DateUtils.formatDifference(lang, (mute.getExpire() - System.currentTimeMillis()) / 1000));
		message = message.replace("%forum%", BattlebitsAPI.FORUM_WEBSITE);
		message = message.replace("%store%", BattlebitsAPI.STORE);
		message = message.replace("%muted-By%", mute.getMutedBy());
		message = message.replace("%reason%", mute.getReason());
		return message;
	}

	public static BaseComponent[] getBanKickMessageBase(Ban ban, Language lang, TimeZone zone) {
		return TextComponent.fromLegacyText(getBanKickMessage(ban, lang, zone));
	}
}
