package br.com.battlebits.commons.core.translate;

import java.util.HashMap;
import java.util.Map;

public class T {

	private static Map<Object, Translate> translates = new HashMap<>();

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
