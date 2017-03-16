package br.com.battlebits.commons.api.bossbar.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.boss.ViaBossBar;

public class FakeBoss extends EntityBoss 
{
	private ViaBossBar bossBar;
	
	public FakeBoss(Player player)
	{
		super(player);
	}
	
	@Override
	public void spawn() 
	{
		if (bossBar == null)
		{
			bossBar = new ViaBossBar("", 200F, BossColor.PINK, BossStyle.SOLID);		
			bossBar.addPlayer(getPlayer());
		}
	}
	
	@Override
	public void remove()
	{
		if (bossBar != null)
		{
			bossBar.removePlayer(getPlayer());
			bossBar = null;
		}
	}
	
	@Override
	public void update() 
	{
		if (bossBar != null)
		{
			bossBar.setTitle(getDisplayName());
			bossBar.setHealth(getHealth());
		}
	}
	
	@Override
	public void move(PlayerMoveEvent event)
	{
		// Unnecessary for version 1.9+
	}
}
