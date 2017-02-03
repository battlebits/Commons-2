package br.com.battlebits.commons;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.backend.mongodb.MongoBackend;
import br.com.battlebits.commons.core.backend.sql.MySQLBackend;
import br.com.battlebits.commons.core.translate.Language;

public class TranslationLoad {

	public static void main(String[] args) {
		BattlebitsAPI.setLogger(Logger.getGlobal());
		MySQLBackend mysql = new MySQLBackend("localhost", "ycommon", "root", "sMYDzGASzVE5hpCD", 3306);
		MongoBackend mongo = new MongoBackend("localhost", "commons", "commons", "", 27017);
		try {
			mysql.startConnection();
			mongo.startConnection();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return;
		}
		try (PreparedStatement statement = mysql.prepareStatment("SELECT * FROM `translations`")) {
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					Language lang = Language.valueOf(result.getString("language"));
					String json = result.getString("json");
					@SuppressWarnings("unchecked")
					Map<String, String> map = BattlebitsAPI.getGson().fromJson(json, Map.class);
					MongoCollection<Document> col = mongo.getClient().getDatabase("commons")
							.getCollection("translation");
					if (col.findOneAndUpdate(Filters.eq("language", lang.toString()),
							new Document("$set", new Document("map", map))) != null)
						col.insertOne(new Document("language", lang.toString()).append("map", map));
				}
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		try {
			mysql.closeConnection();
			mongo.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Traduções carregadas");
	}
}
