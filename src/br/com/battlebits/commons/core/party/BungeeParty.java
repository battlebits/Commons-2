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
	
	protected Map<UUID, Integer> memberLeave = new HashMap<>();
	
	@Getter
	protected Map<UUID, Integer> inviteQueue = new HashMap<>();

	public BungeeParty(UUID owner)
	{
		super(owner);
		
		BattlebitsAPI.getLogger().info("BungeeParty INSTANCE");
	}
	
	@Override
	public void init()
	{
		memberLeave = new HashMap<>();
		inviteQueue = new HashMap<>();
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
			
			memberLeave.values().forEach(t -> ProxyServer.getInstance().getScheduler().cancel(t));
			
			BattlebitsAPI.getPartyCommon().removeParty(getOwner());

		}, 3, TimeUnit.MINUTES);
	}
	
	@Override
	public void onMemberJoin(UUID member)
	{
		/**if (memberLeave == null) 
			System.out.println("memberLeave é null");
		else
			System.out.println("memberLeave não é null");
		
		if (member == null) 
			System.out.println("member é null");
		else
			System.out.println("member não é null");**/
		
		
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
			
		}, 1, TimeUnit.MINUTES).getId());
	}
	
	public void addInvite(ProxiedPlayer target)
	{
		getInviteQueue().computeIfAbsent(target.getUniqueId(), t -> ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getPlugin(), () ->
		{
			if (getInviteQueue().containsKey(target.getUniqueId()))
			{
				getInviteQueue().remove(target.getUniqueId());
			}
			
		}, 1, TimeUnit.MINUTES).getId());
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
