package br.com.battlebits.commons.core.data;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.translate.Language;

public class DataServer {

	@SuppressWarnings("unchecked")
	public static Map<String, String> loadTranslation(Language language) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("translation");
		Document found = collection.find(Filters.eq("language", language.toString())).first();
		if (found != null) {
			return (Map<String, String>) found.get("map");
		}
		collection.insertOne(new Document("language", language.toString()).append("map", new HashMap<>()));
		return new HashMap<>();
	}

	public static void addTranslationTag(Language language, String tag) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("translation");
		Document found = collection.find(Filters.eq("language", language.toString())).first();
		if (found != null) {
			collection.updateOne(Filters.eq("language", language.toString()),
					new Document("$set", new Document("map." + tag, tag.replace("-", " "))));
		} else {
			HashMap<String, String> str = new HashMap<>();
			str.put(tag, "[NOT FOUND: '" + tag + "']");
			collection.insertOne(new Document("language", language.toString()).append("map", str));
		}
	}
}
