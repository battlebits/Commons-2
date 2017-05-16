package br.com.battlebits.commons.core.translate;

import java.util.HashMap;
import java.util.Map;

import br.com.battlebits.commons.BattlebitsAPI;

public class T {

	private static Map<Object, Translate> translates = new HashMap<>();

	public static String getTranslation(Language lang, String id) {
		String string = null;
		for (Translate trans : translates.values()) {
			string = trans.getTranslationNoCheck(lang, id, null, null);
		}
		if (string == null)
			string = BattlebitsAPI.getTranslate().getTranslation(lang, id);
		return string;
	}

	public static void loadTranslate(Object main, Translate translate) {
		translates.put(main, translate);
	}

	public static String t(Object main, Language lang, String id) {
		return translates.containsKey(main) ? translates.get(main).getTranslation(lang, id) : "";
	}

	public static String t(Object main, Language lang, String id, HashMap<String, String> replace) {
		return translates.containsKey(main) ? translates.get(main).getTranslation(lang, id, replace) : "";
	}

	public static String t(Object main, Language lang, String id, String[]... replace) {
		return translates.containsKey(main) ? translates.get(main).getTranslation(lang, id, replace) : "";
	}

	public static String t(Object main, Language lang, String id, String[] target, String[] replace) {
		return translates.containsKey(main) ? translates.get(main).getTranslation(lang, id, target, replace) : "";
	}

}
