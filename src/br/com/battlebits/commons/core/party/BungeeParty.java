package br.com.battlebits.commons.core.party;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.T;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeeParty extends Party
{	
	@Setter
	@Getter
	protected transient boolean cacheOnQuit = false;
	protected transient ScheduledTask ownerLeave;
	protected transient Map<UUID, ScheduledTask> memberLeave = new HashMap<>();
	
	@Getter
	protected transient Map<UUID, ScheduledTask> inviteQueue = new HashMap<>();
 	
	public BungeeParty(UUID owner)
	{
		super(owner);
	}

	@Override
	public void onOwnerJoin()
	{
		if (ownerLeave != null) ownerLeave.cancel();
	}

	@Override
	public void onOwnerLeave()
	{
		ownerLeave = ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getPlugin(), () ->
		{
			ownerLeave = null;
			
			memberLeave.values().forEach(t -> t.cancel());
			
			BattlebitsAPI.getPartyCommon().removeParty(getOwner());

		}, 3, TimeUnit.MINUTES);
	}
	
	@Override
	public void onMemberJoin(UUID member)
	{
		if (memberLeave.containsKey(member))
		{
			ScheduledTask task = memberLeave.remove(member);
			
			if (task != null) task.cancel();
		}
	}
	
	@Override
	public void onMemberLeave(UUID member)
	{
		if (!memberLeave.containsKey(member))
		{
			memberLeave.put(member, ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getPlugin(), () ->
			{
				memberLeave.remove(member);
				
				removeMember(member);
				
			}, 1, TimeUnit.MINUTES));
		}
	}
	
	public void addInvite(ProxiedPlayer target)
	{
		inviteQueue.put(target.getUniqueId(), ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getPlugin(), () -> 
		{
			if (inviteQueue.containsKey(target.getUniqueId()))
			{
				inviteQueue.remove(target.getUniqueId());
			}
				
		}, 1, TimeUnit.MINUTES));
	}

	@Override
	public void sendMessage(String id, String[]... replace)
	{
		for (UUID uuid : Stream.concat(Stream.of(getOwner()), getMembers().stream()).toArray(UUID[]::new))
		{
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
			
			if (player != null)
			{
				player.sendMessage(TextComponent.fromLegacyText(T.t(BattlePlayer.getLanguage(uuid), id, replace)));
			}
		}
	}
	
	
	
	public ProxiedPlayer getBungeeOwner()
	{
		return ProxyServer.getInstance().getPlayer(getOwner());
	}
	
	public Set<ProxiedPlayer> getBungeeMembers()
	{
		Set<ProxiedPlayer> members = new HashSet<>();
		
		for (UUID uuid : getMembers())
		{
			ProxiedPlayer member = ProxyServer.getInstance().getPlayer(uuid);
			
			if (member != null) members.add(member);
		}
		
		return members;
	}
}
