package br.com.battlebits.commons.bungee.listener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.manager.BanManager;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.core.data.DataClan;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.punish.Ban;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.util.GeoIpUtils;
import br.com.battlebits.commons.util.GeoIpUtils.IpCityResponse;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class AccountListener implements Listener {

	@EventHandler(priority = -128)
	public void onLogin(final LoginEvent event) {
		BattlebitsAPI.debug("ACCOUNT > STARTING");
		event.registerIntent(BungeeMain.getPlugin());
		final String userName = event.getConnection().getName();
		final InetSocketAddress ipAdress = event.getConnection().getAddress();
		final UUID uuid = event.getConnection().getUniqueId();
		ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.getPlugin(), new Runnable() {
			@Override
			public void run() {
				String countryCode = "-";
				String timeZoneCode = "0";
				try {
					IpCityResponse responde = GeoIpUtils.getIpStatus(ipAdress.getHostString());
					if (responde != null)
						countryCode = responde.getCountryCode();
					timeZoneCode = responde.getTimeZone();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				BattlebitsAPI.debug("CONNECTION > STARTING");
				try {
					BattlePlayer player = DataPlayer.getRedisPlayer(uuid);
					if (player == null) {
						BattlebitsAPI.debug("CONNECTION > TRYING MONGO");
						player = DataPlayer.createIfNotExistMongo(uuid, userName, ipAdress.getHostString(), countryCode,
								timeZoneCode);
						DataPlayer.saveRedisPlayer(player);
						BattlebitsAPI.debug("CONNECTION > MONGO SUCCESS");
					} else {
						DataPlayer.checkCache(uuid);
						BattlebitsAPI.debug("CONNECTION > REDIS FOUND");
					}
					BattlebitsAPI.debug("CONNECTION > JOIN DATA");
					player.setJoinData(userName, ipAdress.getHostString(), countryCode, timeZoneCode);
					BattlebitsAPI.debug("CONNECTION > JOINED");
					BattlebitsAPI.getAccountCommon().loadBattlePlayer(uuid, player);
					if (player.getClanUniqueId() != null) {
						try {
							Clan clan = BattlebitsAPI.getClanCommon().getClan(player.getClanUniqueId());
							if (clan == null) {
								clan = DataClan.getRedisClan(player.getClanUniqueId());
								if (clan == null) {
									clan = DataClan.getMongoClan(player.getClanUniqueId());
									if (clan != null) {
										DataClan.saveRedisClan(clan);
									}
								} else {
									DataClan.checkCache(player.getClanUniqueId());
								}
								if (clan != null) {
									BattlebitsAPI.getClanCommon().loadClan(clan);
								}
							}
							if (clan != null) {
								if (!clan.isParticipant(player))
									player.setClanUniqueId(null);
								if (clan != null) {
									clan.updatePlayer(player);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					BattlebitsAPI.debug("ACCOUNT > CLOSE");
				} catch (Exception e) {
					event.setCancelled(true);
					String accountLoadFailed = T.t(BattlebitsAPI.getDefaultLanguage(), "account-load-failed");
					event.setCancelReason(accountLoadFailed);
					// e.printStackTrace();
					return;
				}
				BattlebitsAPI.debug("BANNING > STARTING");
				BattlePlayer player = BattlebitsAPI.getAccountCommon()
						.getBattlePlayer(event.getConnection().getUniqueId());

				if (player.getPunishHistoric() != null)
					if (player.getPunishHistoric().getActualBan() == null) {
						Entry<UUID, Ban> ipBan = BungeeMain.getPlugin().getBanManager()
								.getIpBan(ipAdress.getHostString());
						if (ipBan != null) {
							if (!ipBan.getKey().equals(player.getUniqueId()))
								BungeeMain.getPlugin().getBanManager().ban(player, new Ban("CONSOLE",
										ipAdress.getHostString(), "proxy", T.t(player.getLanguage(), "alt-account")));
						}
					}
				Ban ban = null;
				if (player.getPunishHistoric() != null)
					ban = player.getPunishHistoric().getActualBan();
				if (ban != null) {
					event.setCancelled(true);
					event.setCancelReason(
							BanManager.getBanKickMessage(ban, player.getLanguage(), player.getTimeZone()));
				}
				BattlebitsAPI.debug("BANNING > FINISHED");
				player.checkForMultipliers();
				event.completeIntent(BungeeMain.getPlugin());
				DataServer.joinPlayer(BattlebitsAPI.getServerId(), uuid);
				player = null;
				ban = null;
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerDisconnectEvent event) {
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(event.getPlayer().getUniqueId());
		player.setLeaveData();
		if (player.getClan() != null) {
			Clan clan = player.getClan();
			boolean removeClan = true;
			for (UUID uuid : clan.getParticipants().keySet()) {
				if (uuid == player.getUniqueId())
					continue;
				ProxiedPlayer p = BungeeMain.getPlugin().getProxy().getPlayer(uuid);
				if (p != null) {
					removeClan = false;
					break;
				}
			}
			if (removeClan) {
				DataClan.cacheRedisClan(clan.getUniqueId(), clan.getName());
				BattlebitsAPI.getClanCommon().unloadClan(player.getClanUniqueId());
			}
		}
		DataPlayer.cacheRedisPlayer(player.getUniqueId());
		DataServer.leavePlayer(BattlebitsAPI.getServerId(), player.getUniqueId());
		BattlebitsAPI.getAccountCommon().unloadBattlePlayer(player.getUniqueId());
		player = null;
	}

}