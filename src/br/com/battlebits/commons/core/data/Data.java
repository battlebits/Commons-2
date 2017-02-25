package br.com.battlebits.commons.core.data;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import br.com.battlebits.commons.BattlebitsAPI;

public abstract class Data {
	protected static final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC, Modifier.PROTECTED)
			.create();
	
	protected static <T> T parseRedisTree(Map<String, String> data, Class<T> clazz)
	{
		JsonObject jsonObject = new JsonObject();
		
		for (Entry<String, String> entry : data.entrySet())
		{
			jsonObject.add(entry.getKey(), BattlebitsAPI.getParser().parse(entry.getValue()));
		}
		
		return BattlebitsAPI.getGson().fromJson(jsonObject, clazz);
	}
	
	protected static Map<String, String> toRedisTree(Object object)
	{
		Map<String, String> tree = new HashMap<>();
		
		JsonObject jsonTree = (JsonObject) gson.toJsonTree(object);
		
		for (Entry<String, JsonElement> entry : jsonTree.entrySet())
		{
			String key = entry.getKey();
			
			String value;
			
			if (!entry.getValue().isJsonPrimitive())
				value = entry.getValue().toString();
			else
				value = entry.getValue().getAsString();
			
			tree.put(key, value);
		}
		
		return tree;
	}
}
