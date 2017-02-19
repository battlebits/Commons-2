package br.com.battlebits.commons.core.server.loadbalancer.server;

import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MinigameServer extends BattleServer {

	@Getter
	@Setter
	private int time;
	private MinigameState state;

	public MinigameServer(String serverId, Set<UUID> onlinePlayers, int maxPlayers, boolean joinEnabled) {
		super(serverId, onlinePlayers, 100, joinEnabled);
		this.state = MinigameState.WAITING;
	}

	@Override
	public int getActualNumber() {
		return super.getActualNumber();
	}

	public abstract boolean isInProgress();

}
