package br.com.battlebits.commons.core.account;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.friend.Blocked;
import br.com.battlebits.commons.core.friend.Friend;
import br.com.battlebits.commons.core.friend.Request;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.punish.PunishHistoric;
import br.com.battlebits.commons.core.report.Report;
import br.com.battlebits.commons.core.server.ServerStaff;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.util.timezone.TimeZone;
import br.com.battlebits.commons.util.timezone.TimeZoneConversor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class BattlePlayer {

	// INFORMAÇOES DA CONTA
	private String name;
	private String fakeName;
	private UUID uniqueId;

	// DADOS DA CONTA
	private int fichas = 0;
	private int money = 0;
	private int xp = 0;

	// REPORT
	private int reportPoints = 1000;
	private int rejectionLevel = 0;

	// DOUBLE XP
	private int doubleXpMultiplier = 0;
	private long lastActivatedMultiplier = Long.MIN_VALUE;
	private long lastVIPMultiplierReceived = Long.MIN_VALUE;

	private League league = League.UNRANKED;
	private Tag tag;
	private Tournament tournament = null;
	private AccountVersion accountVersion = AccountVersion.NONE;

	// ENDEREÇOS E NETWORKING
	private transient String ipAddress;
	private String lastIpAddress;

	// PLAYING
	private long onlineTime = 0;
	private long joinTime;
	private long lastLoggedIn;
	private long firstTimePlaying;

	// GRUPOS
	@Setter
	private Map<ServerStaff, Group> groups = new HashMap<>();
	@Setter
	private Map<RankType, Long> ranks = new HashMap<>();

	// AMIGOS
	private Map<UUID, Friend> friends = new HashMap<>();
	private Map<UUID, Request> friendRequests = new HashMap<>();
	@Setter
	private Map<UUID, Blocked> blockedPlayers = new HashMap<>();

	// CLANS
	private UUID clanUniqueId = null;
	// private transient UUID party;

	// DADOS PESSOAIS COMPARTILHADOS
	private String skype = "";
	private boolean skypeFriendOnly = true;
	private String twitter = "";
	private String youtubeChannel = "";
	private String steam = "";

	// CONFIGURACOES
	private AccountConfiguration configuration = new AccountConfiguration(this);

	// PAIS E LINGUA
	private String countryCode;
	private Language language = BattlebitsAPI.getDefaultLanguage();
	private TimeZone timeZone;

	// HISTORIA
	private PunishHistoric punishHistoric = new PunishHistoric();

	private transient boolean online;

	private String serverConnected = "";
	private ServerType serverConnectedType = ServerType.NONE;

	private Report report;

	@Getter(AccessLevel.NONE)
	private transient boolean screensharing = false;
	private transient String lastServer = "";

	public BattlePlayer(String name, UUID uniqueId, String ipAddress, String countryCode, String timeZoneCode) {
		this.name = name;
		this.uniqueId = uniqueId;
		this.fakeName = name;

		this.ipAddress = ipAddress;
		if (ipAddress != null)
			this.lastIpAddress = ipAddress;

		this.lastLoggedIn = TimeZoneConversor.getCurrentMillsTimeIn(TimeZone.GMT0);
		this.firstTimePlaying = TimeZoneConversor.getCurrentMillsTimeIn(TimeZone.GMT0);

		this.countryCode = countryCode;
		this.timeZone = TimeZone.fromString(timeZoneCode);
	}

	public Report newReport() {
		report = new Report(getUniqueId(), getName());
		DataPlayer.saveBattlePlayer(this, "report");
		return report;
	}

	public void clearReport() {
		report = null;
		DataPlayer.saveBattlePlayer(this, "report");
	}

	public boolean hasGroupPermission(Group group) {
		if (getServerGroup() == Group.YOUTUBERPLUS) {
			return Group.MOD.ordinal() >= group.ordinal();
		}
		return getServerGroup().ordinal() >= group.ordinal();
	}

	public Group getServerGroup() {
		Group group = Group.NORMAL;
		if (!getGroups().isEmpty()) {
			if (getGroups().containsKey(ServerType.NETWORK.getStaffType())) {
				group = getGroups().get(ServerType.NETWORK.getStaffType());
			} else if (getGroups().containsKey(serverConnectedType.getStaffType())) {
				group = getGroups().get(serverConnectedType.getStaffType());
			}
		}
		if (group == Group.NORMAL)
			if (isStaff())
				group = Group.STAFF;
		if (group == Group.NORMAL) {
			if (!getRanks().isEmpty()) {
				RankType expire = null;
				for (Entry<RankType, Long> expireRank : getRanks().entrySet()) {
					if (expire == null) {
						expire = expireRank.getKey();
					} else if (expireRank.getKey().ordinal() > expire.ordinal()) {
						expire = expireRank.getKey();
					}
				}
				if (expire != null)
					group = Group.valueOf(expire.name());
			}
		}
		if (BattlebitsAPI.isChristmas()) {
			if (group.ordinal() < Group.ULTIMATE.ordinal())
				return Group.ULTIMATE;
		}
		return group;
	}

	public boolean isStaff() {
		for (Group group : getGroups().values()) {
			if (group.ordinal() > Group.HELPER.ordinal()) {
				return true;
			}
		}
		return false;
	}

	public long getOnlineTime() {
		return (System.currentTimeMillis() - joinTime) + onlineTime;
	}

	public String getHostnamea() {
		return ipAddress;
	}

	public Clan getClan() {
		if (clanUniqueId == null)
			return null;
		return BattlebitsAPI.getClanCommon().getClan(clanUniqueId);
	}

	public boolean isScreensharing() {
		return screensharing;
	}

	public void setFakeName(String fakeName) {
		if (!fakeName.equals(this.fakeName)) {
			this.fakeName = fakeName;
			DataPlayer.saveBattlePlayer(this, "fakeName");
		}
	}

	public void setScreensharing(boolean screensharing) {
		if (screensharing) {
			lastServer = getServerConnected();
		}
		this.screensharing = screensharing;
		DataPlayer.saveBattlePlayer(this, "lastServer");
		DataPlayer.saveBattlePlayer(this, "screensharing");
	}

	public void activateDoubleXp() {
		removeDoubleXpMultiplier(1);
		lastActivatedMultiplier = System.currentTimeMillis() + BattlebitsAPI.MULTIPLIER_DURATION;
		DataPlayer.saveBattlePlayer(this, "lastActivatedMultiplier");
	}

	public void addDoubleXpMultiplier(int i) {
		setDoubleXpMultiplier(doubleXpMultiplier += i);
	}

	public void removeDoubleXpMultiplier(int i) {
		int a = doubleXpMultiplier - i;
		if (a < 0)
			a = 0;
		setDoubleXpMultiplier(a);
	}

	public void setDoubleXpMultiplier(int i) {
		doubleXpMultiplier = i;
		DataPlayer.saveBattlePlayer(this, "doubleXpMultiplier");
	}

	public void setRejectionLevel(int rejectionLevel) {
		this.rejectionLevel = rejectionLevel;
		if (this.rejectionLevel < 0)
			this.rejectionLevel = 0;
		DataPlayer.saveBattlePlayer(this, "rejectionLevel");
	}

	public void setReportPoints(int reportPoints) {
		this.reportPoints = reportPoints;
		if (this.reportPoints < 0)
			this.reportPoints = 0;
		DataPlayer.saveBattlePlayer(this, "reportPoints");
	}

	public int addFichas(int fichas) {
		this.fichas += fichas;
		setFichas(this.fichas);
		return this.fichas;
	}

	public int removeFichas(int fichas) {
		this.fichas -= fichas;
		if (this.fichas < 0)
			this.fichas = 0;
		setFichas(this.fichas);
		return this.fichas;
	}

	public void setFichas(int i) {
		this.fichas = i;
		DataPlayer.saveBattlePlayer(this, "fichas");
	}

	public int addMoney(int money) {
		int multiplier = 1 + getLeague().ordinal() + (hasGroupPermission(Group.ULTIMATE) ? 1 : 0);
		int plus = money * multiplier;
		this.money += plus;
		setMoney(this.money);
		return plus;
	}

	public int removeMoney(int money) {
		this.money -= money;
		if (this.money < 0)
			this.money = 0;
		setMoney(this.money);
		return this.money;
	}

	public void setMoney(int i) {
		this.money = i;
		DataPlayer.saveBattlePlayer(this, "money");
	}

	public void setXp(int xp) {
		if (getClan() != null)
			getClan().addXp(xp - this.xp);
		this.xp = xp;
		DataPlayer.saveBattlePlayer(this, "xp");
	}

	public int addXp(int xp) {
		if (xp < 0)
			xp = 0;
		if (isDoubleXPActivated())
			xp *= 2;
		int setarxp = this.xp + xp;
		setXp(setarxp);
		return xp;
	}

	public void setLanguage(Language language) {
		this.language = language;
		DataPlayer.saveBattlePlayer(this, "language");
	}

	public void setClanUniqueId(UUID clanUniqueId) {
		this.clanUniqueId = clanUniqueId;
		DataPlayer.saveBattlePlayer(this, "clanUniqueId");
	}

	public void setAccountVersion(AccountVersion accountVersion) {
		this.accountVersion = accountVersion;
		DataPlayer.saveBattlePlayer(this, "accountVersion");
	}

	public void setLeague(League league) {
		this.league = league;
		DataPlayer.saveBattlePlayer(this, "league");
	}

	public boolean isDoubleXPActivated() {
		return System.currentTimeMillis() < lastActivatedMultiplier;
	}

	public void saveGroups() {
		DataPlayer.saveBattlePlayer(this, "groups");
	}

	public void saveRanks() {
		DataPlayer.saveBattlePlayer(this, "ranks");
	}

	public void connect(String serverIp) {
		checkRanks();
		this.serverConnected = serverIp;
		this.serverConnectedType = ServerType.getServerType(serverIp);
		DataPlayer.saveBattlePlayer(this, "serverConnected");
		DataPlayer.saveBattlePlayer(this, "serverConnectedType");
	}

	public void setJoinData(String userName, String ipAdrress, String countryCode, String timeZoneCode) {
		checkRanks();
		setName(userName);
		configuration.setPlayer(this);
		this.ipAddress = ipAdrress;
		setTimeZone(timeZoneCode);
		joinTime = System.currentTimeMillis();
		setCountryCode(countryCode);
		this.online = true;
		setServerConnectedType(ServerType.NONE);

		DataPlayer.saveBattlePlayer(this, "joinTime");
		DataPlayer.saveBattlePlayer(this, "countryCode");
		DataPlayer.saveBattlePlayer(this, "online");
		DataPlayer.saveBattlePlayer(this, "serverConnectedType");
	}

	private void setName(String name) {
		if (!this.name.equals(name)) {
			this.name = name;
			DataPlayer.saveBattlePlayer(this, "name");
		}
	}

	public boolean setTag(Tag tag) {
		this.tag = tag;
		if (hasGroupPermission(Group.YOUTUBER)) {
			DataPlayer.saveBattlePlayer(this, "tag");
		}
		return true;
	}

	public void setTournament(Tournament tournament) {
		if (tournament != this.tournament) {
			this.tournament = tournament;
			DataPlayer.saveBattlePlayer(this, "tournament");
		}
	}

	private void setTimeZone(String timeZoneCode) {
		if (this.timeZone != TimeZone.fromString(timeZoneCode)) {
			this.timeZone = TimeZone.fromString(timeZoneCode);
			DataPlayer.saveBattlePlayer(this, "timeZone");
		}
	}

	private void setCountryCode(String countryCode) {
		if (!this.countryCode.equals(countryCode)) {
			this.countryCode = countryCode;
			DataPlayer.saveBattlePlayer(this, "countryCode");
		}
	}

	private void setServerConnectedType(ServerType serverConnectedType) {
		if (serverConnectedType != this.serverConnectedType) {
			this.serverConnectedType = serverConnectedType;
			DataPlayer.saveBattlePlayer(this, "serverConnectedType");
		}
	}

	public void checkForMultipliers() {
		if (System.currentTimeMillis() > lastVIPMultiplierReceived) {
			if (hasGroupPermission(Group.MODPLUS)) {
				return;
			} else if (hasGroupPermission(Group.ULTIMATE)) {
				doubleXpMultiplier += 5;
			} else if (hasGroupPermission(Group.PREMIUM)) {
				doubleXpMultiplier += 3;
			} else if (hasGroupPermission(Group.LIGHT)) {
				doubleXpMultiplier += 1;
			} else {
				return;
			}

			lastVIPMultiplierReceived = System.currentTimeMillis() + (1000l * 60l * 60l * 24l * 30l);
			DataPlayer.saveBattlePlayer(this, "doubleXpMultiplier");
			DataPlayer.saveBattlePlayer(this, "lastVIPMultiplierReceived");
		}
	}

	public void setLeaveData() {
		this.online = false;
		lastLoggedIn = System.currentTimeMillis();
		onlineTime = getOnlineTime();
		if (ipAddress != null)
			lastIpAddress = ipAddress;
		ipAddress = null;
		DataPlayer.saveBattlePlayer(this, "online");
		DataPlayer.saveBattlePlayer(this, "lastLoggedIn");
		DataPlayer.saveBattlePlayer(this, "onlineTime");
		DataPlayer.saveBattlePlayer(this, "lastIpAddress");
	}

	public Tag getTag() {
		if (tag == null)
			tag = Tag.valueOf(getServerGroup().toString());
		return tag;
	}

	public void checkRanks() {
		if (!getRanks().isEmpty()) {
			Iterator<Entry<RankType, Long>> it = getRanks().entrySet().iterator();
			boolean save = false;
			while (it.hasNext()) {
				Entry<RankType, Long> entry = it.next();
				if (TimeZoneConversor.getCurrentMillsTimeIn(TimeZone.GMT0) > entry.getValue()) {
					it.remove();
					save = true;
				}
			}
			if (save)
				DataPlayer.saveBattlePlayer(this, "ranks");
		}
	}

	public static Language getLanguage(UUID uuid) {
		if (getPlayer(uuid) == null)
			return BattlebitsAPI.getDefaultLanguage();
		return getPlayer(uuid).getLanguage();
	}

	public static BattlePlayer getPlayer(UUID uuid) {
		return BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
	}

}
