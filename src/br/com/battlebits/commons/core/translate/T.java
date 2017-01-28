package br.com.battlebits.commons.core.translate;

import java.util.HashMap;

public class T {

	public static String t(Language lang, String id) {
		return Translate.getTranslation(lang, id);
	}

	public static String t(Language lang, String id, HashMap<String, String> replace) {
		return Translate.getTranslation(lang, id, replace);
	}

	public static String t(Language lang, String id, String[]... replace) {
		return Translate.getTranslation(lang, id, replace);
	}

	public static String t(Language lang, String id, String[] target, String[] replace) {
		return Translate.getTranslation(lang, id, target, replace);
	}

}
