package br.com.battlebits.commons;

import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import br.com.battlebits.commons.core.account.AccountCommon;
import br.com.battlebits.commons.core.account.AccountVersion;
import br.com.battlebits.commons.core.account.Tournament;
import br.com.battlebits.commons.core.backend.mongodb.MongoBackend;
import br.com.battlebits.commons.core.backend.redis.RedisBackend;
import br.com.battlebits.commons.core.backend.sql.MySQLBackend;
import br.com.battlebits.commons.core.clan.ClanCommon;
import br.com.battlebits.commons.core.party.PartyCommon;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import br.com.battlebits.commons.util.mojang.NameFetcher;
import br.com.battlebits.commons.util.mojang.UUIDFetcher;
import br.com.battlebits.commons.util.mojang.UUIDGetter;
import br.com.battlebits.commons.util.timezone.TimeZone;
import lombok.Getter;
import lombok.Setter;

public class BattlebitsAPI {

	@Getter
	@Setter
	private static MySQLBackend commonsMysql;
	@Getter
	@Setter
	private static MongoBackend commonsMongo;
	@Getter
	@Setter
	private static RedisBackend commonsRedis;

	@Getter
	private static ClanCommon clanCommon = new ClanCommon();
	@Getter
	private static PartyCommon partyCommon = new PartyCommon();
	@Getter
	private static AccountCommon accountCommon = new AccountCommon();
	@Getter
	private static AccountVersion defaultAccountVersion = AccountVersion.SEASON_3_BUG_2;
	@Getter
	private static Tournament tournament = null;
	private static UUIDFetcher uuidFetcher = new UUIDFetcher();
	private static NameFetcher nameFetcher = new NameFetcher();

	public final static long MULTIPLIER_DURATION = 60000 * 60;
	public final static String TRANSLATION_ID = "";
	public final static String FORUM_WEBSITE = "forum.battlebits.net";
	public final static String WEBSITE = "www.battlebits.net";
	public final static String STORE = "loja.battlebits.net";
	public final static String ADMIN_EMAIL = "admin@battlebits.net";
	public final static String TWITTER = "@BattlebitsMC";
	public static TimeZone DEFAULT_TIME_ZONE = TimeZone.GMT0;
	@Getter
	public static Language defaultLanguage = Language.PORTUGUESE;

	@Getter
	@Setter
	public static Translate translate;
	
	@Setter
	private static UUIDGetter getter;

	@Getter
	@Setter
	private static Logger logger;

	@Getter
	@Setter
	private static String serverId;
	
	@Getter
	@Setter
	private static ServerType serverType;
	
	@Getter
	@Setter
	private static String serverAddress;

	private static boolean debugMode = false;
	private static final Calendar CALENDAR = Calendar.getInstance();
	@Getter
	private static final Gson gson = new Gson();
	@Getter
	private static final JsonParser parser = new JsonParser();

	public static String getBungeeChannel() {
		return "yCommon";
	}

	public static boolean isChristmas() {
		return CALENDAR.get(Calendar.MONDAY) == Calendar.DECEMBER && CALENDAR.get(Calendar.DAY_OF_MONTH) == 28;
	}

	public static boolean isChildrensDay() {
		return CALENDAR.get(Calendar.MONDAY) == Calendar.OCTOBER && CALENDAR.get(Calendar.DAY_OF_MONTH) == 12;
	}

	public static boolean isNewYear() {
		return CALENDAR.get(Calendar.MONDAY) == Calendar.JANUARY && CALENDAR.get(Calendar.DAY_OF_MONTH) == 1;
	}

	public static void debug(String debugStr) {
		if (debugMode) {
			getLogger().log(Level.INFO, debugStr);
		}
	}

	public static UUID getUUIDOf(String string) {
		if (getter.getUuid(string) != null)
			return getter.getUuid(string);
		return uuidFetcher.getUUID(string);
	}

	public static String getNammeOf(UUID uuid) {
		return nameFetcher.getUsername(uuid);
	}

}
