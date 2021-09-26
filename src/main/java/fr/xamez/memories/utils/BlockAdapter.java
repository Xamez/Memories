package fr.xamez.memories.utils;

//public class BlockAdapter implements JsonDeserializer<Block>, JsonSerializer<Block> {

	/*@Override
	public Block deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext ) throws JsonParseException {
		final JavaPlugin plugin = JavaPlugin.getPlugin(CiteMemoryBuild.class);
		final JsonObject obj = (JsonObject) json;
		final BlockData blockData = plugin.getServer().createBlockData(obj.get( "blockData" ).getAsString());
		return ;
	}

	@Override
	public JsonElement serialize( Block block, Type type, JsonSerializationContext jsonSerializationContext ) {
		final JsonObject obj = new JsonObject();
		obj.addProperty( "world", location.getWorld().getName() );
		obj.addProperty( "x", location.getX() );
		obj.addProperty( "y", location.getY() );
		obj.addProperty( "z", location.getZ() );
		obj.addProperty( "yaw", location.getYaw() );
		obj.addProperty( "pitch", location.getPitch() );
		return obj;
	}*/

//}