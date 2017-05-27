package br.com.battlebits.commons.core.permission;

import br.com.battlebits.commons.core.permission.group.*;

public enum Group {

	NORMAL, //
	LIGHT, //
	PREMIUM, //
	ULTIMATE, //
	YOUTUBER, //
	BUILDER, //
	STAFF, //
	HELPER, //
	DEV, //
	YOUTUBERPLUS(new StreamerGroup()), //
	TRIAL(new ModeratorGroup()), //
	MOD(new ModeratorGroup()), //
	MODPLUS(new StreamerGroup()), //
	MANAGER(new StreamerGroup()), //
	ADMIN(new OwnerGroup()), //
	DONO(new OwnerGroup());

	private GroupInterface group;

    Group() {
		this(new SimpleGroup());
	}

    Group(GroupInterface group) {
		this.group = group;
	}

	public GroupInterface getGroup() {
		return group;
	}

}
