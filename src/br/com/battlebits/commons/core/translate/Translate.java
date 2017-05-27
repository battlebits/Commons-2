package br.com.battlebits.commons.core.translate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.battlebits.commons.core.data.DataServer;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.backend.mongodb.MongoBackend;
import net.md_5.bungee.api.ChatColor;

public class Translate {
	private String database;
	private MongoBackend beckend;
	private Map<Language, Map<String, String>> languageTranslations = new HashMap<>();
	private static Pattern finder = Pattern.compile("§%(([\\S^)]+)%§)");

	public Translate(String database, MongoBackend beckend) {
		this.database = database;
		this.beckend = beckend;
	}

	public String getTranslation(Language language, String messageId) {
		return getTranslation(language, messageId, null, null);
	}

	public String getTranslation(Language language, String messageId, HashMap<String, String> replacement) {
		String[] target = replacement.keySet().toArray(new String[replacement.size()]);
		String[] replace = replacement.values().toArray(new String[replacement.size()]);
		return getTranslation(language, messageId, target, replace);
	}

	public String getTranslation(Language language, String messageId, String[]... replacement) {
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

	public String getTranslation(Language language, String messageId, String[] target, String[] replacement) {
		String message = getTranslationNoCheck(language, messageId, target, replacement);
		if (message == null) {
			message = "[NOT FOUND: '" + messageId + "']";
			BattlebitsAPI.debug(language.toString() + " > " + messageId + " > NAO ENCONTRADA");
			DataServer.addTranslationTag(language, messageId, this.database);
		}
		String m = ChatColor.translateAlternateColorCodes('&', message);
		if (target != null && replacement != null)
			for (int i = 0; i < (target.length < replacement.length ? target.length : replacement.length); i++) {
				m = m.replace(target[i], replacement[i]);
			}
		return m;
	}
	
	public String getTranslationNoCheck(Language language, String messageId, String[] target, String[] replacement) {
		String message = null;
		Matcher matcher = finder.matcher(messageId);
		while (matcher.find()) {
			messageId = matcher.group(2).toLowerCase();
		}
		if (!languageTranslations.containsKey(language)) {
			BattlebitsAPI.debug(language.toString() + " > NAO ENCONTRADA");
			return null;
		}
		if (languageTranslations.get(language).containsKey(messageId)) {
			message = languageTranslations.get(language).get(messageId);
		}
		return message;
	}

	public void loadTranslations() {
		for(Language lang : Language.values()){
			languageTranslations.put(lang, loadTranslation(lang));
			BattlebitsAPI.debug(this.database + " > " + lang.toString() + " > CARREGADA");
		}
	}
	

	@SuppressWarnings("unchecked")
	private Map<String, String> loadTranslation(Language language) {
		MongoDatabase database = beckend.getClient().getDatabase(this.database);
		MongoCollection<Document> collection = database.getCollection("translation");
		Document found = collection.find(Filters.eq("language", language.toString())).first();
		if (found != null) {
			return (Map<String, String>) found.get("map");
		}
		collection.insertOne(new Document("language", language.toString()).append("map", new HashMap<>()));
		return new HashMap<>();
	}
}
