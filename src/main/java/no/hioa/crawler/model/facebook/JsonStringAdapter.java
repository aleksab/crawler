package no.hioa.crawler.model.facebook;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class JsonStringAdapter implements JsonDeserializer<String>
{
	public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		return StringEscapeUtils.unescapeJson(json.getAsJsonPrimitive().getAsString());
	}
}
