package br.com.battlebits.commons.bungee.listener;

import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.manager.BanManager;
import br.com.battlebits.commons.bungee.party.BungeeParty;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.core.data.DataClan;
import br.com.battlebits.commons.core.data.DataParty;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.party.Party;
import br.com.battlebits.commons.core.punish.Ban;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.util.GeoIpUtils;
import br.com.battlebits.commons.util.GeoIpUtils.IpCityResponse;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class AccountListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(final LoginEvent event) {
		BattlebitsAPI.debug("ACCOUNT > STARTING");
		event.registerIntent(BungeeMain.getPlugin());
		final String userName = event.getConnection().getName();
		if(userName == null) {
			String accountLoadFailed = T.t(BungeeMain.getPlugin(),BattlebitsAPI.getDefaultLanguage(), "account-load-failed");
			event.setCancelReason(accountLoadFailed);
			return;
		}
		final InetSocketAddress ipAdress = event.getConnection().getAddress();
		final UUID uuid = event.getConnection().getUniqueId();
		if(uuid == null) {
			String accountLoadFailed = T.t(BungeeMain.getPlugin(),BattlebitsAPI.getDefaultLanguage(), "account-load-failed");
			event.setCancelReason(accountLoadFailed);
			return;
		}
		ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.getPlugin(), new Runnable() {
			@Override
			public void run() {
				BattlebitsAPI.debug("CONNECTION > STARTING");
				try {
					IpCityResponse response = GeoIpUtils.getIpStatus(ipAdress.getHostString());
					BattlePlayer player = DataPlayer.getRedisPlayer(uuid);
					if (player == null) {
						BattlebitsAPI.debug("CONNECTION > TRYING MONGO");
						player = DataPlayer.createIfNotExistMongo(uuid, userName, ipAdress.getHostString(), response);
						DataPlayer.saveRedisPlayer(player);
						BattlebitsAPI.debug("CONNECTION > MONGO SUCCESS");
					} else {
						BattlebitsAPI.debug("CONNECTION > REDIS FOUND");
					}
					DataPlayer.checkCache(uuid);
					BattlebitsAPI.debug("CONNECTION > JOIN DATA");
					player.setJoinData(userName, ipAdress.getHostString(), response);
					player.setServerConnectedType(ServerType.NONE);
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
					
					/* Party */
					Party party = BattlebitsAPI.getPartyCommon().getByOwner(uuid);
					if (party == null) {
						party = DataParty.getRedisParty(uuid, BungeeParty.class);
						if (party != null) {
							party.init();
							BattlebitsAPI.debug("REDIS > PARTY FOUND");
							BattlebitsAPI.getPartyCommon().loadParty(party);
							DataParty.checkCache(party);							
						} else {
							BattlebitsAPI.debug("REDIS > PARTY NOT FOUND");
						}
					}
					
					BattlebitsAPI.debug("ACCOUNT > CLOSE");
				} catch (Exception e) {
					event.setCancelled(true);
					String accountLoadFailed = T.t(BungeeMain.getPlugin(),BattlebitsAPI.getDefaultLanguage(), "account-load-failed");
					event.setCancelReason(accountLoadFailed);
					e.printStackTrace();
					event.completeIntent(BungeeMain.getPlugin());
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
										ipAdress.getHostString(), "proxy", T.t(BungeeMain.getPlugin(),player.getLanguage(), "alt-account")));
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
			}
		});
	}

	@EventHandler(priority = -127)
	public void onPostLoginCheck(PostLoginEvent event) {
		if (BattlebitsAPI.getAccountCommon().getBattlePlayer(event.getPlayer().getUniqueId()) == null) {
			event.getPlayer().disconnect(new TextComponent(T.t(BungeeMain.getPlugin(),BattlebitsAPI.getDefaultLanguage(), "account-load-failed")));
		}
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		BungeeMain.getPlugin().getProxy().getScheduler().runAsync(BungeeMain.getPlugin(), new Runnable() {
			@Override
			public void run() {
				UUID uuid = event.getPlayer().getUniqueId();
				if (BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid) != null) {
					
					/* Party */
					Party party = BattlebitsAPI.getPartyCommon().getByOwner(uuid);
					if (party == null) {
						party = BattlebitsAPI.getPartyCommon().getParty(uuid);
						if (party != null) party.onMemberJoin(uuid);
					} else {
						party.onOwnerJoin();
					}
					
					DataServer.joinPlayer(event.getPlayer().getUniqueId());
				}
			}
		});
	}
 
	@EventHandler
	public void onQuit(PlayerDisconnectEvent event) {
		removePlayer(event.getPlayer().getUniqueId());
	}

	private void removePlayer(UUID uuid) {
		BungeeMain.getPlugin().getProxy().getScheduler().runAsync(BungeeMain.getPlugin(), new Runnable() {
			@Override
			public void run() {
								
				/* Party */
				Party party = BattlebitsAPI.getPartyCommon().getByOwner(uuid);
				if (party == null) {
					party = BattlebitsAPI.getPartyCommon().getParty(uuid);
					if (party != null) party.onMemberLeave(uuid);
				} else {
					party.onOwnerLeave();
					DataParty.expire(party);
				}
				
				BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
				if (player != null) {
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
					DataServer.leavePlayer(player.getUniqueId());
					BattlebitsAPI.getAccountCommon().unloadBattlePlayer(player.getUniqueId());
				}
			}
		});
	}
}