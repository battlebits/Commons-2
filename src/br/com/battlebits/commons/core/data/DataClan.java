package br.com.battlebits.commons.core.data;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.clan.Clan;

public class DataClan {

	public static Clan getClan(UUID uuid) {
		Clan clan = BattlebitsAPI.getClanCommon().getClan(uuid);
		if (clan == null) {
			// clan = getRedisClan(uuid);
			if (clan == null)
				clan = getMongoClan(uuid);
		}
		return clan;
	}

	public static Clan getClan(String name) {
		Clan clan = BattlebitsAPI.getClanCommon().getClan(name);
		if (clan == null) {
			// clan = getRedisClan(uuid);
			if (clan == null){
				clan = getMongoClan(name);
				//
			}
		}
		return clan;
	}

	public static Clan getMongoClan(UUID uuid) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("uniqueId", uuid)).first();
		if (found == null)
			return null;
		return BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), Clan.class);
	}

	public static Clan getMongoClan(String name) {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("name", name)).first();
		if (found == null)
			return null;
		return BattlebitsAPI.getGson().fromJson(BattlebitsAPI.getGson().toJson(found), Clan.class);
	}

	public void saveMongoClan(Clan clan, MongoClient client) {
		MongoDatabase database = client.getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		Document found = collection.find(Filters.eq("clanName", clan.getName())).first();
		if (found != null) {
			collection.updateOne(Filters.eq("clanName", clan.getName()),
					Document.parse(BattlebitsAPI.getGson().toJson(clan)));
		} else {
			collection.insertOne(Document.parse(BattlebitsAPI.getGson().toJson(clan)));
		}
	}

	public static UUID getNewUniqueId() {
		MongoDatabase database = BattlebitsAPI.getMongo().getClient().getDatabase("commons");
		MongoCollection<Document> collection = database.getCollection("clan");
		UUID uuid;
		do {
			uuid = UUID.randomUUID();
		} while (collection.find(Filters.eq("uniqueId", uuid.toString())).first() != null);
		return uuid;
	}

}
