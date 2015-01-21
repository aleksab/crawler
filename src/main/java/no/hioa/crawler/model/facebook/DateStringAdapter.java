package no.hioa.crawler.model.facebook;

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DateStringAdapter implements JsonDeserializer<DateTime>
{
	public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		LocalDateTime local = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss+0000").parseLocalDateTime(json.getAsJsonPrimitive().getAsString());
		return local.toDateTime(DateTimeZone.UTC);
		// return DateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss+0000"));
	}
}
