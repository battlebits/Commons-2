package br.com.battlebits.commons.bukkit.listener;

import java.net.InetAddress;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.vanish.VanishAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.event.account.PlayerChangeGroupEvent;
import br.com.battlebits.commons.bukkit.event.account.PlayerUpdatedFieldEvent;
import br.com.battlebits.commons.bukkit.party.BukkitParty;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.core.data.DataClan;
import br.com.battlebits.commons.core.data.DataParty;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.party.Party;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.util.GeoIpUtils;
import br.com.battlebits.commons.util.GeoIpUtils.IpCityResponse;

public class AccountListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public synchronized void onAsync(AsyncPlayerPreLoginEvent event) {
		if (Bukkit.getPlayer(event.getUniqueId()) != null) {
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage("Already online");
			return;
		}
		String userName = event.getName();
		InetAddress ipAdress = event.getAddress();
		UUID uuid = event.getUniqueId();
		BattlebitsAPI.debug("CONNECTION > STARTING");
		try {
			IpCityResponse response = GeoIpUtils.getIpStatus(ipAdress.getHostAddress());
			BukkitPlayer player = DataPlayer.getRedisBukkitPlayer(uuid);
			if (player == null) {
				player = DataPlayer.createIfNotExistMongoBukkit(uuid, userName, ipAdress.getHostAddress(), response);
				DataPlayer.saveRedisPlayer(player);
				player.setCacheOnQuit(true);
				BattlebitsAPI.debug("CONNECTION > TRYING MONGO");
			} else {
				if (DataPlayer.checkCache(uuid))
					player.setCacheOnQuit(true);
				BattlebitsAPI.debug("CONNECTION > REDIS FOUND");
			}
			player.connect(BattlebitsAPI.getServerId(), BattlebitsAPI.getServerType());
			player.setJoinData(userName, ipAdress.getHostAddress(), response);
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
							if (DataClan.checkCache(player.getClanUniqueId()))
								clan.setCacheOnQuit(true);
						}
						if (clan != null) {
							BattlebitsAPI.getClanCommon().loadClan(clan);
						}
					}
					if (clan != null) {
						if (!clan.isParticipant(player))
							player.setClanUniqueId(null);
						if (player.getClanUniqueId() != null) {
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
				party = DataParty.getRedisParty(uuid, BukkitParty.class);
				if (party != null)
					BattlebitsAPI.getPartyCommon().loadParty(party);
			}

			BattlebitsAPI.debug("ACCOUNT > CLOSE");
		} catch (Exception e) {
			event.setKickMessage(T.t(BukkitMain.getInstance(), BattlebitsAPI.getDefaultLanguage(), "account-load-failed"));
			e.printStackTrace();
			return;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRemoveAccount(AsyncPlayerPreLoginEvent event) {
		if (BattlebitsAPI.getAccountCommon().getBattlePlayer(event.getUniqueId()) == null) {
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage(T.t(BukkitMain.getInstance(), BattlebitsAPI.getDefaultLanguage(), "account-not-load"));
		}
		if (event.getLoginResult() != Result.ALLOWED) {
			removePlayer(event.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onLogin(PlayerLoginEvent event) {
		if (event.getResult() != org.bukkit.event.player.PlayerLoginEvent.Result.ALLOWED)
			return;
		if (BattlePlayer.getPlayer(event.getPlayer().getUniqueId()) == null) {
			event.disallow(org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER, T.t(BukkitMain.getInstance(), BattlebitsAPI.getDefaultLanguage(), "account-not-load"));
			return;
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				DataServer.joinPlayer(event.getPlayer().getUniqueId());
			}
		}.runTaskAsynchronously(BukkitMain.getInstance());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();

		/* Party */
		Party party = BattlebitsAPI.getPartyCommon().getByOwner(uuid);
		if (party == null) {
			party = BattlebitsAPI.getPartyCommon().getParty(uuid);
			if (party != null)
				party.onMemberJoin(uuid);
		} else {
			party.onOwnerJoin();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				UUID uuid = event.getPlayer().getUniqueId();

				/* Party */
				BukkitParty party = (BukkitParty) BattlebitsAPI.getPartyCommon().getByOwner(uuid);
				if (party == null) {
					party = (BukkitParty) BattlebitsAPI.getPartyCommon().getParty(uuid);
					if (party != null) {
						party.onMemberLeave(uuid);
						if (party.getOnlineCount() == 0)
							BattlebitsAPI.getPartyCommon().removeParty(party);
					}
				} else {
					party.onOwnerLeave();
					if (party.getOnlineCount() == 0)
						BattlebitsAPI.getPartyCommon().removeParty(party);
				}

				removePlayer(uuid);
			}
		}.runTaskAsynchronously(BukkitMain.getInstance());
	}

	private void removePlayer(UUID uniqueId) {
		BukkitPlayer player = (BukkitPlayer) BattlePlayer.getPlayer(uniqueId);
		if (player == null)
			return;
		if (player.getClan() != null) {
			Clan clan = player.getClan();
			boolean removeClan = true;
			for (UUID uuid : clan.getParticipants().keySet()) {
				if (uuid == player.getUniqueId())
					continue;
				Player p = Bukkit.getPlayer(uuid);
				if (p != null && p.isOnline()) {
					removeClan = false;
				}
			}
			if (removeClan) {
				if (clan.isCacheOnQuit())
					DataClan.cacheRedisClan(clan.getUniqueId(), clan.getName());
				BattlebitsAPI.getClanCommon().unloadClan(player.getClanUniqueId());
			}
		}
		if (player.isCacheOnQuit())
			DataPlayer.cacheRedisPlayer(uniqueId);
		DataServer.leavePlayer(player.getUniqueId());
		BattlebitsAPI.getAccountCommon().unloadBattlePlayer(uniqueId);
	}

	@EventHandler
	public void onUpdate(PlayerUpdatedFieldEvent event) {
		BukkitPlayer player = event.getBukkitPlayer();
		switch (event.getField()) {
		case "clanUniqueId":
			if (event.getObject() != null)
				BattlebitsAPI.getClanCommon().loadClan(DataClan.getClan(player.getClanUniqueId()));
			else if (player.getClanUniqueId() != null && player.getClan() != null) {
				Clan clan = player.getClan();
				boolean removeClan = true;
				for (UUID uuid : clan.getParticipants().keySet()) {
					if (uuid == player.getUniqueId())
						continue;
					Player p = Bukkit.getPlayer(uuid);
					if (p != null && p.isOnline()) {
						removeClan = false;
					}
				}
				if (removeClan) {
					if (clan.isCacheOnQuit())
						DataClan.cacheRedisClan(clan.getUniqueId(), clan.getName());
					BattlebitsAPI.getClanCommon().unloadClan(player.getClanUniqueId());
				}
			}
			break;
		case "groups":
		case "ranks":
			player.loadTags();
			player.setTag(player.getDefaultTag());
			VanishAPI.getInstance().updateVanishToPlayer(event.getPlayer());
			Bukkit.getPluginManager().callEvent(new PlayerChangeGroupEvent(event.getPlayer(), player, player.getServerGroup()));
			break;
		default:
			break;
		}
	}
}
