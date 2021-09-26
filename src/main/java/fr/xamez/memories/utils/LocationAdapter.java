package fr.xamez.memories.utils;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class LocationAdapter implements JsonDeserializer<Location>, JsonSerializer<Location> {

	@Override
	public Location deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext ) throws JsonParseException {
		final JsonObject obj = (JsonObject) json;
		final JsonElement worldName = obj.get( "world" );
		final JsonElement x = obj.get( "x" );
		final JsonElement y = obj.get( "y" );
		final JsonElement z = obj.get( "z" );
		final JsonElement yaw = obj.get( "yaw" );
		final JsonElement pitch = obj.get( "pitch" );
		World world = Bukkit.getWorld( worldName.getAsString() );
		return new Location( world, x.getAsDouble(), y.getAsDouble(), z.getAsDouble(), yaw.getAsFloat(), pitch.getAsFloat() );
	}

	@Override
	public JsonElement serialize( Location location, Type type, JsonSerializationContext jsonSerializationContext ) {
		final JsonObject obj = new JsonObject();
		obj.addProperty( "world", location.getWorld().getName() );
		obj.addProperty( "x", location.getX() );
		obj.addProperty( "y", location.getY() );
		obj.addProperty( "z", location.getZ() );
		obj.addProperty( "yaw", location.getYaw() );
		obj.addProperty( "pitch", location.getPitch() );
		return obj;
	}

}