package br.com.battlebits.commons.core.server;

public enum ServerType {
	DOUBLEKITHG(ServerStaff.HUNGERGAMES), //
	FAIRPLAY(ServerStaff.HUNGERGAMES), //
	CUSTOMHG(ServerStaff.HUNGERGAMES), //
	HUNGERGAMES(ServerStaff.HUNGERGAMES), //
	PVP_FULLIRON(ServerStaff.BATTLECRAFT), //
	PVP_SIMULATOR(ServerStaff.BATTLECRAFT), //
	SKYWARS(ServerStaff.SKYWARS), //
	LOBBY(ServerStaff.LOBBY), //
	RAID(ServerStaff.RAID), //
	GARTICCRAFT(ServerStaff.GARTICCRAFT), //
	TESTSERVER(ServerStaff.TESTSERVER), //
	NETWORK(ServerStaff.NETWORK), //
	NONE(ServerStaff.NONE);

	private ServerStaff staffType;

	private ServerType(ServerStaff staffType) {
		this.staffType = staffType;
	}

	public ServerStaff getStaffType() {
		return staffType;
	}

	public String getName() {
		return getServerName(super.toString().toLowerCase());
	}

	private static String getServerName(String server) {
		String serverName = server;
		char[] stringArray = serverName.toCharArray();
		stringArray[0] = Character.toUpperCase(stringArray[0]);
		serverName = new String(stringArray);
		return serverName;
	}
}
