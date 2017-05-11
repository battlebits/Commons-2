package br.com.battlebits.commons.bukkit.party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.party.Party;
import br.com.battlebits.commons.core.translate.T;

public class BukkitParty extends Party {
	public BukkitParty(UUID owner) {
		super(owner);
	}

	@Override
	public void init() {
	}

	@Override
	public void onOwnerJoin() {
	}

	@Override
	public void onOwnerLeave() {
	}

	@Override
	public void onMemberJoin(UUID member) {
	}

	@Override
	public void onMemberLeave(UUID member) {
	}

	@Override
	public void sendMessage(String prefix, String id, String[]... replace) {
		for (UUID uuid : Stream.concat(Stream.of(getOwner()), getMembers().stream()).toArray(UUID[]::new)) {
			Player player = Bukkit.getPlayer(uuid);

			if (player != null) {
				player.sendMessage(prefix + T.t(BukkitMain.getInstance(), BattlePlayer.getLanguage(uuid), id, replace));
			}
		}
	}

	public Player getBukkitOwner() {
		return Bukkit.getPlayer(getOwner());
	}

	public Set<Player> getBukkitMembers() {
		Set<Player> members = new HashSet<>();

		for (UUID uuid : getMembers()) {
			Player member = Bukkit.getPlayer(uuid);

			if (member != null)
				members.add(member);
		}

		return members;
	}

	@Override
	public int getOnlineCount() {
		int count = 0;

		if (Bukkit.getPlayer(getOwner()) != null)
			count++;

		for (UUID uuid : getMembers())
			if (Bukkit.getPlayer(uuid) != null)
				count++;

		return count;
	}
}
