package br.com.battlebits.commons.core.loadbalancer.server;

public class HungerGamesServer extends MinigameServer {

	public HungerGamesServer(String serverId, int onlinePlayers, boolean joinEnabled) {
		super(serverId, onlinePlayers, 100, joinEnabled);
		setState(MinigameState.WAITING);
	}

	@Override
	public boolean canBeSelected() {
		return super.canBeSelected() && getState() != MinigameState.INVENCIBILITY
				&& getState() != MinigameState.GAMETIME
				&& ((getState() == MinigameState.PREGAME && getTime() >= 15) || getState() == MinigameState.WAITING);
	}

}
