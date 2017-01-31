package br.com.battlebits.commons.bungee.loadbalancer.server;

public class SkywarsServer extends MinigameServer {

	public SkywarsServer(String serverId, int onlinePlayers, boolean joinEnabled) {
		super(serverId, onlinePlayers, 12, joinEnabled);
		setState(MinigameState.PREGAME);
	}

	@Override
	public boolean canBeSelected() {
		return super.canBeSelected() && (getState() == MinigameState.PREGAME && getTime() > 5);
	}

}
