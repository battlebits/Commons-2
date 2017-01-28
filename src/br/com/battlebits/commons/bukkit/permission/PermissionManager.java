package br.com.battlebits.commons.bukkit.permission;

import br.com.battlebits.commons.bukkit.BukkitCommon;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.permission.injector.PermissionMatcher;
import br.com.battlebits.commons.bukkit.permission.injector.RegExpMatcher;
import br.com.battlebits.commons.bukkit.permission.injector.regexperm.RegexPermissions;
import br.com.battlebits.commons.bukkit.permission.listener.PermissionListener;
import br.com.battlebits.commons.core.server.ServerType;

public class PermissionManager extends BukkitCommon {

	private RegexPermissions regexPerms;
	protected PermissionMatcher matcher = new RegExpMatcher();
	protected PermissionListener superms;

	public PermissionManager(BukkitMain main) {
		super(main);
	}

	@Override
	public void onEnable() {
		registerListener(superms = new PermissionListener(this));
		regexPerms = new RegexPermissions(this);
	}

	@Override
	public void onDisable() {
		if (this.regexPerms != null) {
			this.regexPerms.onDisable();
			this.regexPerms = null;
		}
		if (this.superms != null) {
			this.superms.onDisable();
			this.superms = null;
		}
	}

	public RegexPermissions getRegexPerms() {
		return regexPerms;
	}

	public ServerType getServerType() {
		return ServerType.NETWORK;
	}

	public PermissionMatcher getPermissionMatcher() {
		return this.matcher;
	}
}
