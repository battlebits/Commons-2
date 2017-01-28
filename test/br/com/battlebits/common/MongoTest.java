package br.com.battlebits.common;

import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.backend.mongodb.MongoBackend;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;

public class MongoTest {

	public static void main(String[] args) {
		MongoBackend backend = new MongoBackend();
		backend.startConnection();
		MongoClient client = backend.getClient();
		MongoDatabase db = client.getDatabase("skywars");
		MongoCollection<Document> col = db.getCollection("data");
		
		FindIterable<Document> docs = col.find().sort(new Document("xp", -1)).limit(100);
		for(Document doc : docs) {
			System.out.println(doc.getString("name"));
			System.out.println(doc.getString("uniqueId"));
			System.out.println(doc.get("xp"));
		}

		// col.updateOne(Filters.eq("uniqueId",
		// "e24695ad-6618-471e-826a-2438f043a293"),
		// new Document("$set", new Document("configuration.ignoreAll",
		// false)));

		// Random r = new Random();
		//
		// col.deleteMany(Filters.exists("uuid"));
		//
		// timing = System.currentTimeMillis();
		// List<Document> docs = new ArrayList<>();
		// for (int i = 0; i < 200000; i++) {
		// Document doc = new Document("uuid", UUID.randomUUID().toString());
		// doc.put("number", r.nextInt(100000));
		// docs.add(doc);
		// }
		// col.insertMany(docs);
		// System.out.println("Inserting " + col.count() + ": " +
		// (System.currentTimeMillis() - timing) + " ms");

		// timing = System.currentTimeMillis();
		// for(Document doc : col.find()) {
		// doc.remove("agr");
		// doc.put("number", r.nextInt(100000));
		// }
		// System.out.println("Putting: " + (System.currentTimeMillis() -
		// timing) + " ms");

		// db.createCollection("accounts");
		backend.closeConnection();
	}

	public static void maina(String[] args) {
		MongoBackend backend = new MongoBackend();
		backend.startConnection();
		BattlebitsAPI.setMongo(backend);
		BattlebitsAPI.setLogger(Logger.getGlobal());
		for (Language lang : Language.values()) {
			Translate.loadTranslations(BattlebitsAPI.TRANSLATION_ID, lang, DataServer.loadTranslation(lang));
		}

		// DataServer.addTranslationTag(Language.PORTUGUESE, "admin");
		System.out.println(Translate.getyCommonMapTranslation(Language.PORTUGUESE));
	}
	
	public static class PlayerData {
	}

}
