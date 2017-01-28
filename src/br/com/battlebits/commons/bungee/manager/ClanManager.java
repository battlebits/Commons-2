package br.com.battlebits.commons.bungee.manager;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.clan.Clan;

public class ClanManager {

	public Clan loadClan(UUID uniqueId) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("uniqueId", uniqueId.toString())).first();
		BattlebitsAPI.getClanCommon()
				.loadClan(BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), Clan.class));
		return BattlebitsAPI.getClanCommon().getClan(uniqueId);
	}

	public boolean clanNameExists(String clanName) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("clanName", clanName)).first();
		return found != null;
	}

	public boolean clanAbbreviationExists(String abbreviation) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("abbreviation", abbreviation)).first();
		return found != null;
	}

	public void updateClan(Clan clan) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("uniqueId", clan.getUniqueId())).first();
		if (found != null) {
			collection.updateOne(Filters.eq("clanName", clan.getName()),
					Document.parse(BattlebitsAPI.getGson().toJson(clan)));
		} else {
			collection.insertOne(Document.parse(BattlebitsAPI.getGson().toJson(clan)));
		}
	}

	public void disbandClan(Clan clan) {
		MongoClient client = BattlebitsAPI.getMongo().getClient();
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		collection.deleteOne(Filters.eq("uniqueId", clan.getUniqueId()));
	}
}
