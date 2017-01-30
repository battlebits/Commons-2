package br.com.battlebits.commons.core.translate;

import java.util.HashMap;
import java.util.Map;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.data.DataServer;
import net.md_5.bungee.api.ChatColor;

public class Translate {
	private static Map<String, Map<Language, Map<String, String>>> languageTranslations = new HashMap<>();

	public static String getTranslation(Language language, String messageId) {
		return getTranslation(language, messageId, null, null);
	}

	public static String getTranslation(Language language, String messageId, HashMap<String, String> replacement) {
		String[] target = replacement.keySet().toArray(new String[replacement.size()]);
		String[] replace = replacement.values().toArray(new String[replacement.size()]);
		return getTranslation(language, messageId, target, replace);
	}

	public static String getTranslation(Language language, String messageId, String[]... replacement) {
		String[] target = new String[replacement.length];
		String[] replace = new String[replacement.length];

		for (int i = 0; i < replacement.length; i++) {
			String[] s = replacement[i];
			if (s.length >= 2) {
				target[i] = s[0];
				replace[i] = s[1];
			}
		}

		return getTranslation(language, messageId, target, replace);
	}

	public static String getTranslation(Language language, String messageId, String[] target, String[] replacement) {
		String message = null;

		for (Map<Language, Map<String, String>> translations : languageTranslations.values()) {
			if (!translations.containsKey(language)) {
				BattlebitsAPI.debug(language.toString() + " > NAO ENCONTRADA");
				continue;
			}
			if (!translations.get(language).containsKey(messageId)) {
				continue;
			}
			message = translations.get(language).get(messageId);
			break;
		}

		if (message == null) {
			message = "[NOT FOUND: '" + messageId + "']";
			BattlebitsAPI.debug(language.toString() + " > " + messageId + " > NAO ENCONTRADA");
			DataServer.addTranslationTag(language, messageId);
		}
		String m = ChatColor.translateAlternateColorCodes('&', message);
		if (target != null && replacement != null)
			for (int i = 0; i < (target.length < replacement.length ? target.length : replacement.length); i++) {
				m = m.replace(target[i], replacement[i]);
			}
		return m;
	}

	public static String getyCommonMapTranslation(Language language) {
		if (!languageTranslations.get(BattlebitsAPI.TRANSLATION_ID).containsKey(language)) {
			BattlebitsAPI.debug(language.toString() + " > NAO ENCONTRADA");
			return null;
		}
		return BattlebitsAPI.getGson().toJson(languageTranslations.get(BattlebitsAPI.TRANSLATION_ID).get(language));
	}

	public static void loadTranslations(String translationType, Language lang, Map<String, String> map) {
		Map<Language, Map<String, String>> map2 = languageTranslations.get(translationType);
		if (map2 == null) {
			map2 = new HashMap<>();
			languageTranslations.put(translationType, map2);
		}
		map2.put(lang, map);
		BattlebitsAPI.debug(lang.toString() + " > CARREGADA");
	}
}
