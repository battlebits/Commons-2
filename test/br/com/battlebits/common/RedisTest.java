package br.com.battlebits.common;

import br.com.battlebits.commons.core.backend.redis.RedisBackend;
import redis.clients.jedis.Jedis;

public class RedisTest {

	public static void main(String[] args) {
		RedisBackend backend = new RedisBackend();
		backend.startConnection();
//
//		Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
//
//		JsonParser parser = new JsonParser();

		// str
		try (Jedis jedis = backend.getPool().getResource()) {
			jedis.flushDB();
//			Map<String, String> fields = jedis.hgetAll("account:e24695ad-6618-471e-826a-2438f043a293");
//
//			JsonObject obj = new JsonObject();
//
//			for (Entry<String, String> entry : fields.entrySet()) {
//				obj.add(entry.getKey(), parser.parse(entry.getValue()));
//			}
//
//			BattlePlayer parsed = BattlebitsAPI.getGson().fromJson(obj.toString(), BattlePlayer.class);
//			parsed.getGroups().put(ServerStaff.NETWORK, Group.DONO);
//
//			jedis.hset("account:e24695ad-6618-471e-826a-2438f043a293", "groups", gson.toJson(parsed.getGroups()));

			// System.out.println(obj.toJSONString());

			// for (Field field : BattlePlayer.class.getDeclaredFields()) {
			// if (Modifier.isTransient(field.getModifiers()))
			// continue;
			// if (field.getType().isEnum() ||
			// field.getType().isAssignableFrom(String.class)
			// || field.getType().isAssignableFrom(int.class) ||
			// field.getType().isAssignableFrom(long.class)
			// || field.getType().isAssignableFrom(double.class)
			// || field.getType().isAssignableFrom(boolean.class))
			// System.out.println(field.getName() + " - " +
			// field.getType().getSimpleName());
			// else if (field.getType() == Map.class) {
			// System.out.println(field.getName() + " - MAPA - " +
			// field.getType().getSimpleName());
			// } else if (field.getType() == UUID.class) {
			//
			// } else {
			// System.out.println("");
			// System.out.println(field.getName() + " - " +
			// field.getType().getSimpleName());
			// for (Field f : field.getType().getDeclaredFields()) {
			// System.out.println(" " + f.getName() + " - " +
			// f.getType().getSimpleName());
			// }
			// }
			// }
			// TODO Publish
		}
		backend.closeConnection();
	}

}
