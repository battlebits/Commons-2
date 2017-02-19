package br.com.battlebits.commons.bukkit.account;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.event.account.PlayerChangeLeagueEvent;
import br.com.battlebits.commons.bukkit.event.account.PlayerChangeTagEvent;
import br.com.battlebits.commons.bukkit.event.account.PlayerLanguageEvent;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.account.League;
import br.com.battlebits.commons.core.account.Tag;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.util.GeoIpUtils.IpCityResponse;
import lombok.Getter;
import lombok.Setter;

public class BukkitPlayer extends BattlePlayer {
	private UUID lastTellUUID;
	private ArrayList<Tag> tags;
	@Getter
	@Setter
	protected transient boolean cacheOnQuit;

	public BukkitPlayer(String name, UUID uniqueId, String hostName, IpCityResponse response) {
		super(name, uniqueId, hostName, response);
	}

	@Override
	public boolean setTag(Tag tag) {
		return setTag(tag, false);
	}

	public boolean setTag(Tag tag, boolean forcetag) {
		if (!tags.contains(tag) && !forcetag) {
			tag = getDefaultTag();
		}
		PlayerChangeTagEvent event = new PlayerChangeTagEvent(getBukkitPlayer(), getTag(), tag, forcetag);
		BukkitMain.getInstance().getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			if (!forcetag)
				super.setTag(tag);
		}
		return !event.isCancelled();
	}

	public Tag getDefaultTag() {
		return tags.get(0);
	}

	@Override
	public void setXp(int xp) {
		super.setXp(xp);
		boolean upLiga = false;
		if (getXp() >= getLeague().getMaxXp()) {
			upLiga = true;
			xp = getXp() - getLeague().getMaxXp();
		}
		if (upLiga) {
			setLeague(getLeague().getNextLeague());
			setXp(xp);
		}
	}

	@Override
	public void setLeague(League liga) {
		PlayerChangeLeagueEvent event = new PlayerChangeLeagueEvent(getBukkitPlayer(), this, getLeague(), liga);
		BukkitMain.getInstance().getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			super.setLeague(liga);
		}
	}

	public void loadTags() {
		tags = new ArrayList<>();
		for (Tag t : Tag.values()) {
			if (t == Tag.TORNEIO)
				if (getTournament() != null && getTournament() == BattlebitsAPI.getTournament()) {
					tags.add(0, t);
					continue;
				}
			if ((t.isExclusive()
					&& (t.getGroupToUse() == getServerGroup() || getServerGroup().ordinal() >= Group.ADMIN.ordinal()))
					|| (!t.isExclusive() && getServerGroup().ordinal() >= t.getGroupToUse().ordinal())) {
				tags.add(t);
			}
		}
	}

	@Override
	public void setJoinData(String userName, String ipAdrress, IpCityResponse response) {
		super.setJoinData(userName, ipAdrress, response);
		loadTags();
		if (getTag() == Tag.STAFF || !hasGroupPermission(Group.YOUTUBER))
			setTag(getDefaultTag());
	}

	@Override
	public void setLanguage(Language language) {
		super.setLanguage(language);
		Bukkit.getPluginManager().callEvent(new PlayerLanguageEvent(getBukkitPlayer(), this, language));
	}

	public UUID getLastTellUUID() {
		return lastTellUUID;
	}

	public void setLastTellUUID(UUID lastTellUUID) {
		this.lastTellUUID = lastTellUUID;
	}

	public boolean hasLastTell() {
		return this.lastTellUUID != null;
	}

	public ArrayList<Tag> getTags() {
		return tags;
	}

	public Player getBukkitPlayer() {
		return Bukkit.getPlayer(getUniqueId());
	}
}
