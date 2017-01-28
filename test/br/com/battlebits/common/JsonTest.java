package br.com.battlebits.common;

import java.lang.reflect.Modifier;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;

public class JsonTest {

	public static void main(String[] args) {
		Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
		BattlePlayer player = new BattlePlayer("GustavoInacio", UUID.fromString("e24695ad-6618-471e-826a-2438f043a293"),
				null, "BR", "0");
		JsonObject jsonObject = BattlebitsAPI.getParser().parse(gson.toJson(player)).getAsJsonObject();
		if (!jsonObject.has("money"))
			return;
		JsonElement element = jsonObject.get("money");
		JsonObject json = new JsonObject();
		json.add("uniqueId", new JsonPrimitive(player.getUniqueId().toString()));
		json.add("field", new JsonPrimitive("money"));
		json.add("value", element);
		System.out.println(json.toString());
	}
}
