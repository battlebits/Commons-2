package br.com.battlebits.commons.bungee.party;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.party.Party;
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
	
	@Getter
	protected Map<UUID, Integer> inviteQueue = new HashMap<>();
	protected Map<UUID, Integer> memberLeave = new HashMap<>();

	public BungeeParty(UUID owner)
	{
		super(owner);		
	}
	
	@Override
	public void init()
	{
		inviteQueue = new HashMap<>();
		memberLeave = new HashMap<>();
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
			BattlebitsAPI.getPartyCommon().removeParty(getOwner());
			
			memberLeave.values().forEach(t -> ProxyServer.getInstance().getScheduler().cancel(t));

			ownerLeave = null;

		}, 3L, TimeUnit.MINUTES);
	}
	
	@Override
	public void onMemberJoin(UUID member)
	{
		if (memberLeave.containsKey(member))
		{
			int task = memberLeave.remove(member);
			
			ProxyServer.getInstance().getScheduler().cancel(task);
		}
	}
	
	@Override
	public void onMemberLeave(UUID member)
	{
		memberLeave.computeIfAbsent(member, t -> ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getPlugin(), () -> 
		{
			memberLeave.remove(member);
			
			removeMember(member);
			
		}, 1L, TimeUnit.MINUTES).getId());
	}
	
	public void addInvite(ProxiedPlayer target)
	{
		inviteQueue.computeIfAbsent(target.getUniqueId(), t -> ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getPlugin(), () ->
		{
			if (inviteQueue.containsKey(target.getUniqueId()))
			{
				inviteQueue.remove(target.getUniqueId());
			}
			
		}, 1L, TimeUnit.MINUTES).getId());
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

	@Override
	public int getOnlineCount()
	{
		int count = 0;
		
		if (ProxyServer.getInstance().getPlayer(getOwner()) != null)
			count++;
		
		for (UUID uuid : getMembers())
			if (ProxyServer.getInstance().getPlayer(uuid) != null)
				count++;
		
		return count;
	}
}
