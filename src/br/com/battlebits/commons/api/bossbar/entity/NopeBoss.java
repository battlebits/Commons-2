package br.com.battlebits.commons.api.bossbar.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import br.com.battlebits.commons.api.bossbar.BossBarEntity;

public class NopeBoss extends BossBarEntity {

	public NopeBoss(Player player) {
		super(player);
	}
	
	@Override
	public void spawn() {
		// Nope
	}

	@Override
	public void remove() {
		// Nope
	}

	@Override
	public void update() {
		// Nope
	}

	@Override
	public void move(PlayerMoveEvent event) {
		// Nope
	}
	
	@Override
	public boolean setHealth(float percent) {
		// Nope		
		return false;
	}
	
	@Override
	public boolean setTitle(String title) {
		// Nope
		return false;
	}
	
	@Override
	public boolean isAlive() {
		// Nope
		return false;
	}
	
	@Override
	public void setAlive(boolean alive) {
		// Nope
	}
}
