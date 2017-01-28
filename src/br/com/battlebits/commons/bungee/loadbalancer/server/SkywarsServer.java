package br.com.battlebits.commons.bungee.loadbalancer.server;

public class SkywarsServer extends BattleServer {

	private int tempo;
	private SkywarsState state;

	public SkywarsServer(String serverId, int onlinePlayers, boolean joinEnabled) {
		super(serverId, onlinePlayers, 100, joinEnabled);
		this.state = SkywarsState.PREGAME;
	}

	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	public int getTempo() {
		return tempo;
	}

	public SkywarsState getState() {
		return state;
	}

	public void setState(SkywarsState state) {
		this.state = state;
	}

	@Override
	public int getActualNumber() {
		return super.getActualNumber();
	}

	@Override
	public boolean canBeSelected() {
		return super.canBeSelected() && (state == SkywarsState.PREGAME && tempo > 5);
	}

	public static enum SkywarsState {
		PREGAME, PREPARING, INGAME, ENDING;
	}

}
